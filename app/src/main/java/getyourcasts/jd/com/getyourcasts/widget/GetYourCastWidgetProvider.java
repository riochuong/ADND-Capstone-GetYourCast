package getyourcasts.jd.com.getyourcasts.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Pair;
import android.widget.RemoteViews;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import getyourcasts.jd.com.getyourcasts.R;
import getyourcasts.jd.com.getyourcasts.exoplayer.MediaPlayBackService;
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast;
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp;
import getyourcasts.jd.com.getyourcasts.view.media.MediaPlayerActivity;
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Implementation of App Widget functionality.
 */
public class GetYourCastWidgetProvider extends AppWidgetProvider {

    private static final int WIDGET_PRESS_TO_PLAY = 1;
    private static final int WIDGET_PRESS_TO_PAUSE = 2;
    private static final int WIDGET_EMPTY_PLAYLIST = 3;

    private static final String WIDGET_PLAY_PAUSE_ACTION_PROVIDER = "widget_play_pause_action_provider";
    private static final String WIDGET_NEXT_ACTION_PROVIDER = "widget_next_action_provider";
    private static final String WIDGET_PREVIOUS_ACTION_PROVIDER = "widget_prev_action_provider";
    private static int widgetCurrState = WIDGET_PRESS_TO_PLAY;
    private static String widgetImgSrc = null;
    private static final int WIDGET_REQ_CODE = 24;
    public static final String WIDGET_MEDIA_ACTION_KEY =  "widget_media_action_key";
    private Disposable disposable;
    private PodcastViewModel viewModel;



    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                         int appWidgetId, boolean changeImg) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(),
                                                        R.layout.get_your_cast_widget_provider);
        // set up play/pause onclick listener
        Intent broadcastBack = new Intent(context, GetYourCastWidgetProvider.class);
        broadcastBack.setAction(WIDGET_PLAY_PAUSE_ACTION_PROVIDER);
        views.setOnClickPendingIntent(R.id.widget_play_pause_btn,
                PendingIntent.getBroadcast(context, WIDGET_REQ_CODE,
                                                broadcastBack, PendingIntent.FLAG_UPDATE_CURRENT));
        updatePlayPauseBtn(views);

        // setup prev/next on click listener
        Intent broadcastNext = new Intent(context, GetYourCastWidgetProvider.class);
        broadcastNext.setAction(WIDGET_NEXT_ACTION_PROVIDER);
        Intent broadcastPrev = new Intent(context, GetYourCastWidgetProvider.class);
        broadcastPrev.setAction(WIDGET_PREVIOUS_ACTION_PROVIDER);
        views.setOnClickPendingIntent(R.id.widget_img,
                PendingIntent.getActivity(context,WIDGET_REQ_CODE,
                        new Intent(context, MediaPlayerActivity.class)
                        ,PendingIntent.FLAG_CANCEL_CURRENT));

        views.setOnClickPendingIntent(R.id.widget_next_btn,
                PendingIntent.getBroadcast(context, WIDGET_REQ_CODE,
                        broadcastNext, PendingIntent.FLAG_UPDATE_CURRENT));
        views.setOnClickPendingIntent(R.id.widget_prev_btn,
                PendingIntent.getBroadcast(context, WIDGET_REQ_CODE,
                        broadcastPrev, PendingIntent.FLAG_UPDATE_CURRENT));

        // use glide to load image resource into imageview
        if (widgetImgSrc != null && changeImg){
            GlideApp.with(context)
                     .asBitmap()
                    .load(widgetImgSrc)
                     .into(new SimpleTarget<Bitmap>() {
                         @Override
                         public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                             views.setImageViewBitmap(R.id.widget_img, resource);
                             appWidgetManager.updateAppWidget(appWidgetId, views);
                         }
                     });
        } else{
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private void updatePlayPauseBtn(RemoteViews views) {
         switch(widgetCurrState) {
             case WIDGET_PRESS_TO_PAUSE:
                 views.setImageViewResource(R.id.widget_play_pause_btn, R.mipmap.ic_media_pause);
                 break;
             case WIDGET_EMPTY_PLAYLIST:
             case WIDGET_PRESS_TO_PLAY:
                 views.setImageViewResource(R.id.widget_play_pause_btn, R.mipmap.ic_media_play);
                 break;
         }
    }



    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action!= null && action.equals(WIDGET_PLAY_PAUSE_ACTION_PROVIDER)){
            Intent mediaAction = getIntentForPlayBtnNextState(context);
            context.startService(mediaAction);
        } else if (action != null && action.equals(WIDGET_NEXT_ACTION_PROVIDER)) {
            Intent actionNext = getNextIntentBtn(context);
            context.startService(actionNext);
        }  else if (action != null && action.equals(WIDGET_PREVIOUS_ACTION_PROVIDER)) {
            Intent actionPrev = getPreviousIntentBtn(context);
            context.startService(actionPrev);
        }
        super.onReceive(context, intent);
    }

    private Intent getNextIntentBtn ( Context context) {
        Intent intent = new Intent(context, MediaPlayBackService.class);
        intent.putExtra(WIDGET_MEDIA_ACTION_KEY, MediaPlayBackService.WIDGET_ACTION_NEXT);
        return intent;
    }

    private Intent getPreviousIntentBtn ( Context context) {
        Intent intent = new Intent(context, MediaPlayBackService.class);
        intent.putExtra(WIDGET_MEDIA_ACTION_KEY, MediaPlayBackService.WIDGET_ACTION_PREV);
        return intent;
    }

    private Intent getIntentForPlayBtnNextState(Context context){
        Intent intent = new Intent(context, MediaPlayBackService.class);
        switch (widgetCurrState) {
            case WIDGET_PRESS_TO_PLAY:
                widgetCurrState = WIDGET_PRESS_TO_PAUSE;
                intent.putExtra(WIDGET_MEDIA_ACTION_KEY, MediaPlayBackService.WIDGET_ACTION_PLAY);
                forceUpdateAppWidgets (context, false);
                break;
            case WIDGET_PRESS_TO_PAUSE:
                widgetCurrState = WIDGET_PRESS_TO_PLAY;
                intent.putExtra(WIDGET_MEDIA_ACTION_KEY, MediaPlayBackService.WIDGET_ACTION_PAUSE);
                forceUpdateAppWidgets (context, false);
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
            updateAppWidget(context, appWidgetManager, appWidgetId,false);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        if (viewModel == null){
            viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(context));
        }

        MediaPlayBackService.subscribeMediaPlaybackSubject(
                new Observer<Pair<Episode, Integer>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(Pair<Episode, Integer> info) {
                        int state = info.second;
                        Episode ep = info.first;
                        switch(state) {
                            case MediaPlayBackService.MEDIA_PLAYING:
                                widgetCurrState = WIDGET_PRESS_TO_PAUSE;
                                if (widgetImgSrc == null) {
                                    updateImgRes(context, ep);
                                } else{
                                    forceUpdateAppWidgets(context, false);
                                }
                                break;
                            case MediaPlayBackService.MEDIA_TRACK_CHANGED:
                                widgetCurrState = WIDGET_PRESS_TO_PAUSE;
                                updateImgRes(context, ep);
                                break;
                            case MediaPlayBackService.MEDIA_PAUSE:
                                widgetCurrState = WIDGET_PRESS_TO_PLAY;
                                if (widgetImgSrc == null) {
                                    updateImgRes(context, ep);
                                } else{
                                    forceUpdateAppWidgets(context, false);
                                }
                                break;
                            case MediaPlayBackService.MEDIA_STOPPED:
                                widgetCurrState = WIDGET_PRESS_TO_PLAY;
                                widgetImgSrc = null;
                                forceUpdateAppWidgets(context, false);
                                break;
                            case MediaPlayBackService.MEDIA_PLAYLIST_EMPTY:
                                widgetCurrState = WIDGET_EMPTY_PLAYLIST;
                                forceUpdateAppWidgets(context, false);
                                break;

                        }


                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                }
        );
    }

    private void forceUpdateAppWidgets (Context context, boolean changeImg) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int [] ids = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, GetYourCastWidgetProvider.class));
        for (int id : ids) {
            updateAppWidget(context, appWidgetManager, id, changeImg);
        }
    }

    private void updateImgRes (Context context, Episode ep) {
        if (viewModel != null){
            viewModel.getPodcastObservable(ep.getPodcastId())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Podcast>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Podcast podcast) {
                            // store widget image
                            widgetImgSrc = podcast.getImgLocalPath();
                            forceUpdateAppWidgets (context, true);
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        if (disposable != null ){
            disposable.dispose();
            disposable = null;
        }

    }
}

