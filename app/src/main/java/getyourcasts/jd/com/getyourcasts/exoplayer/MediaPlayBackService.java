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
import android.util.Log;
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
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import getyourcasts.jd.com.getyourcasts.R;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode;
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

    private MediaPlayBackServiceBinder binder = new MediaPlayBackServiceBinder();
    private Disposable playListRemoveDisposable;

    /*Simple fields for playlist management*/
    private List<Episode> playList ;
    private int currEpisodePos = -1;


    private static final String TAG = MediaPlayBackService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1990;

    // state of the playback
    public static final int MEDIA_PLAYING = 0;
    public static final int MEDIA_PAUSE = 1;
    public static final int MEDIA_STOPPED = 2;
    public static final int MEDIA_TRACK_CHANGED = 3;

    // state of the playlist
//    public static final int MEDIA_ADDED_TO_TOP_PLAYLIST = 3;
//    public static final int MEDIA_ADDED_TO_END_PLAYLIST = 4;
    public static final int MEDIA_REMOVED_FROM_PLAYLIST = 5;

    /* MEDIA STATE OBJECT */
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
        if (exoPlayer != null){
            exoPlayer.release();
        }

        if (exoPlayer == null) {
            exoPlayer = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector());
            mediaSessionConn.setPlayer(exoPlayer, null);
            mediaSessionConn.mediaSession.setActive(true);
        }
    }

    private void initPlayListRemoveObserver(){
        subscribeMediaPlaybackSubject(new Observer<Pair<Episode, Integer>>() {
            @Override
            public void onSubscribe(Disposable d) {
                playListRemoveDisposable = d;
            }

            @Override
            public void onNext(Pair<Episode, Integer> info) {
                    int state = info.second;
                    switch (state){
                        case MEDIA_REMOVED_FROM_PLAYLIST:
                            int index = findIndexFromList(info.first);
                            if (index >= 0) {removeTrackFromPlayList(index);}
                            break;
                    }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private synchronized int findIndexFromList(Episode ep) {
        for (int i = 0; i < playList.size() ; i++) {
            if (playList.get(i).getUniqueId().equals(ep.getUniqueId())){
                return i;
            }
        }
        Log.e(TAG, "Cannot find episode from playlist");
        return -1;
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
                .setContentTitle(playList.get(currEpisodePos).getTitle())
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mediaSessionConn = buildMediaSession();
        dataSourceFactory = buildDataSource();
        extractorFactory = buildExtractorFactory();
        playList = new ArrayList<>();
        initPlayListRemoveObserver();
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

        if (playListRemoveDisposable != null ){
            playListRemoveDisposable.dispose();
            playListRemoveDisposable = null;
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
    public synchronized void playMediaFile(Episode episode) {
        stopPlayback();
        initExoPlayer();
        exoPlayer.addListener(this);
        if (exoPlayer != null && episode.getLocalUrl() != null) {
            currEpisodePos = 0;
            addTrackToPlaylist(episode, 0);
            if (currEpisodePos >= 0) publishMediaPlaybackSubject(playList.get(currEpisodePos), MEDIA_STOPPED);
            startServiceAsForeground();
            exoPlayer.prepare(buildMediaSourceFromUrl(episode.getLocalUrl()));
            exoPlayer.setPlayWhenReady(true);

        }

    }

    /**
     * add this function to support select song on the playlist side
     * @param index
     */
    public synchronized void playMediaFileAtIndex(int index) {
        stopPlayback();
        initExoPlayer();
        exoPlayer.addListener(this);
        if (index >= 0 && index < playList.size()) {
            currEpisodePos = index;
            startServiceAsForeground();
            exoPlayer.prepare(buildMediaSourceFromUrl(playList.get(index).getLocalUrl()));
            exoPlayer.setPlayWhenReady(true);
            publishMediaPlaybackSubject(playList.get(currEpisodePos), MEDIA_TRACK_CHANGED);
        }

    }

    public void setPlayerView (SimpleExoPlayerView view) {
        view.setPlayer(exoPlayer);
    }


    private int findDuplicateIndex(Episode ep) {
        for (int i = 0; i < playList.size(); i++) {
            if (playList.get(i).getUniqueId().equals(ep.getUniqueId())){
                return i;
            }
        }
        return -1;
    }

    private synchronized void addTrackToPlaylist(Episode episode, int index) {
        if (episode.getLocalUrl() != null) {
            int dupIndex = findDuplicateIndex(episode);
            if (dupIndex > 0) {
                Episode removedEp = playList.get(index);
                playList.remove(dupIndex);
            }
            playList.add(index, episode);
        }
    }

    /**
     * add track to the current end of the playlist
     * @param episode
     */
    public void addTrackToEndPlaylist(Episode episode) {
        if (episode.getLocalUrl() != null) {
            MediaSource mediaSource = buildMediaSourceFromUrl(episode.getLocalUrl());
            int lastIndex = playList.size();
            playList.add(episode);
        }
    }


    /**
     * remove item from playlist
     * @param index
     */
    private synchronized void removeTrackFromPlayList(int index) {
        playList.remove(index);
    }

    /**
     * to help synchronize between playlist activity and current playlist
     * @return
     */
    public synchronized List<Episode> getMediaPlaylist() {
        // make a copy here to avoid mutable outside of this class
        List<Episode> newList = new ArrayList<>();
        for (Episode ep: this.playList) {
            newList.add(new Episode(ep));
        }
        return newList;
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
            currEpisodePos = -1;
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
            Log.d(TAG, "Track Changed");
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (currEpisodePos >= 0) {
            switch (playbackState) {

                case Player.STATE_READY:
                    if (exoPlayer.getPlayWhenReady()) {
                        publishMediaPlaybackSubject(playList.get(currEpisodePos), MEDIA_PLAYING);
                    }
                    else{
                        publishMediaPlaybackSubject(playList.get(currEpisodePos), MEDIA_PAUSE);
                    }

                case Player.STATE_BUFFERING:
                    break;
                case Player.STATE_ENDED:
                    // now pick and play next song !!!
                    int nextIndex = (currEpisodePos == playList.size() - 1) ? 0 : currEpisodePos + 1;
                    playMediaFileAtIndex(nextIndex);
                    break;
                case Player.STATE_IDLE:
                    publishMediaPlaybackSubject(playList.get(currEpisodePos), MEDIA_STOPPED);

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