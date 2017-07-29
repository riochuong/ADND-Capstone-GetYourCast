package getyourcasts.jd.com.getyourcasts.repository.remote.data

import android.database.Cursor
import getyourcasts.jd.com.getyourcasts.repository.local.*

/**
 * Created by chuondao on 7/22/17.
 */

data class Podcast(val collectionId: String,
                   val collectionName: String,
                   val artistName: String?,
                   val imgLocalPath: String?,
                   val artworkUrl100: String?,
                   val releaseDate: String?,
                   val lastUpdate: String?,
                   val feedUrl: String,
                   val trackCount: Long) {

    companion object {

        fun fromCursor (cursor: Cursor): Podcast? {
            if (cursor.count > 0){
                val id = cursor.getStringValue(PodcastsTable.UNIQUE_ID)!!
                val podName = cursor.getStringValue(PodcastsTable.PODCAST_NAME)!!
                val artistName = cursor.getStringValue(PodcastsTable.ARTIST_NAME)
                val imglocal = cursor.getStringValue(PodcastsTable.IMG_LOCAL_PATH)
                val imgOnl = cursor.getStringValue(PodcastsTable.IMG_ONLINE_PATH)
                val releaseDate = cursor.getStringValue(PodcastsTable.RELEASE_DATE)
                val lastUpdate = cursor.getStringValue(PodcastsTable.LAST_UPDATE)
                val feedUrl = cursor.getStringValue(PodcastsTable.FEED_URL)!!
                val trackCount =  cursor.getIntValue(PodcastsTable.TRACK_COUNT)!!.toLong()

                return Podcast(
                       id, podName,artistName,imglocal,imgOnl,releaseDate,lastUpdate,feedUrl,trackCount
                )
            }
            return null
        }

        /**
         * Observable cannot pass null around so stick this for empty
         */
        fun getEmptyPodcast():Podcast {
            return Podcast("",
                    "","",
                    "","","","","",0)
        }


    }


}