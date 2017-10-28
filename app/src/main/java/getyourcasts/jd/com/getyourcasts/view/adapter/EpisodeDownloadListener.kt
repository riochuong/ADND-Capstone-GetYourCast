package getyourcasts.jd.com.getyourcasts.view.adapter

import android.util.Log

import com.tonyodev.fetch.Fetch
import com.tonyodev.fetch.listener.FetchListener

/**
 * Created by chuondao on 9/10/17.
 */

abstract class EpisodeDownloadListener(private val transactionId: Long) : FetchListener {

    override fun onUpdate(id: Long, status: Int, progress: Int, downloadedBytes: Long, fileSize: Long, error: Int) {
        // if this is the id that we are concerned
        if (id == transactionId) {
            when (status) {
                Fetch.STATUS_DOWNLOADING -> this.onProgressUpdate(progress)

                Fetch.STATUS_DONE -> this.onComplete()

                Fetch.STATUS_PAUSED -> this.onStop()

                Fetch.STATUS_ERROR -> {
                    Log.e(EpisodesRecyclerViewAdapter.TAG, "Error happen ${error.toString()}")
                    this.onError()
                }
            }
        }
    }

    abstract fun onProgressUpdate(progress: Int)

    abstract fun onComplete()

    /* called when we stop the download in the middle !! */
    abstract fun onStop()

    abstract fun onError()
}
