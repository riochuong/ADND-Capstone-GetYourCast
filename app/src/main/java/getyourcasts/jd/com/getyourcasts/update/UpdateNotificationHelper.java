package getyourcasts.jd.com.getyourcasts.update;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import getyourcasts.jd.com.getyourcasts.view.EpisodeInfoActivity;
import getyourcasts.jd.com.getyourcasts.view.UpdateListActivity;

/**
 * Created by chuondao on 8/17/17.
 */

public class UpdateNotificationHelper {

    private static final int UPDATE_EP_ID  = 937;
    public static final String EPS_UPDATE_KEY  = "eps_update_key";

    private static PendingIntent getPendingIntentToLaunchApp(Context context, UpdateInfo updateInfo){
        Intent startActivity = new Intent(context, UpdateListActivity.class);
        startActivity.putExtra(EPS_UPDATE_KEY, updateInfo);
        return PendingIntent.getActivity(context,
                UPDATE_EP_ID,
                startActivity,
                PendingIntent.FLAG_UPDATE_CURRENT);

    }

    public static void notifyNewUpdateAvailable() {

    }
}
