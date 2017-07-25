package getyourcasts.jd.com.getyourcasts.repository.remote.data

import org.jetbrains.anko.db.NOT_NULL
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



data class ItuneResponse(val results: List<Podcast>)


/*======================= RSS FEED URL RESPONSE PARSER =======================================*/

@Root(name = "rss", strict = false)
data class FeedResponse(
        @field:Element(name = "channel", required = true)
        @param:Element(name = "channel", required = true)
        val channel: Channel
)


@Root(name = "channel", strict = false)
@Convert(Channel.ChannelConverter::class)
data class Channel(
        val items: List<FeedItem> = ArrayList<FeedItem>(),
        val title: String,
        val channelDescription: String

) {
    /**
     *
     * custom converter for Channel and feed item
     */
    class ChannelConverter : Converter<Channel> {

        companion object {
            val NOR_AVAIL_STR = "N/A"
            val TITLE_STR = "title"
        }

        override fun write(node: OutputNode?, value: Channel?) {
            throw Throwable("Not Implemented")
        }

        override fun read(node: InputNode?): Channel {
            var title : String = NOR_AVAIL_STR
            var desc : String = NOR_AVAIL_STR
            val items : MutableList<FeedItem> = ArrayList<FeedItem>()

            try {
                if (node != null) {
                    var nextNode = node.getNext()
                    while (nextNode != null) {
                        val prefix = nextNode.prefix
                        val value = nextNode.value
                        when (nextNode.name) {
                            TITLE_STR -> {
                                if (prefix == null && value != null) {
                                    title = value
                                }
                            }
                        // parse
                            "description" -> {
                                if ( (prefix == null) && (nextNode.value != null) )  {
                                    desc = value
                                }
                            }
                            "item" -> {
                                items.add(parseFeedItem(nextNode))
                                nextNode = null
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
            return Channel(items,title, desc)

        }

        // parse FeedItem
        fun parseFeedItem(itemNode: InputNode): FeedItem {
            var nextNode = itemNode.getNext()
            var title: String  = NOR_AVAIL_STR
            var pubDate: String = NOR_AVAIL_STR
            var desc: String = NOR_AVAIL_STR
            var mediaInfo: MediaInfo? = null
            while (nextNode != null) {
                val prefix = nextNode.prefix
                val value = nextNode.value
                if (prefix == null) {
                    when (nextNode.name) {
                        TITLE_STR -> {
                            if (value != null) {title = value}
                        }
                        "pubDate" -> {
                            if (value != null) {pubDate = value}
                        }
                        "description" -> {
                            if (value != null) {desc = value}
                        }

                        "enclosure" ->{
                            if (mediaInfo == null && prefix == null){
                                val url = nextNode.getAttribute("url")
                                val size = nextNode.getAttribute("length")
                                val type = nextNode.getAttribute("type")
                                if (url != null && size != null && type != null){
                                    mediaInfo = MediaInfo(url.value,size.value,type.value)
                                }
                            }
                        }

                    }
                }
                nextNode = nextNode.getNext()

                if (nextNode == null){
                    nextNode = itemNode.getNext()
                }

            }
            return FeedItem(title, pubDate,desc, mediaInfo)

        }



    }
}



data class FeedItem(
        val title: String,
        val pubDate: String,
        val description: String,
        val mediaInfo: MediaInfo?

)

data class MediaInfo(
        val url: String,
        var size: String,
        val type: String
)