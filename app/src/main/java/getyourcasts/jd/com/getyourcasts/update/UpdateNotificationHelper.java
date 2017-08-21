package getyourcasts.jd.com.getyourcasts.update;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import getyourcasts.jd.com.getyourcasts.R;
import getyourcasts.jd.com.getyourcasts.view.EpisodeInfoActivity;
import getyourcasts.jd.com.getyourcasts.view.UpdateListActivity;

/**
 * Created by chuondao on 8/17/17.
 */

public class UpdateNotificationHelper {

    private static final int UPDATE_EP_ID  = 937;
    private static final String EPS_UPDATE_KEY  = "eps_update_key";

    private static PendingIntent getPendingIntentToLaunchApp(Context context){
        Intent startActivity = new Intent(context, UpdateListActivity.class);
        return PendingIntent.getActivity(context,
                UPDATE_EP_ID,
                startActivity,
                PendingIntent.FLAG_UPDATE_CURRENT);

    }

    static void notifyNewUpdateAvailable(Context ctx) {
        NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(ctx)
                    .setContentTitle(ctx.getString(R.string.update_avail_str))
                    .setSmallIcon(R.mipmap.ic_update)
                    .setContentIntent(getPendingIntentToLaunchApp(ctx));
        NotificationManager notifManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notifManager != null){
            notifManager.notify(UPDATE_EP_ID, notiBuilder.build());
        }
    }
}
