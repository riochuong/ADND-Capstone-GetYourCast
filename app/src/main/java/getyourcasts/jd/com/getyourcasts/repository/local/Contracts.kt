package getyourcasts.jd.com.getyourcasts.repository.local

/**
 * Created by chuondao on 7/21/17.
 */


object PodcastsTable {
    val ID = "_id"
    val NAME = "PodcastTable"
    val PODCAST_NAME="pod_cast_name"
    val LAST_UPDATE = "last_update"
    val UNIQUE_ID = "unique_id" // use it to link with episode table
    val IMG_PATH = "img_path"
    val FEED_URL = "feed_url" // url used for fetching update episodes
}

object EpisodeTable {
    val ID = "_id"
    val POD_UNIQUE_ID = "pod_unique_id"
    val NAME = "EpisodeTable"
    val EPISODE_NAME = "episode_name"
    val DATE_RELEASED = "date_released"
    val DATE_DOWNLOADED = "date_downloaded"
    val CONSUME_PERC = "consume_percentage"
    val DESCRIPTION = "description"
    val FETCH_URL = "fetch_url"
    val FILE_SIZE = "file_size"
    val MEDIA_TYPe = "media_type"
}