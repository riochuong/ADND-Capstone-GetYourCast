package getyourcasts.jd.com.getyourcasts.view.adapter;

import android.util.Log;

import com.tonyodev.fetch.Fetch;
import com.tonyodev.fetch.listener.FetchListener;

/**
 * Created by chuondao on 9/10/17.
 */

public abstract class EpisodeDownloadListener implements FetchListener {
    private long transactionId;

    public EpisodeDownloadListener(long transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public void onUpdate(long id, int status, int progress, long downloadedBytes, long fileSize, int error) {
        // if this is the id that we are concerned
        if (id == transactionId) {
            switch (status) {
                case Fetch.STATUS_DOWNLOADING:
                    this.onProgressUpdate(progress);
                    break;

                case Fetch.STATUS_DONE:
                    this.onComplete();
                    break;

                case Fetch.STATUS_PAUSED:
                    this.onStop();
                    break;
            }
        } else if (error != Fetch.NO_ERROR){
            Log.e(EpisodesRecyclerViewAdapter.TAG, "Error happens ${error.toString()}");
            this.onError();
        }
    }

    public abstract void onProgressUpdate(int progress);

    public abstract void onComplete ();

    /* called when we stop the download in the middle !! */
    public abstract void onStop ();

    public abstract void onError();
}
