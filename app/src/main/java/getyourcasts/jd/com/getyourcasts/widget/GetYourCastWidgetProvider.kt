package getyourcasts.jd.com.getyourcasts.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import android.widget.RemoteViews
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp
import getyourcasts.jd.com.getyourcasts.exoplayer.MediaPlayBackService
import getyourcasts.jd.com.getyourcasts.view.MainPodcastActivity
import getyourcasts.jd.com.getyourcasts.view.media.MediaPlayerActivity


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
        resolveButtonState(context, action, true, null)
        super.onReceive(context, intent)
    }




    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them

    }
    override fun onRestored(context: Context, oldWidgetIds: IntArray, newWidgetIds: IntArray) {
        super.onRestored(context, oldWidgetIds, newWidgetIds)
    }



    companion object {
        val WIDGET_PRESS_TO_PLAY = 1
        val WIDGET_PRESS_TO_PAUSE = 2
        val WIDGET_EMPTY_PLAYLIST = 3

        /**
         * this will be called when button is clicked
         */
        fun resolveButtonState (context: Context,
                                        action: String,
                                        activeCick: Boolean,
                                        imgRsc: String?) {
            var mediaAction : Intent? = null
            when (action) {
                WIDGET_PLAY_ACTION_PROVIDER -> {
                    mediaAction = getIntentForPlayBtnNextState(context, WIDGET_PRESS_TO_PAUSE)
                    forceUpdateAppWidgets(context, imgRsc, WIDGET_PRESS_TO_PAUSE)
                }
                WIDGET_PAUSE_ACTION_PROVIDER -> {
                    mediaAction = getIntentForPlayBtnNextState(context, WIDGET_PRESS_TO_PLAY)
                    forceUpdateAppWidgets(context, imgRsc, WIDGET_PRESS_TO_PLAY)
                }
                WIDGET_NEXT_ACTION_PROVIDER -> {
                    mediaAction = getNextIntentBtn(context)
                    forceUpdateAppWidgets(context , imgRsc , WIDGET_PRESS_TO_PAUSE)
                }
                WIDGET_PREVIOUS_ACTION_PROVIDER -> {
                    mediaAction = getPreviousIntentBtn(context)
                    forceUpdateAppWidgets(context , imgRsc , WIDGET_PRESS_TO_PAUSE)
                }
                WIDGET_EMPTY_PLAYLIST_ACTION_PROVIDER -> {
                    forceUpdateAppWidgets(context, null, WIDGET_EMPTY_PLAYLIST)
                }
            }
            // if user touch the button directly on the screen
            if (activeCick && mediaAction != null) {
                context.startService(mediaAction)
            }
        }

        private fun forceUpdateAppWidgets(context: Context, imgSrc: String?, widgetCurrState: Int) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, GetYourCastWidgetProvider::class.java))
            for (id in ids) {
                updateAppWidget(context, appWidgetManager, imgSrc, widgetCurrState, id)
            }
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
                }
                WIDGET_PRESS_TO_PAUSE -> {
                    intent.putExtra(WIDGET_MEDIA_ACTION_KEY, MediaPlayBackService.WIDGET_ACTION_PAUSE)
                }
                else -> throw UnsupportedOperationException()
            }
            return intent
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
            // check if widgetState is empty
            if (widgetCurrState == WIDGET_EMPTY_PLAYLIST) {
                // show empty view
                views.setViewVisibility(R.id.widget_playback_control, View.GONE)
                views.setViewVisibility(R.id.widget_empty_view, View.VISIBLE)
                views.setOnClickPendingIntent(R.id.widget_empty_view,
                        PendingIntent.getActivity(context,
                                WIDGET_REQ_CODE,
                                Intent(context, MainPodcastActivity::class.java),
                                PendingIntent.FLAG_UPDATE_CURRENT
                        ));
            }
            else {
                // set up play/pause onclick listener
                views.setViewVisibility(R.id.widget_empty_view, View.GONE)
                views.setViewVisibility(R.id.widget_playback_control, View.VISIBLE)
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
            }


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
                WIDGET_PRESS_TO_PLAY -> {
                    views.setImageViewResource(R.id.widget_play_pause_btn, R.mipmap.ic_media_play)
                }
            }
        }
        val WIDGET_PLAY_ACTION_PROVIDER = "widget_play_action_provider"
        val WIDGET_PAUSE_ACTION_PROVIDER = "widget_pause_action_provider"
        val WIDGET_NEXT_ACTION_PROVIDER = "widget_next_action_provider"
        val WIDGET_PREVIOUS_ACTION_PROVIDER = "widget_prev_action_provider"
        val WIDGET_EMPTY_PLAYLIST_ACTION_PROVIDER = "widget_empty_action_provider"
        private val WIDGET_REQ_CODE = 24
        val WIDGET_MEDIA_ACTION_KEY = "widget_media_action_key"
    }

}

