package getyourcasts.jd.com.getyourcasts.repository.remote.data;

/**
 * Created by chuondao on 8/10/17.
 */

public class FeedItem {
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
