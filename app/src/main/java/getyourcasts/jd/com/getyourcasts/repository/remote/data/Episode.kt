package getyourcasts.jd.com.getyourcasts.repository.remote.data

import android.database.Cursor
import getyourcasts.jd.com.getyourcasts.repository.local.EpisodeTable
import getyourcasts.jd.com.getyourcasts.repository.local.PodcastsTable
import getyourcasts.jd.com.getyourcasts.repository.local.getIntValue
import getyourcasts.jd.com.getyourcasts.repository.local.getStringValue

/**
 * Created by chuondao on 7/22/17.
 */
data class Episode (val podcastId: String,
                    val title: String,
                    val pubDate: String?,
                    val description: String?,
                    val downloadUrl : String?,
                    val localUrl: String?,
                    val fileSize : String?,
                    val type : String?,
                    val favorite: Int?,
                    val progress: Int?,
                    val downloaded: Int?
                    ) {


    companion object {
        // construct episode object from feed item so it can be inserted into DB
        fun fromFeedItem(feedItem:FeedItem, podcastId: String): Episode{
            return Episode(
                    podcastId,
                    feedItem.title,
                    feedItem.pubDate,
                    feedItem.description,
                    feedItem.mediaInfo?.url,
                    null, // no local url available
                    feedItem.mediaInfo?.size,
                    feedItem.mediaInfo?.type,
                    0 ,// initilize favorite to NOT
                    0,
                    0
            )
        }

        fun fromCursor(cursor: Cursor): Episode? {
            if (cursor.count > 0) {

                return Episode(
                        cursor.getStringValue(EpisodeTable.PODCAST_ID)!!,
                        cursor.getStringValue(EpisodeTable.EPISODE_NAME)!!,
                        cursor.getStringValue(EpisodeTable.DATE_RELEASED),
                        cursor.getStringValue(EpisodeTable.DESCRIPTION),
                        cursor.getStringValue(EpisodeTable.FETCH_URL),
                        cursor.getStringValue(EpisodeTable.LOCAL_URL),
                        cursor.getStringValue(EpisodeTable.FILE_SIZE),
                        cursor.getStringValue(EpisodeTable.MEDIA_TYPE),
                        cursor.getIntValue(EpisodeTable.FAVORITE),
                        cursor.getIntValue(EpisodeTable.PROGRESS),
                        cursor.getIntValue(EpisodeTable.DOWNLOADED)
                )
            }
            return null
        }
    }

}