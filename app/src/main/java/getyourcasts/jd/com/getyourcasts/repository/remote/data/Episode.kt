package getyourcasts.jd.com.getyourcasts.repository.remote.data

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable
import getyourcasts.jd.com.getyourcasts.repository.local.EpisodeTable
import getyourcasts.jd.com.getyourcasts.repository.local.getIntValue
import getyourcasts.jd.com.getyourcasts.repository.local.getStringValue

/**
 * Created by chuondao on 7/22/17.
 */
data class Episode(val podcastId: String,
                   val title: String,
                   val uniqueId: String,
                   val pubDate: String?,
                   val description: String?,
                   val downloadUrl: String?,
                   val localUrl: String?,
                   val fileSize: String?,
                   val type: String?,
                   val favorite: Int?,
                   val progress: Int?,
                   val downloaded: Int?
) : Parcelable {
    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Episode> = object : Parcelable.Creator<Episode> {
            override fun createFromParcel(source: Parcel): Episode = Episode(source)
            override fun newArray(size: Int): Array<Episode?> = arrayOfNulls(size)
        }


        // construct episode object from feed item so it can be inserted into DB
        fun fromFeedItem(feedItem: FeedItem, podcastId: String): Episode {
            return Episode(
                    podcastId,
                    feedItem.title.trim(),
                    "${podcastId}_${feedItem.title.trim()}".hashCode().toString(),
                    feedItem.pubDate,
                    feedItem.description,
                    feedItem.mediaInfo?.url,
                    null, // no local url available
                    feedItem.mediaInfo?.size,
                    feedItem.mediaInfo?.type,
                    0, // initilize favorite to NOT
                    0,
                    0
            )
        }

        fun fromCursor(cursor: Cursor): Episode? {
            if (cursor.count > 0) {

                val uniqueId =
                        "${cursor.getStringValue(EpisodeTable.PODCAST_ID)}_${cursor.getStringValue(EpisodeTable
                                .EPISODE_NAME)}".hashCode().toString()

                return Episode(
                        cursor.getStringValue(EpisodeTable.PODCAST_ID)!!,
                        cursor.getStringValue(EpisodeTable.EPISODE_NAME)!!,
                        uniqueId,
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

    fun getEpisodeUniqueKey(): String {
        return "${this.podcastId}_${this.title}".hashCode().toString()
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
            source.readString(),
            source.readValue(Int::class.java.classLoader) as Int?,
            source.readValue(Int::class.java.classLoader) as Int?,
            source.readValue(Int::class.java.classLoader) as Int?
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(podcastId)
        dest.writeString(title)
        dest.writeString(uniqueId)
        dest.writeString(pubDate)
        dest.writeString(description)
        dest.writeString(downloadUrl)
        dest.writeString(localUrl)
        dest.writeString(fileSize)
        dest.writeString(type)
        dest.writeValue(favorite)
        dest.writeValue(progress)
        dest.writeValue(downloaded)
    }
}