package getyourcasts.jd.com.getyourcasts.viewmodel;

/**
 * Created by chuondao on 8/10/17.
 */

public class EpisodeState{
    private String uniqueId;
    private int state;
    private long transId;

    public EpisodeState(String uniqueId, int state, long transId) {
        this.uniqueId = uniqueId;
        this.state = state;
        this.transId = transId;
    }

    public static final int FETCHED = 0;
    public static final int DOWNLOADING = 2;
    public static final int DOWNLOADED = 1;
    public static final int DELETED = 3;

    public String getUniqueId() {
        return uniqueId;
    }

    public int getState() {
        return state;
    }

    public long getTransId() {
        return transId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setTransId(long transId) {
        this.transId = transId;
    }
}
