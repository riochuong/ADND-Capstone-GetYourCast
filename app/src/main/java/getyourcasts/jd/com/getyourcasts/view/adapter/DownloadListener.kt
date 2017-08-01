package getyourcasts.jd.com.getyourcasts.view.adapter

import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.tonyodev.fetch.Fetch
import com.tonyodev.fetch.listener.FetchListener
import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import io.reactivex.Observable

/**
 * Created by chuondao on 7/31/17.
 */
abstract class EpisodeDownloadListener(val transactionId: Long) : FetchListener {

    override fun onUpdate(id: Long, status: Int, progress: Int, downloadedBytes: Long, fileSize: Long, error: Int) {
        // if this is the id that we are concerned
        if (id == transactionId) {
            when (status) {
                Fetch.STATUS_DOWNLOADING -> {
                    this.onProgressUpdate(progress)
                }
                Fetch.STATUS_DONE ->{
                    this.onComplete()
                }
            }
        } else if (error != Fetch.NO_ERROR){
            Log.e(EpisodesRecyclerViewAdapter.TAG, "Error happens ${error.toString()}")
            this.onError()
        }
    }

    abstract fun onProgressUpdate(progress:Int)

    abstract fun onComplete ()

    abstract fun onError()

}