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
    val TRACK_COUNT = "track_count"
}

object EpisodeTable {
    val ID = "_id"
    val NAME = "EpisodeTable"
    val PODCAST_ID = "podcast_id"
    val EPISODE_NAME = "episode_name"
    val DATE_RELEASED = "date_released"
    val DATE_DOWNLOADED = "date_downloaded"
    val PROGRESS = "consume_percentage"
    val DESCRIPTION = "description"
    val LOCAL_URL = "local_url"
    val FETCH_URL = "fetch_url"
    val FILE_SIZE = "file_size"
    val DOWNLOADED = "downloaded"
    val MEDIA_TYPE = "media_type"
}