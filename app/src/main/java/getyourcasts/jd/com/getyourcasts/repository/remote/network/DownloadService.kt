package getyourcasts.jd.com.getyourcasts.repository.remote.network

import android.app.NotificationManager
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.support.v7.app.NotificationCompat
import android.util.Log
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.DownloadListener
import com.androidnetworking.interfaces.DownloadProgressListener
import com.androidnetworking.internal.DownloadProgressHandler
import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.repository.local.Contract
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import getyourcasts.jd.com.getyourcasts.util.StorageUtil
import getyourcasts.jd.com.getyourcasts.view.adapter.MyDownloadProgressListener
import getyourcasts.jd.com.getyourcasts.viewmodel.EpisodeState
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

/**
 * Created by chuondao on 9/11/17.
 */

class DownloadService : Service() {

    private val binder = DownloadServiceBinder()
    private var notifyManager: NotificationManager? = null
    private var viewModel: PodcastViewModel? = null
    private var notificationMap: MutableMap <String,Pair<Int,NotificationCompat.Builder>> = HashMap()
    private var notiDisposable: Disposable? = null
    private var currNotiId = 0

    companion object {
        private val TAG = DownloadService::class.java.simpleName
    }


    private fun initNetworkingLibrary() {
        AndroidNetworking.initialize(applicationContext)
    }

    private fun initDownloadSubscriberForNotification () {
        PodcastViewModel.subscribeEpisodeSubject(
                object: Observer<EpisodeState> {
                    override fun onNext(t: EpisodeState) {
                        val notiBuilder = notificationMap[t.uniqueId]!!.second
                        val notiId = notificationMap[t.uniqueId]!!.first
                        when (t.state) {
                            EpisodeState.EPISODE_DOWNLOADING -> {
                                notiBuilder!!.setProgress(100, t.downloadProgress, false)
                                // notify manager
                                notiBuilder.setContentText(getString(R.string.download_in_prog))
                                notifyManager!!.notify(notiId, notiBuilder.build())
                            }
                            EpisodeState.EPISODE_DOWNLOADED -> {
                                // notify manager
                                notiBuilder!!.setContentText(getString(R.string.download_complete))
                                notifyManager!!.notify(notiId, notiBuilder.build())
                            }

                        }
                    }

                    override fun onComplete() {

                    }

                    override fun onSubscribe(d: Disposable) {
                        if (notiDisposable != null) notiDisposable!!.dispose()
                        notiDisposable = d
                    }

                    override fun onError(e: Throwable) {

                    }

                }
        )
    }


    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG, "Download Service bound ! ")
        initNetworkingLibrary()
        initDownloadSubscriberForNotification()
        viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(this))
        return binder
    }


    override fun onRebind(intent: Intent) {
        initNetworkingLibrary()
        initDownloadSubscriberForNotification()
        super.onRebind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        notifyManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d(TAG, "Download Service Unbound")
        return super.onUnbind(intent)
    }


    inner class DownloadServiceBinder : Binder() {
        val service: DownloadService
            get() = this@DownloadService
    }

    private fun buildProgressNotification(fileName: String): NotificationCompat.Builder {
        val mBuilder = NotificationCompat.Builder(this)
        mBuilder.setContentTitle(fileName)
                .setContentText(getString(R.string.download_in_prog))
                .setSmallIcon(R.mipmap.ic_todownload)
        return mBuilder
    }


    /**
     * the request download link
     * will be enqued and the id for the request will be returned
     * @return: -1 if failed to enqueue otherwise valid Id will be returned
     */
     fun requestDownLoad (episode: Episode,
                        dirPath: String,
                        filename: String): Long {
        if (!NetworkHelper.isConnectedToNetwork(this)) {
            PodcastViewModel.updateEpisodeSubject(
                    EpisodeState(episode.uniqueId, EpisodeState.EPISODE_FETCHED, -1))
            NetworkHelper.showNetworkErrorDialog(this)
            return -1L
        }

        val req = AndroidNetworking.download(episode.downloadUrl, dirPath, filename)
                .setTag(episode.uniqueId)
                .setPriority(Priority.MEDIUM)
                .build()
        req.downloadProgressListener = MyDownloadProgressListener(episode)
        // remove duplicate filename in the db
        StorageUtil.cleanUpOldFile(episode, this)
        // register complete listener action
        req.startDownload( object: DownloadListener {
            override fun onDownloadComplete() {
                val cvUpdate = ContentValues()
                cvUpdate.put(Contract.EpisodeTable.LOCAL_URL, "$dirPath/$filename")
                cvUpdate.put(Contract.EpisodeTable.DOWNLOAD_STATUS, EpisodeState.EPISODE_DOWNLOADED)
                viewModel!!.getUpdateEpisodeObservable(episode, cvUpdate)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                object : Observer<Boolean> {
                                    override fun onSubscribe(d: Disposable) {

                                    }

                                    override fun onNext(res: Boolean) {
                                        if (res) {
                                            Log.d(TAG, "Successfully update Downloaded Episode " + episode)
                                            if (res)
                                                PodcastViewModel.updateEpisodeSubject(
                                                        EpisodeState(episode.uniqueId,
                                                                EpisodeState.EPISODE_DOWNLOADED,
                                                                100))
                                        } else {
                                            Log.e(TAG, "Failed update downloaded episode " + episode)
                                        }
                                    }

                                    override fun onError(e: Throwable) {
                                        e.printStackTrace()
                                    }

                                    override fun onComplete() {

                                    }
                                }
                        )
            }

            override fun onError(anError: ANError?) {
                if (anError != null) {
                    Log.e(TAG, anError.toString())
                    anError.printStackTrace()
                }
                val cvUpdate = ContentValues()
                cvUpdate.put(Contract.EpisodeTable.LOCAL_URL, "")
                cvUpdate.put(Contract.EpisodeTable.DOWNLOAD_STATUS, EpisodeState.EPISODE_FETCHED)
                viewModel!!.getUpdateEpisodeObservable(episode, cvUpdate)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                object : Observer<Boolean> {
                                    override fun onSubscribe(d: Disposable) {

                                    }

                                    override fun onNext(res: Boolean) {
                                        if (res) {
                                            Log.d(TAG, "Failed to download - update Episode DB " + episode)
                                            if (res)
                                                PodcastViewModel.updateEpisodeSubject(
                                                        EpisodeState(episode.uniqueId,
                                                                EpisodeState.EPISODE_FETCHED,
                                                                -1))
                                        } else {
                                            Log.e(TAG, "Failed update downloaded episode " + episode)
                                        }
                                    }

                                    override fun onError(e: Throwable) {
                                        e.printStackTrace()
                                    }

                                    override fun onComplete() {

                                    }
                                }
                        )
            }

        })
        // register notification progress
        val notiBuilder = buildProgressNotification(episode.title)
        notiBuilder.build()
        notificationMap.put(episode.uniqueId,  Pair(currNotiId++, notiBuilder))
        return 1L
    }

    fun requestStopDownload(epUniqueId: String) {
        AndroidNetworking.cancel(epUniqueId)
    }
}
