package getyourcasts.jd.com.getyourcasts.repository.remote.data

import android.database.Cursor
import getyourcasts.jd.com.getyourcasts.repository.local.PodcastsTable
import getyourcasts.jd.com.getyourcasts.repository.local.getInt
import getyourcasts.jd.com.getyourcasts.repository.local.getString

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
                return Podcast(
                        cursor.getString(PodcastsTable.UNIQUE_ID)!!,
                        cursor.getString(PodcastsTable.NAME)!!,
                        cursor.getString(PodcastsTable.ARTIST_NAME),
                        cursor.getString(PodcastsTable.IMG_LOCAL_PATH),
                        cursor.getString(PodcastsTable.IMG_ONLINE_PATH),
                        cursor.getString(PodcastsTable.RELEASE_DATE),
                        cursor.getString(PodcastsTable.LAST_UPDATE),
                        cursor.getString(PodcastsTable.FEED_URL)!!,
                        cursor.getInt(PodcastsTable.TRACK_COUNT)!!.toLong()
                )
            }
            return null
        }

    }


}