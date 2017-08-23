package getyourcasts.jd.com.getyourcasts.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import getyourcasts.jd.com.getyourcasts.R;
import getyourcasts.jd.com.getyourcasts.exoplayer.MediaPlayBackService;

/**
 * Implementation of App Widget functionality.
 */
public class GetYourCastWidgetProvider extends AppWidgetProvider {
    private static final String WIDGET_ACTION_PROVIDER  = "widget_action_provider";
    private static int currentState = MediaPlayBackService.WIDGET_ACTION_PAUSE;
    private static final int WIDGET_REQ_CODE = 24;
    public static final String WIDGET_MEDIA_ACTION_KEY =  "widget_media_action_key";

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(),
                                                        R.layout.get_your_cast_widget_provider);
        Intent broadcastBack = new Intent(context, GetYourCastWidgetProvider.class);
        broadcastBack.setAction(WIDGET_ACTION_PROVIDER);

        views.setOnClickPendingIntent(R.id.widget_play_pause_btn,
                PendingIntent.getBroadcast(context, WIDGET_REQ_CODE,
                                                broadcastBack, PendingIntent.FLAG_UPDATE_CURRENT));
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action!= null && action.equals(WIDGET_ACTION_PROVIDER)){
            Intent mediaAction = getIntentForPlayBtnNextState(context);
            context.startService(mediaAction);
        }
        super.onReceive(context, intent);
    }

    private Intent getIntentForPlayBtnNextState(Context context){
        Intent intent = new Intent(context, MediaPlayBackService.class);
        switch (currentState) {
            case MediaPlayBackService.WIDGET_ACTION_PAUSE:
                currentState = MediaPlayBackService.WIDGET_ACTION_PLAY;
                intent.putExtra(WIDGET_MEDIA_ACTION_KEY, MediaPlayBackService.WIDGET_ACTION_PLAY);
                break;
            case MediaPlayBackService.WIDGET_ACTION_PLAY:
                currentState = MediaPlayBackService.WIDGET_ACTION_PAUSE;
                intent.putExtra(WIDGET_MEDIA_ACTION_KEY, MediaPlayBackService.WIDGET_ACTION_PAUSE);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return intent;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

