package getyourcasts.jd.com.getyourcasts.repository.remote.data

/**
 * Created by chuondao on 7/22/17.
 */

data class Podcast(val collectionId: String,
                   val collectionName: String,
                   val artistName: String,
                   val artworkUrl100: String,
                   val releaseDate: String,
                   val feedUrl: String,
                   val trackCount: Long)