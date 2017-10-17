package getyourcasts.jd.com.getyourcasts.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.RemoteViews

import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition

import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.exoplayer.MediaPlayBackService
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp
import getyourcasts.jd.com.getyourcasts.view.media.MediaPlayerActivity
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel


/**
 * Implementation of App Widget functionality.
 */
class GetYourCastWidgetProvider : AppWidgetProvider() {



    /**
     * update remote views
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetId
     * @param changeImg
     */





    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != null && action == WIDGET_PLAY_ACTION_PROVIDER) {
            val mediaAction = getIntentForPlayBtnNextState(context, WIDGET_PRESS_TO_PLAY)
            context.startService(mediaAction)
        } else if (action != null && action == WIDGET_PAUSE_ACTION_PROVIDER){
            val mediaAction = getIntentForPlayBtnNextState(context, WIDGET_PRESS_TO_PAUSE)
            context.startService(mediaAction)
        } else if (action != null && action == WIDGET_NEXT_ACTION_PROVIDER) {
            val actionNext = getNextIntentBtn(context)
            context.startService(actionNext)
        } else if (action != null && action == WIDGET_PREVIOUS_ACTION_PROVIDER) {
            val actionPrev = getPreviousIntentBtn(context)
            context.startService(actionPrev)
        }
        super.onReceive(context, intent)
    }

    private fun getNextIntentBtn(context: Context): Intent {
        val intent = Intent(context, MediaPlayBackService::class.java)
        intent.putExtra(WIDGET_MEDIA_ACTION_KEY, MediaPlayBackService.WIDGET_ACTION_NEXT)
        return intent
    }

    private fun getPreviousIntentBtn(context: Context): Intent {
        val intent = Intent(context, MediaPlayBackService::class.java)
        intent.putExtra(WIDGET_MEDIA_ACTION_KEY, MediaPlayBackService.WIDGET_ACTION_PREV)
        return intent
    }

    private fun getIntentForPlayBtnNextState(context: Context, widgetCurrState: Int): Intent {
        val intent = Intent(context, MediaPlayBackService::class.java)
        when (widgetCurrState) {
            WIDGET_PRESS_TO_PLAY -> {
                intent.putExtra(WIDGET_MEDIA_ACTION_KEY, MediaPlayBackService.WIDGET_ACTION_PLAY)
                forceUpdateAppWidgets(context, null, widgetCurrState)
            }
            WIDGET_PRESS_TO_PAUSE -> {
                intent.putExtra(WIDGET_MEDIA_ACTION_KEY, MediaPlayBackService.WIDGET_ACTION_PAUSE)
                forceUpdateAppWidgets(context, null, widgetCurrState)
            }
            else -> throw UnsupportedOperationException()
        }
        return intent
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them

    }



    override fun onRestored(context: Context, oldWidgetIds: IntArray, newWidgetIds: IntArray) {
        super.onRestored(context, oldWidgetIds, newWidgetIds)
    }


    private fun forceUpdateAppWidgets(context: Context, imgSrc: String?, widgetCurrState: Int) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, GetYourCastWidgetProvider::class.java))
        for (id in ids) {
            updateAppWidget(context, appWidgetManager, imgSrc, widgetCurrState, id)
        }
    }


    companion object {
        val WIDGET_PRESS_TO_PLAY = 1
        val WIDGET_PRESS_TO_PAUSE = 2
        val WIDGET_EMPTY_PLAYLIST = 3
        /**
         * function to force update media widget
         */
        fun updateMediaWidget(context: Context,
                              appWidgetManager: AppWidgetManager,
                              appWidgetIds: IntArray,
                              imgRes: String, widgetCurrState: Int)
        {
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, imgRes, widgetCurrState, appWidgetId)
            }
        }

        /*force update app widget*/
        private fun updateAppWidget( context: Context,
                                     appWidgetManager: AppWidgetManager,
                                     widgetImgSrc: String?,
                                     widgetCurrState: Int,
                                     appWidgetId: Int) {
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName,
                    R.layout.get_your_cast_widget_provider)
            // set up play/pause onclick listener
            val broadcastBack = Intent(context, GetYourCastWidgetProvider::class.java)
            broadcastBack.action = WIDGET_PLAY_ACTION_PROVIDER
            views.setOnClickPendingIntent(R.id.widget_play_pause_btn,
                    PendingIntent.getBroadcast(context, WIDGET_REQ_CODE,
                            broadcastBack, PendingIntent.FLAG_UPDATE_CURRENT))
            updatePlayPauseBtn(views, widgetCurrState)
            // setup prev/next on click listener
            val broadcastNext = Intent(context, GetYourCastWidgetProvider::class.java)
            broadcastNext.action = WIDGET_NEXT_ACTION_PROVIDER
            val broadcastPrev = Intent(context, GetYourCastWidgetProvider::class.java)
            broadcastPrev.action = WIDGET_PREVIOUS_ACTION_PROVIDER
            views.setOnClickPendingIntent(R.id.widget_img,
                    PendingIntent.getActivity(context, WIDGET_REQ_CODE,
                            Intent(context, MediaPlayerActivity::class.java), PendingIntent.FLAG_CANCEL_CURRENT))

            views.setOnClickPendingIntent(R.id.widget_next_btn,
                    PendingIntent.getBroadcast(context, WIDGET_REQ_CODE,
                            broadcastNext, PendingIntent.FLAG_UPDATE_CURRENT))
            views.setOnClickPendingIntent(R.id.widget_prev_btn,
                    PendingIntent.getBroadcast(context, WIDGET_REQ_CODE,
                            broadcastPrev, PendingIntent.FLAG_UPDATE_CURRENT))

            // use glide to load image resource into imageview
            if (widgetImgSrc != null) {
                GlideApp.with(context)
                        .asBitmap()
                        .load(widgetImgSrc)
                        .into(object : SimpleTarget<Bitmap>() {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>) {
                                views.setImageViewBitmap(R.id.widget_img, resource)
                                appWidgetManager.updateAppWidget(appWidgetId, views)
                            }
                        })
            } else {
                // Instruct the widget manager to update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        private fun updatePlayPauseBtn(views: RemoteViews, widgetCurrState: Int) {
            when (widgetCurrState) {
                WIDGET_PRESS_TO_PAUSE -> views.setImageViewResource(R.id.widget_play_pause_btn, R.mipmap.ic_media_pause)
                WIDGET_EMPTY_PLAYLIST, WIDGET_PRESS_TO_PLAY -> {
                    views.setImageViewResource(R.id.widget_play_pause_btn, R.mipmap.ic_media_play)
                }
            }
        }
        internal val WIDGET_PLAY_ACTION_PROVIDER = "widget_play_action_provider"
        internal val WIDGET_PAUSE_ACTION_PROVIDER = "widget_pause_action_provider"
        private val WIDGET_NEXT_ACTION_PROVIDER = "widget_next_action_provider"
        private val WIDGET_PREVIOUS_ACTION_PROVIDER = "widget_prev_action_provider"
        private val WIDGET_REQ_CODE = 24
        val WIDGET_MEDIA_ACTION_KEY = "widget_media_action_key"
    }

}

