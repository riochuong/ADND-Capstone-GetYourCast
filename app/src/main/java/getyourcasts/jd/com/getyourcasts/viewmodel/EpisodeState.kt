package getyourcasts.jd.com.getyourcasts.viewmodel

/**
 *  class to help updating Episode State
 *  downloadLprogress should only be available when episode is
 *  DOWNLOADING otherwise -1 will be set
 */

class EpisodeState(var uniqueId: String?, var state: Int, val downloadProgress: Int) {
    companion object {
        const val EPISODE_FETCHED = 0
        const val EPISODE_DOWNLOADING = 2
        const val EPISODE_DOWNLOADED = 1
        const val EPISODE_DELETED = 3
    }
}
