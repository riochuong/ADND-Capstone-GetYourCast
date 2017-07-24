package getyourcasts.jd.com.getyourcasts.repository.remote.data

import org.jetbrains.annotations.PropertyKey
import org.simpleframework.xml.*

/**
 * Created by chuondao on 7/22/17.
 */

/* =============== ITUNE RESPONSE ====================================== */
data class EpisodeResponse (val podcastId: String,
                    val name: String,
                    val id: String)

data class Podcast(val collectionId: String,
                   val collectionName: String,
                   val artistName : String,
                   val artworkUrl100: String,
                   val releaseDate : String,
                   val feedUrl : String,
                   val trackCount: Long)

data class ItuneResponse (val results: List<Podcast>)



/*======================= RSS FEED PARSER =======================================*/

@Root(name="rss", strict = false)
data class FeedResponse   (
        @field:Element(name="channel", required =true)
        @param:Element(name="channel", required =true)
        val channel : Channel
)

@Root(name="channel", strict=false)
data class Channel (

    @field: ElementList(inline = true, name="item")
    @param:ElementList(inline = true, name="item")
    val items: List<FeedItem>,

    @field:Element(name="title")
    @param:Element(name="title")
    val title : String

//    //@field:Path(value="channel")
//    @field:Element(name="description", required = false)
//    @param:Element(name="description", required = false)
//    val channelDescription : String

//    @Element(name="itunes:subtitle")
//    val subtitle : String
)

@Root(name="item", strict = false)
data class FeedItem  (

   @field:Element(name="title")
   @param:Element(name="title")
   val title: String,

   @field:Element(name="pubDate")
   @param:Element(name="pubDate")
   val pubData:  String,


//   @field:Element(name="description",required = false)
//   @param:Element(name="description",required = false)
//   val description: String,

   @field:Element(name="enclosure")
   @param:Element(name="enclosure")
   val mediaInfo : MediaInfo

)

@Root(name="enclosure", strict=false)
data class MediaInfo  (
    @field:Attribute(name="url")
    @param:Attribute(name="url")
    val url: String,

    @field:Attribute(name="length")
    @param:Attribute(name="length")
    val size: String,

    @field:Attribute(name="type")
    @param:Attribute(name="type")
    val type: String
)