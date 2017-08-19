package getyourcasts.jd.com.getyourcasts.update;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode;

/**
 * Created by chuondao on 8/17/17.
 */

public class UpdateInfo implements Parcelable {
    private Map<String, List<Episode>> updateList;

    public UpdateInfo(Map<String, List<Episode>> updateList) {
        this.updateList = updateList;
    }

    public Map<String, List<Episode>> getUpdateList() {
        return updateList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.updateList.size());
        for (Map.Entry<String, List<Episode>> entry : this.updateList.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeTypedList(entry.getValue());
        }
    }

    protected UpdateInfo(Parcel in) {
        int updateListSize = in.readInt();
        this.updateList = new HashMap<String, List<Episode>>(updateListSize);
        for (int i = 0; i < updateListSize; i++) {
            String key = in.readString();
            List<Episode> value = in.createTypedArrayList(Episode.CREATOR);
            this.updateList.put(key, value);
        }
    }

    public static final Parcelable.Creator<UpdateInfo> CREATOR = new Parcelable.Creator<UpdateInfo>() {
        @Override
        public UpdateInfo createFromParcel(Parcel source) {
            return new UpdateInfo(source);
        }

        @Override
        public UpdateInfo[] newArray(int size) {
            return new UpdateInfo[size];
        }
    };
}
