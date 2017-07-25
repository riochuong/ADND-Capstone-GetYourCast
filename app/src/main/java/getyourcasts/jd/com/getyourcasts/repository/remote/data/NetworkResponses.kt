package getyourcasts.jd.com.getyourcasts.repository.remote.data

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import org.simpleframework.xml.convert.Convert
import org.simpleframework.xml.convert.Converter
import org.simpleframework.xml.stream.InputNode
import org.simpleframework.xml.stream.OutputNode

/**
 * Created by chuondao on 7/22/17.
 */

/* =============== ITUNE RESPONSE ====================================== */
data class EpisodeResponse(val podcastId: String,
                           val name: String,
                           val id: String)

data class Podcast(val collectionId: String,
                   val collectionName: String,
                   val artistName: String,
                   val artworkUrl100: String,
                   val releaseDate: String,
                   val feedUrl: String,
                   val trackCount: Long)

data class ItuneResponse(val results: List<Podcast>)


///*======================= RSS FEED PARSER =======================================*/
//
@Root(name = "rss", strict = false)
data class FeedResponse(
        @field:Element(name = "channel", required = true)
        @param:Element(name = "channel", required = true)
        val channel: Channel
)


@Root(name = "channel", strict = false)
@Convert(Channel.ChannelConverter::class)
data class Channel(

        @field: ElementList(inline = true, name = "item")
        @param: ElementList(inline = true, name = "item")
        var items: MutableList<FeedItem> = ArrayList<FeedItem>(),

        @field:Element(name = "title")
        @param:Element(name = "title")
        var title: String? = null,

        @field:Element(name = "description")
        @param:Element(name = "description")
        var channelDescription: String? = null

) {
    /**
     *
     * custom converter for Channel
     */
    class ChannelConverter : Converter<Channel> {
        override fun write(node: OutputNode?, value: Channel?) {
            throw UnsupportedOperationException("Not Implemented")
        }

        override fun read(node: InputNode?): Channel {
            var channel: Channel = Channel()
            try {
                if (node != null) {
                    var nextNode = node.getNext()
                    while (nextNode != null) {
                        val prefix = nextNode.prefix
                        when (nextNode.name) {
                            "title" -> {
                                if (prefix == null) {
                                    channel.title = nextNode.value
                                }
                            }
                        // parse
                            "description" -> {
                                if (prefix == null) {
                                    channel.channelDescription = nextNode.value
                                }
                            }
                            "item" -> {
                                channel.items.add(parseFeedItem(nextNode))
                            }
                        }
                        if (nextNode != null){
                            nextNode = nextNode.getNext()
                        }
                        if (nextNode == null) {
                            nextNode = node.getNext()
                        }
                    }
                }
            } catch(e: Exception) {
                e.printStackTrace()
            }
            return channel

        }

        // parse FeedItem
        fun parseFeedItem(itemNode: InputNode): FeedItem {
            var feedItem = FeedItem()
            var nextNode = itemNode.getNext()
            while (nextNode != null) {
                val prefix = nextNode.prefix
                if (prefix == null) {
                    when (nextNode.name) {
                        "title" -> {
                            feedItem.title = nextNode.value
                        }
                        "pubDate" -> {
                            feedItem.pubDate = nextNode.value
                        }
                        "description" -> {
                            feedItem.description = nextNode.value
                        }

                        "enclosure" ->{
                            if (feedItem.mediaInfo == null){
                                val url = nextNode.getAttribute("url").value
                                val size = nextNode.getAttribute("length").value
                                val type = nextNode.getAttribute("type").value
                                feedItem.mediaInfo = MediaInfo(url,size,type)
                            }
                        }

                    }
                }
                if (nextNode != null ){
                    nextNode = nextNode.getNext()
                }
                if (nextNode == null){
                    nextNode = itemNode.getNext()
                }

            }
            return feedItem

        }



    }
}


@Root(name = "item", strict = false)
data class FeedItem(

        @field:Element(name = "title")
        @param:Element(name = "title")
        var title: String? = null,

        @field:Element(name = "pubDate")
        @param:Element(name = "pubDate")
        var pubDate: String? = null,


        @field:Element(name = "description", required = false)
        @param:Element(name = "description", required = false)
        var description: String? = null,

        @field:Element(name = "enclosure")
        @param:Element(name = "enclosure")
        var mediaInfo: MediaInfo? = null

)

@Root(name = "enclosure", strict = false)
data class MediaInfo(
        @field:Attribute(name = "url")
        @param:Attribute(name = "url")
        var url: String? = null,

        @field:Attribute(name = "length")
        @param:Attribute(name = "length")
        var size: String? = null,

        @field:Attribute(name = "type")
        @param:Attribute(name = "type")
        val type: String? = null
)