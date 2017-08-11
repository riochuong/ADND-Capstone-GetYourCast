package getyourcasts.jd.com.getyourcasts.repository.remote.data;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;

import java.io.Serializable;

/**
 * Created by chuondao on 8/9/17.
 */


@Root(name = "rss", strict = false)
public final class FeedResponse implements Serializable {

    public FeedResponse() {
        this.channel = null;
    }

    public FeedResponse(Channel channel) {
        this.channel = channel;
    }

    @Element(name="channel")
    private Channel channel;

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }
}

