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

import com.tonyodev.fetch.Fetch
import com.tonyodev.fetch.listener.FetchListener
import com.tonyodev.fetch.request.Request
import com.tonyodev.fetch.request.RequestInfo

import java.util.HashMap

import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.repository.local.Contract
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.util.StorageUtil
import getyourcasts.jd.com.getyourcasts.view.adapter.EpisodeDownloadListener
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
    private var fetcher: Fetch? = null
    private var listReqIds: MutableMap<Long, Long> = HashMap()
    private var notifyManager: NotificationManager? = null
    private var currentId = 0
    private var viewModel: PodcastViewModel? = null


    private fun initFetch() {
        fetcher = Fetch.newInstance(this)
        fetcher!!.setConcurrentDownloadsLimit(CONCC_LIMIT)
        listReqIds = HashMap()
        // remove all request from fetch when first start !!
        fetcher!!.removeRequests()
    }


    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG, "Download Service bound ! ")
        initFetch()
        viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(this))
        return binder
    }


    override fun onRebind(intent: Intent) {
        if (!fetcher!!.isValid) {
            initFetch()
        }
        super.onRebind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        notifyManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
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

    private fun registerLisenerForNotiProg(ep: Episode,
                                           transId: Long,
                                           notiBuilder: NotificationCompat.Builder,
                                           notiId: Int,
                                           fullUrl: String) {
        registerListener(object : EpisodeDownloadListener(transId) {
            override fun onProgressUpdate(progress: Int) {
                notiBuilder.setProgress(100, progress, false)
                notifyManager!!.notify(notiId, notiBuilder.build())
                PodcastViewModel.updateEpisodeSubject(
                        EpisodeState(ep.uniqueId,
                                EpisodeState.DOWNLOADING,
                                transId))
            }

            override fun onComplete() {
                // remove progress bar
                notiBuilder.setProgress(0, 0, false)
                // notify manager
                notiBuilder.setContentText(getString(R.string.download_complete))
                notifyManager!!.notify(notiId, notiBuilder.build())
                // remove request to avoid not able to download it again
                fetcher!!.removeRequest(transId)
                // update the DB
                val cvUpdate = ContentValues()
                cvUpdate.put(Contract.EpisodeTable.LOCAL_URL, fullUrl)
                cvUpdate.put(Contract.EpisodeTable.DOWNLOAD_STATUS, EpisodeState.DOWNLOADED)
                viewModel!!.getUpdateEpisodeObservable(ep, cvUpdate)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                object : Observer<Boolean> {
                                    override fun onSubscribe(d: Disposable) {

                                    }

                                    override fun onNext(res: Boolean) {
                                        if (res!!) {
                                            Log.d(TAG, "Successfully update Downloaded Episode " + ep)
                                            if (res)
                                                PodcastViewModel.updateEpisodeSubject(
                                                        EpisodeState(ep.uniqueId,
                                                                EpisodeState.DOWNLOADED,
                                                                transId))
                                        } else {
                                            Log.e(TAG, "Failed update downloaded episode " + ep)
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

            override fun onStop() {
                PodcastViewModel.updateEpisodeSubject(
                        EpisodeState(ep.uniqueId,
                                EpisodeState.FETCHED,
                                transId))
                notiBuilder.setProgress(0, 0, false)
                notiBuilder.setContentText(getString(R.string.download_cancelled))
                notifyManager!!.notify(notiId, notiBuilder.build())
            }

            override fun onError() {
                // failed to download remove everything even partial file
                //                fetcher.removeRequest(transId);
                //fetcher.remove(transId);
                Log.e(TAG, "Failed to request download " + ep.downloadUrl)
                val cvUpdate = ContentValues()
                cvUpdate.put(Contract.EpisodeTable.LOCAL_URL, "")
                cvUpdate.put(Contract.EpisodeTable.DOWNLOAD_STATUS, EpisodeState.FETCHED)
                viewModel!!.getUpdateEpisodeObservable(ep, cvUpdate)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                object : Observer<Boolean> {
                                    override fun onSubscribe(d: Disposable) {

                                    }

                                    override fun onNext(res: Boolean) {
                                        if (res!!) {
                                            Log.d(TAG, "Failed to download Epsiode. Update Episode as Fetched only  " +
                                                    "" + ep)
                                            PodcastViewModel.updateEpisodeSubject(
                                                    EpisodeState(ep.uniqueId,
                                                            EpisodeState.FETCHED,
                                                            transId))
                                        } else {
                                            Log.e(TAG, "Failed to update DB as Fetched after failed to request " + "download")
                                        }

                                    }

                                    override fun onError(e: Throwable) {

                                    }

                                    override fun onComplete() {

                                    }
                                }
                        )

            }
        })


    }


    /**
     * the request download link
     * will be enqued and the id for the request will be returned
     * @return: -1 if failed to enqueue otherwise valid Id will be returned
     */
    fun requestDownLoad(episode: Episode,
                        dirPath: String,
                        filename: String): Long {
        if (!NetworkHelper.isConnectedToNetwork(this)) {
            PodcastViewModel.updateEpisodeSubject(
                    EpisodeState(episode.uniqueId, EpisodeState.FETCHED, -1))
            NetworkHelper.showNetworkErrorDialog(this)
            return -1L
        }

        if (fetcher!!.isValid) {
            val req = Request(episode.downloadUrl, dirPath, filename)
            // remove duplicate filename in the db
            StorageUtil.cleanUpOldFile(episode, this)
            // temporary fix to remove request at beginning
            if (fetcher!!.contains(req)) {
                val info = fetcher!!.get(req)
                if (info != null) {
                    fetcher!!.remove(info.id)
                }
            }
            val id = fetcher!!.enqueue(req)
            // failed to enqueue
            if (id < 0) {
                Log.d(TAG, "Failed to enqueue !")
                return -1L
            }
            listReqIds.put(id, id)
            val fullUrl = dirPath + "/" + filename
            // showing download progress
            PodcastViewModel.updateEpisodeSubject(
                    EpisodeState(episode.uniqueId, EpisodeState.DOWNLOADING, id))
            registerLisenerForNotiProg(episode,
                    id,
                    buildProgressNotification(episode.title),
                    currentId++,
                    fullUrl)

            // notify other views to change status
            return id
        }

        return -1L
    }


    fun registerListener(listener: FetchListener) {
        if (fetcher!!.isValid) {
            fetcher!!.addFetchListener(listener)
            Log.d(TAG, "Successfully register Listener for Fetch download")
        } else {
            Log.d(TAG, "Failed to register Listener!!")
        }
    }

    fun unregisterListener(listener: FetchListener) {
        if (fetcher!!.isValid) {
            fetcher!!.removeFetchListener(listener)
        }
    }

    fun requestStopDownload(transId: Long) {
        if (fetcher!!.isValid) {
            fetcher!!.pause(transId)
            fetcher!!.removeRequest(transId)
        }
    }

    companion object {
        private val TAG = DownloadService::class.java.simpleName
        private val CONCC_LIMIT = 2
    }
}
