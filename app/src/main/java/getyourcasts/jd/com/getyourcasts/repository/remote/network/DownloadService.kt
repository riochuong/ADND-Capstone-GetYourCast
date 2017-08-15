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
import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.repository.local.Contract
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.util.StorageUtil
import getyourcasts.jd.com.getyourcasts.view.adapter.EpisodeDownloadListener
import getyourcasts.jd.com.getyourcasts.viewmodel.EpisodeState
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel
import io.reactivex.android.schedulers.AndroidSchedulers


/**
 * Created by chuondao on 7/30/17.
 */

class DownloadService : Service() {

    private var binder: IBinder = DownloadServiceBinder()
    private lateinit var fetcher: Fetch
    private var listReqIds: MutableMap<Long,Long> = HashMap<Long,Long>()
    private lateinit var notifyManager : NotificationManager
    private var currentId = 0

    private lateinit  var viewModel : PodcastViewModel

    companion object {
        val TAG = DownloadService.javaClass.simpleName
        const val CONCC_LIMIT = 2

    }

    private fun initFetch(){
        fetcher = Fetch.newInstance(this)
        fetcher.setConcurrentDownloadsLimit(CONCC_LIMIT)
        listReqIds  = HashMap<Long,Long>()
    }




    override fun onBind(intent: Intent?): IBinder {
        Log.d(TAG,"Download Service bound ! ")
        initFetch()
        viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(this))
        return binder
    }

    override fun onRebind(intent: Intent?) {
        if (! fetcher.isValid){
            initFetch()
        }
        super.onRebind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        notifyManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG,"Download Service Unbound")
        return super.onUnbind(intent)
    }

    inner class DownloadServiceBinder: Binder(){
          fun getService() : DownloadService {
              return this@DownloadService
          }
    }

    private fun buildProgressNotification (fileName: String): NotificationCompat.Builder{
        val mBuilder = NotificationCompat.Builder(this)
        mBuilder.setContentTitle(fileName)
                .setContentText(getString(R.string.download_in_prog))
                .setSmallIcon(R.mipmap.ic_todownload)
        return mBuilder
    }

    private fun registerLisenerForNotiProg( ep: Episode,
                                            transId: Long,
                                           notiBuilder: NotificationCompat.Builder,
                                           notiId: Int,
                                            fullUrl: String){
        registerListener(
                object: EpisodeDownloadListener(transId) {
                    override fun onStop() {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onProgressUpdate(progress: Int) {
                        notiBuilder.setProgress(100, progress, false)
                        notifyManager.notify(notiId, notiBuilder.build())
                        PodcastViewModel.updateEpisodeSubject(
                                EpisodeState(ep.uniqueId,
                                                              EpisodeState.DOWNLOADING,
                                                              transId.toLong()))
                    }

                    override fun onComplete() {
                       // remove progress bar
                        notiBuilder.setProgress(0,0,false)
                        // notify manager
                        notiBuilder.setContentText(getString(R.string.download_complete))
                        notifyManager.notify(notiId, notiBuilder.build())
                        // update the DB
                        val cvUpdate = ContentValues()
                        cvUpdate.put(Contract.EpisodeTable.LOCAL_URL, fullUrl)
                        cvUpdate.put(Contract.EpisodeTable.DOWNLOADED, EpisodeState.DOWNLOADED)
                        viewModel.getUpdateEpisodeObservable(ep,cvUpdate)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        {
                                           Log.d(TAG,"Successfully update DB $ep")
                                            PodcastViewModel.updateEpisodeSubject(
                                                    EpisodeState(ep.uniqueId,
                                                            EpisodeState.DOWNLOADED,
                                                            transId.toLong()))
                                        },
                                        {

                                        }
                                )


                    }

                    override fun onError() {
                        Log.e(TAG, "Failed to request download ${ep.downloadUrl}")
                        val cvUpdate = ContentValues()
                        cvUpdate.put(Contract.EpisodeTable.LOCAL_URL, "")
                        cvUpdate.put(Contract.EpisodeTable.DOWNLOADED, 0)
                        viewModel.getUpdateEpisodeObservable(ep,cvUpdate)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        {
                                            Log.d(TAG,"Successfully update DB $ep")
                                            PodcastViewModel.updateEpisodeSubject(
                                                    EpisodeState(ep.uniqueId,
                                                            EpisodeState.FETCHED,
                                                            transId.toLong()))
                                        },
                                        {

                                        }
                                )

                    }

                }
        )
    }


    /**
     * the request download link
     * will be enqued and the id for the request will be returned
     * @return: -1 if failed to enqueue otherwise valid Id will be returned
     */
    fun requestDownLoad (episode: Episode,
                         url: String,
                         dirPath: String,
                         filename: String): Long{
        if (fetcher.isValid) {
            val req = Request(url, dirPath, filename)
            // remove duplicate filename in the db
            StorageUtil.cleanUpOldFile(episode, this)
            val id = fetcher.enqueue(req)
            listReqIds.put(id,id)
            val fullUrl = "${dirPath}/$filename"
            registerLisenerForNotiProg(episode,
                                        id,
                    buildProgressNotification(episode.title),
                    currentId++,
                    fullUrl)

            // notify other views to change status
            PodcastViewModel.updateEpisodeSubject(
                    EpisodeState(episode.uniqueId, EpisodeState.DOWNLOADING, id
                            .toLong()))

            return id
        }

        return -1
    }



    fun registerListener (listener: FetchListener){
        if (fetcher.isValid){
            fetcher.addFetchListener(listener)
            Log.d(TAG,"Successfully register Listener for Fetch download")
        }else{
            Log.d(TAG, "Failed to register Listener!!")
        }
    }

    fun unregisterListener(listener: FetchListener){
        if (fetcher.isValid){
            fetcher.removeFetchListener(listener)
        }
    }

    fun requestStopDownload(transId: Long) {
        if (fetcher.isValid) {
            fetcher.remove(transId);
        }
    }
}