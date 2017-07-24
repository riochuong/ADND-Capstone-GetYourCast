package getyourcasts.jd.com.getyourcasts.repository.remote.data

/**
 * Created by chuondao on 7/22/17.
 */
data class Episode (val podcastId: String,
                    val title: String,
                    val pubDate: String,
                    val description: String,
                    val downloadUrl : String,
                    val fileSize : Long,
                    val type : String,
                    val id: String)