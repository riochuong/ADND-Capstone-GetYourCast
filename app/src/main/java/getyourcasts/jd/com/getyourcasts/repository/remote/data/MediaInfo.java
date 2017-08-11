package getyourcasts.jd.com.getyourcasts.repository.remote.data;

/**
 * Created by chuondao on 8/10/17.
 */

public class MediaInfo {
    private String url;
    private String size;
    private String type;

    public MediaInfo(String url, String size, String type) {
        this.url = url;
        this.size = size;
        this.type = type;
    }

    public MediaInfo() {
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