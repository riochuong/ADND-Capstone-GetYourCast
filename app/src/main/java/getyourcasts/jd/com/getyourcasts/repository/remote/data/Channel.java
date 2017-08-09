package getyourcasts.jd.com.getyourcasts.repository.remote.data;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chuondao on 8/9/17.
 */

@Root(name = "channel", strict = false)
@Convert(Channel.ChannelConverter.class)
public class Channel {
    private List<FeedItem> items = new ArrayList<FeedItem>();
    private String title;
    private String channelDescription;

    private static final String NOT_AVAIL_STR = "N/A";
    private static final String TITLE_STR = "title";
    private static final String DESCRIPTION_STR = "description";
    private static final String ITEM_STR = "item";
    private static final String PUB_DATE_STR = "pubDate";
    private static final String ENCLOSURE_STR = "enclosure";


    public Channel(List<FeedItem> items, String title, String channelDescription) {
        this.items = items;
        this.title = title;
        this.channelDescription = channelDescription;
    }

    public List<Episode> toListEpisodes(String podcastId){
        List<Episode> list = new ArrayList<>();
        for(FeedItem item : items){
            list.add(Episode.fromFeedItem(item, podcastId));
        }
        return list;
    }

    public List<FeedItem> getItems() {
        return items;
    }

    public String getTitle() {
        return title;
    }

    public String getChannelDescription() {
        return channelDescription;
    }

    public void setItems(List<FeedItem> items) {
        this.items = items;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setChannelDescription(String channelDescription) {
        this.channelDescription = channelDescription;
    }

    class ChannelConverter implements Converter<Channel> {

        @Override
        public Channel read(InputNode node) throws Exception {
            String title = NOT_AVAIL_STR;
            String desc = NOT_AVAIL_STR;
            List<FeedItem> items = new ArrayList<>();

            try {
                if (node != null) {
                    InputNode nextNode = node.getNext();
                    while (nextNode != null) {
                        String prefix = nextNode.getPrefix();
                        String value = nextNode.getValue();
                        switch (nextNode.getName()) {
                            case TITLE_STR:
                                if (prefix == null && value != null) {
                                    title = value;
                                }
                                break;
                            case DESCRIPTION_STR:
                                if ((prefix == null) && (value != null)) {
                                    desc = value;
                                }
                                break;
                            case ITEM_STR:
                                items.add(parseFeedItem(nextNode));
                                nextNode = null;
                                break;
                        }
                        if (nextNode != null) {
                            nextNode = nextNode.getNext();
                        }
                        if (nextNode == null) {
                            nextNode = node.getNext();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new Channel(items, title, desc);

        }


        private FeedItem parseFeedItem(InputNode itemNode) {

            String title = NOT_AVAIL_STR;
            String pubDate = NOT_AVAIL_STR;
            String desc = NOT_AVAIL_STR;
            MediaInfo mediaInfo = null;
            try {
                InputNode nextNode = itemNode.getNext();
                while (nextNode != null) {
                    String prefix = nextNode.getPrefix();
                    String value = nextNode.getValue();
                    if (prefix == null) {
                        switch (nextNode.getName()) {
                            case TITLE_STR:
                                if (value != null) {
                                    title = value;
                                }
                                break;

                            case PUB_DATE_STR:
                                if (value != null) {
                                    pubDate = value;
                                }
                                break;
                            case DESCRIPTION_STR:
                                if (value != null) {
                                    desc = value;
                                }
                                break;
                            case ENCLOSURE_STR:
                                if (mediaInfo == null && prefix == null) {
                                    InputNode url = nextNode.getAttribute("url");
                                    InputNode size = nextNode.getAttribute("length");
                                    InputNode type = nextNode.getAttribute("type");
                                    if (url != null && size != null && type != null) {
                                        mediaInfo = new MediaInfo(url.getValue(), size.getValue(), type.getValue());
                                    }
                                }
                        }

                    }

                    // try to get next element
                    nextNode = nextNode.getNext();

                    // check for any other items to read
                    if (nextNode == null) {
                        nextNode = itemNode.getNext();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return new FeedItem(title, pubDate, desc, mediaInfo);
        }

        @Override
        public void write(OutputNode node, Channel value) throws Exception {
            try {
                throw new Throwable("Not Implemented");
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

        }
    }


    class FeedItem {
        public FeedItem(String title, String pubDate, String description, MediaInfo mediaInfo) {
            this.title = title;
            this.pubDate = pubDate;
            this.description = description;
            this.mediaInfo = mediaInfo;
        }

        private String title;
        private String pubDate;
        private String description;
        private MediaInfo mediaInfo;

        public String getTitle() {
            return title;
        }

        public String getPubDate() {
            return pubDate;
        }

        public String getDescription() {
            return description;
        }

        public MediaInfo getMediaInfo() {
            return mediaInfo;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setPubDate(String pubDate) {
            this.pubDate = pubDate;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setMediaInfo(MediaInfo mediaInfo) {
            this.mediaInfo = mediaInfo;
        }
    }

    class MediaInfo {
        private String url;
        private String size;
        private String type;

        public MediaInfo(String url, String size, String type) {
            this.url = url;
            this.size = size;
            this.type = type;
        }

        public String getUrl() {
            return url;
        }

        public String getSize() {
            return size;
        }

        public String getType() {
            return type;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

}
