package getyourcasts.jd.com.getyourcasts.repository.remote.data

/**
 * Created by chuondao on 7/22/17.
 */

data class EpisodeResponse (val podcastId: String,
                    val name: String,
                    val id: String)

data class Podcast(val collectionId: String,
                   val collectionName: String,
                   val artistName : String,
                   val artworkUrl100: String,
                   val releaseDate : String,
                   val trackCount: Long)

data class ItuneResponse (val results: List<Podcast>)