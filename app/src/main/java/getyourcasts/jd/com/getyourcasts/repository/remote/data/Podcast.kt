package getyourcasts.jd.com.getyourcasts.repository.remote.data

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import getyourcasts.jd.com.getyourcasts.repository.local.PodcastsTable
import getyourcasts.jd.com.getyourcasts.repository.local.getIntValue
import getyourcasts.jd.com.getyourcasts.repository.local.getStringValue

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
                   val trackCount: Long,
                   val description: String?) : Parcelable {

    // Parcelable to pass between activities
    companion object {

        @JvmField val CREATOR: Parcelable.Creator<Podcast> = object : Parcelable.Creator<Podcast> {
            override fun createFromParcel(source: Parcel): Podcast = Podcast(source)
            override fun newArray(size: Int): Array<Podcast?> = arrayOfNulls(size)
        }


        fun fromCursor(cursor: Cursor): Podcast? {
            if (cursor.count > 0) {
                val id = cursor.getStringValue(PodcastsTable.UNIQUE_ID)!!
                val podName = cursor.getStringValue(PodcastsTable.PODCAST_NAME)!!
                val artistName = cursor.getStringValue(PodcastsTable.ARTIST_NAME)
                val imglocal = cursor.getStringValue(PodcastsTable.IMG_LOCAL_PATH)
                val imgOnl = cursor.getStringValue(PodcastsTable.IMG_ONLINE_PATH)
                val releaseDate = cursor.getStringValue(PodcastsTable.RELEASE_DATE)
                val lastUpdate = cursor.getStringValue(PodcastsTable.LAST_UPDATE)
                val feedUrl = cursor.getStringValue(PodcastsTable.FEED_URL)!!
                val trackCount = cursor.getIntValue(PodcastsTable.TRACK_COUNT)!!.toLong()
                val desc = cursor.getStringValue(PodcastsTable.DESCRIPTION)

                return Podcast(
                        id, podName, artistName, imglocal, imgOnl, releaseDate, lastUpdate, feedUrl, trackCount, desc
                )
            }
            return null
        }

        /**
         * Observable cannot pass null around so stick this for empty
         */
        fun getEmptyPodcast(): Podcast {
            return Podcast("",
                    "", "",
                    "", "", "", "", "", 0,"")
        }


    }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            source.readLong(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(collectionId)
        dest.writeString(collectionName)
        dest.writeString(artistName)
        dest.writeString(imgLocalPath)
        dest.writeString(artworkUrl100)
        dest.writeString(releaseDate)
        dest.writeString(lastUpdate)
        dest.writeString(feedUrl)
        dest.writeLong(trackCount)
        dest.writeString(description)
    }
}