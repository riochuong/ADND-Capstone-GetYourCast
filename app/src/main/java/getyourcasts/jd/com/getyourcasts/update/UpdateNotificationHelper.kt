package getyourcasts.jd.com.getyourcasts.update

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat

import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.view.EpisodeInfoActivity
import getyourcasts.jd.com.getyourcasts.view.UpdateListActivity

/**
 * Created by chuondao on 8/17/17.
 */

object UpdateNotificationHelper {

    private val UPDATE_EP_ID = 937

    private fun getPendingIntentToLaunchApp(context: Context): PendingIntent {
        val startActivity = Intent(context, UpdateListActivity::class.java)
        return PendingIntent.getActivity(context,
                UPDATE_EP_ID,
                startActivity,
                PendingIntent.FLAG_UPDATE_CURRENT)

    }

    internal fun notifyNewUpdateAvailable(ctx: Context) {
        val notiBuilder = NotificationCompat.Builder(ctx)
                .setContentTitle(ctx.getString(R.string.update_avail_str))
                .setSmallIcon(R.mipmap.ic_update)
                .setContentIntent(getPendingIntentToLaunchApp(ctx))
        val notifManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifManager?.notify(UPDATE_EP_ID, notiBuilder.build())
    }
}
