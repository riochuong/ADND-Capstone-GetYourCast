package getyourcasts.jd.com.getyourcasts.exoplayer;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Pair;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.DynamicConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import org.jetbrains.annotations.NotNull;

import getyourcasts.jd.com.getyourcasts.R;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode;
import getyourcasts.jd.com.getyourcasts.view.media.MediaPlayerActivity;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;


/**
 * Created by chuondao on 8/11/17.
 */

public class MediaPlayBackService extends Service implements Player.EventListener {

    private SimpleExoPlayer exoPlayer = null;
    private DataSource.Factory dataSourceFactory = null;
    private ExtractorsFactory extractorFactory = null;
    private MediaSessionConnector mediaSessionConn;
    private Episode currEpisode = null;
    private MediaPlayBackServiceBinder binder = new MediaPlayBackServiceBinder();
    private DynamicConcatenatingMediaSource dynamicMediaSource = new DynamicConcatenatingMediaSource();


    private static final String TAG = MediaPlayBackService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1990;

    // state of the playback
    public static final int MEDIA_PLAYING = 0;
    public static final int MEDIA_PAUSE = 1;
    public static final int MEDIA_STOPPED = 2;


    private static Subject<Pair<Episode, Integer>> MediaPlaybackSubject = BehaviorSubject.create();

    public static void subscribeMediaPlaybackSubject(Observer<Pair<Episode, Integer>> obsvr) {
        MediaPlaybackSubject.observeOn(AndroidSchedulers.mainThread()).subscribe(obsvr);
    }

    public static void publishMediaPlaybackSubject(Episode episode, int state) {
        MediaPlaybackSubject.onNext(new Pair(episode, state));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void initExoPlayer() {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector());
            dataSourceFactory = buildDataSource();
            extractorFactory = buildExtractorFactory();
            mediaSessionConn.setPlayer(exoPlayer, null);
            mediaSessionConn.mediaSession.setActive(true);
        }
    }

    private void startServiceAsForeground() {
        startForeground(NOTIFICATION_ID, buildForegroundNotification());
    }

    /**
     * build notification to let user know apps is playing music
     */
    private Notification buildForegroundNotification()

    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setOngoing(true)
                .setContentTitle(currEpisode.getTitle())
                .setSmallIcon(R.mipmap.ic_play_white);
        return builder.build();
    }


    private MediaSessionConnector buildMediaSession()

    {
        MediaSessionCompat mediaSession = new MediaSessionCompat(this, TAG);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat
                .FLAG_HANDLES_TRANSPORT_CONTROLS);
        // dont want media button to start our app when it is off
        mediaSession.setMediaButtonReceiver(null);
        mediaSession.setCallback(new MediaSessionCallback());

        // set an intiail playback state with ACTION_PLAY, so media buttons can start the player
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder().setActions(
                PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_STOP
        );
        mediaSession.setPlaybackState(stateBuilder.build());
        MediaSessionConnector mediaSessionConn = new MediaSessionConnector(mediaSession);
        return mediaSessionConn;
    }


    private MediaSource buildMediaSourceFromUrl(String localUrl)

    {
        MediaSource mediaSource =
                new ExtractorMediaSource(Uri.parse(localUrl), dataSourceFactory,
                        extractorFactory, null, null);
        return mediaSource;
    }

    public void playLocalVideo(Episode episode, SimpleExoPlayerView view) {
        stopPlayback();
        initExoPlayer();
        if (exoPlayer != null && episode.getLocalUrl() != null) {
            exoPlayer.prepare(buildMediaSourceFromUrl(episode.getLocalUrl()));
            exoPlayer.setPlayWhenReady(true);
            view.setPlayer(exoPlayer);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mediaSessionConn = buildMediaSession();
        return START_STICKY;
    }


    @Override
    public void onDestroy() {

        if (exoPlayer != null) {
            stopPlayback();
        }
        // clean up media session
        if (mediaSessionConn != null) {
            mediaSessionConn.mediaSession.release();
        }
        super.onDestroy();
    }

    private DataSource.Factory buildDataSource()

    {
        return new DefaultDataSourceFactory(this, this.getResources().getString(R.string.app_name));
    }

    private ExtractorsFactory buildExtractorFactory()

    {
        return new DefaultExtractorsFactory();
    }


    /*====================== SERVICE APP FUNTIONS EXPOSED TO CLIENTS ============ */

    /**
     * play an audio file from a local url
     */
    public void playMediaFile(Episode episode) {
        stopPlayback();
        initExoPlayer();
        exoPlayer.addListener(this);
        if (exoPlayer != null && episode.getLocalUrl() != null) {
            exoPlayer.prepare(buildMediaSourceFromUrl(episode.getLocalUrl()));
            exoPlayer.setPlayWhenReady(true);

            // before stop this player ...send out a cast for other view
            if (currEpisode != null) publishMediaPlaybackSubject(currEpisode, MEDIA_STOPPED);
            currEpisode = episode;
            startServiceAsForeground();
        }
    }

    public void setPlayerView (SimpleExoPlayerView view) {
        view.setPlayer(exoPlayer);
    }


    public void addTrackToPlaylist(Episode episode) {
        if (episode.getLocalUrl() != null) {
            MediaSource mediaSource = buildMediaSourceFromUrl(episode.getLocalUrl());
            dynamicMediaSource.addMediaSource(mediaSource);
        }
    }

    public void registerPlayerListener(Player.EventListener listener) {
        if (exoPlayer != null) {
            exoPlayer.addListener(listener);
        }
    }

    public void stopPlayback() {
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer.removeListener(this);
            exoPlayer = null;
            currEpisode = null;
            stopForeground(true);
            mediaSessionConn.mediaSession.setActive(false);
        }

    }

    public void pausePlayback() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
        }
    }

    public void resumePlayback() {
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(true);
        }
    }


    /* ======================================= LISTENER ================================================*/
    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (currEpisode != null) {
            switch (playbackState) {

                case Player.STATE_READY:
                    if (exoPlayer.getPlayWhenReady()) {
                        publishMediaPlaybackSubject(currEpisode, MEDIA_PLAYING);
                    }
                    else{
                        publishMediaPlaybackSubject(currEpisode, MEDIA_PAUSE);
                    }

                case Player.STATE_BUFFERING:
                    break;

                case Player.STATE_ENDED:
                case Player.STATE_IDLE:
                    publishMediaPlaybackSubject(currEpisode, MEDIA_STOPPED);

            }
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }


    public class MediaPlayBackServiceBinder extends Binder {
        public MediaPlayBackService getService() {
            return MediaPlayBackService.this;
        }
    }

    /* ====== EXOPLAYER LISTENERS ===========*/


    /* MEDIA SESSION CALL BACK*/
    private class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            exoPlayer.setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            exoPlayer.setPlayWhenReady(false);
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
        }

        @Override
        public void onSkipToPrevious() {
            exoPlayer.seekTo(0);
        }

        @Override
        public void onStop() {
            exoPlayer.stop();
        }

    }
}