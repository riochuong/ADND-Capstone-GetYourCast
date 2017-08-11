package getyourcasts.jd.com.getyourcasts.repository.remote.data;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chuondao on 8/9/17.
 */

@Root(name = "channel", strict = false)
@Convert(ChannelConverter.class)
public class Channel implements Serializable {
    private List<FeedItem> items = new ArrayList<FeedItem>();
    private String title;
    private String channelDescription;



    public Channel(List<FeedItem> items, String title, String channelDescription) {
        this.items = items;
        this.title = title;
        this.channelDescription = channelDescription;
    }

    public Channel() {

    }

    public List<Episode> toListEpisodes(String podcastId){
        List<Episode> list = new ArrayList<>();
        for(FeedItem item : items){
            if (item.getMediaInfo() != null){
                list.add(Episode.fromFeedItem(item, podcastId));
            }
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



}
