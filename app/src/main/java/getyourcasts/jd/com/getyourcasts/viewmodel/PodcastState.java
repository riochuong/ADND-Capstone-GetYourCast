package getyourcasts.jd.com.getyourcasts.viewmodel;
//
///**
// * Created by chuondao on 8/10/17.
// */
//
 /* class to deliver sync state between different item related to epidsode */
public class PodcastState{

    private String uniqueId;
    private int state;

    public PodcastState(String uniqueId, int state) {
        this.uniqueId = uniqueId;
        this.state = state;
    }

    public static int UNSUBSCRIBED = 0;
    public static int SUBSCRIBED = 1;

    public String getUniqueId() {
        return uniqueId;
    }

    public int getState() {
        return state;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public void setState(int state) {
        this.state = state;
    }
}