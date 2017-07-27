package getyourcasts.jd.com.getyourcasts.repository.remote.data

/**
 * Created by chuondao on 7/22/17.
 */
data class Episode (val podcastId: String,
                    val title: String,
                    val pubDate: String,
                    val description: String,
                    val downloadUrl : String?,
                    val localUrl: String?,
                    val fileSize : String?,
                    val type : String?
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
                    feedItem.mediaInfo?.type
            )
        }
    }

}