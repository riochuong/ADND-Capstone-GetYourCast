package getyourcasts.jd.com.getyourcasts.repository.remote.data;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.convert.Convert;

/**
 * Created by chuondao on 8/9/17.
 */


@Root(name = "rss", strict = false)
public class FeedResponse {

    public FeedResponse(Channel channel) {
        this.channel = channel;
    }

    private Channel channel;

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }
}

