package getyourcasts.jd.com.getyourcasts.repository.remote.network

import android.app.IntentService
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.tonyodev.fetch.Fetch
import com.tonyodev.fetch.listener.FetchListener
import com.tonyodev.fetch.request.Request

/**
 * Created by chuondao on 7/30/17.
 */

class DownloadService : Service() {

    private var binder: IBinder = DownloadServiceBinder()
    private lateinit var fetcher: Fetch
    private var listReqIds: MutableMap<Long,Long> = HashMap<Long,Long>()

    companion object {
        val TAG = DownloadService.javaClass.simpleName
    }


    override fun onBind(intent: Intent?): IBinder {
        fetcher = Fetch.newInstance(this)
        listReqIds  = HashMap<Long,Long>()
        return binder
    }

    override fun onRebind(intent: Intent?) {
        if (! fetcher.isValid){
            fetcher = Fetch.newInstance(this)
            listReqIds  = HashMap<Long,Long>()
        }
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        fetcher.release()
        return super.onUnbind(intent)
    }

    inner class DownloadServiceBinder: Binder(){
          fun getService() : DownloadService {
              return this@DownloadService
          }
    }

    /**
     * the request download link
     * will be enqued and the id for the request will be returned
     * @return: -1 if failed to enqueue otherwise valid Id will be returned
     */
    fun requestDownLoad (url: String, dirPath: String, filename: String): Long{
        if (fetcher.isValid){
            val req = Request(url, dirPath, filename)
            val id = fetcher.enqueue(req)
            listReqIds.put(id,id)
        }

        return -1
    }

    fun registerListener (listener: FetchListener){
        if (fetcher.isValid){
            fetcher.addFetchListener(listener)
        }

    }
}