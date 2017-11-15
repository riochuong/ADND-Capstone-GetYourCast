package getyourcasts.jd.com.getyourcasts.view.adapter

import com.androidnetworking.interfaces.DownloadProgressListener
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.viewmodel.EpisodeState
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel

/**
 * Created by chuondao on 11/14/17.
 */
class MyDownloadProgressListener (val episode : Episode) : DownloadProgressListener {
    private var updateCount = 0

    companion object {
        const val MIN_UPDATE = 10
    }
    override fun onProgress(bytesDownloaded: Long, totalBytes: Long) {
        val percProg = (bytesDownloaded.toDouble() / totalBytes.toDouble()) * 100.0
        if (percProg > updateCount * MIN_UPDATE){
            PodcastViewModel.updateEpisodeSubject(
                    EpisodeState(episode.uniqueId, EpisodeState.EPISODE_DOWNLOADING, percProg.toInt()))
            updateCount++
        }
    }

}