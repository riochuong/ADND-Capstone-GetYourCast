package getyourcasts.jd.com.getyourcasts.repository.remote.data;

import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chuondao on 8/10/17.
 */

public class ChannelConverter implements Converter<Channel> {

    private static final String NOT_AVAIL_STR = "N/A";
    private static final String TITLE_STR = "title";
    private static final String DESCRIPTION_STR = "description";
    private static final String ITEM_STR = "item";
    private static final String PUB_DATE_STR = "pubDate";
    private static final String ENCLOSURE_STR = "enclosure";


    public ChannelConverter() {
    }

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
