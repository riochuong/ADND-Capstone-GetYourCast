package getyourcasts.jd.com.getyourcasts.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import getyourcasts.jd.com.getyourcasts.R;
import getyourcasts.jd.com.getyourcasts.exoplayer.MediaPlayBackService;
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast;
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp;
import getyourcasts.jd.com.getyourcasts.view.media.MediaServiceBoundListener;
import getyourcasts.jd.com.getyourcasts.view.media.PlaybackControlsFragment;
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class BaseActivity extends AppCompatActivity {

    PlaybackControlsFragment playbackControlsFragment;
    boolean isPlaybackShow = false;
    PlaybackControlViewHolder controlViewHolder;
    PodcastViewModel viewModel;
    List<MediaServiceBoundListener> mediaBoundListeners = new ArrayList<>();


    public PlaybackControlViewHolder getControlViewHolder() {
        return controlViewHolder;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        playbackControlsFragment  = (PlaybackControlsFragment) getSupportFragmentManager().findFragmentById(R.id
                .fragment_playback_controls);
        controlViewHolder = new PlaybackControlViewHolder(playbackControlsFragment.getView());
        if (playbackControlsFragment == null){
            throw new IllegalStateException("This activity does not have playback control fragment");
        }
        hidePlaybackControls();
    }

    /* hide fragment playback */
    protected void hidePlaybackControls(){
        if (playbackControlsFragment != null){
            getSupportFragmentManager().beginTransaction().hide(playbackControlsFragment).commit();
            isPlaybackShow = false;
        }
    }

    /**
     * show playbacks when ready
     */
    protected  void showPlaybackControls () {
        getSupportFragmentManager().beginTransaction()
                .show(playbackControlsFragment)
                .commit();
        isPlaybackShow = true;
    }

    public void setControlPlaybackInfo (Episode ep,  int state) {
        // load img
        viewModel.getPodcastObservable(ep.getPodcastId())
                 .observeOn(AndroidSchedulers.mainThread())
                 .subscribe(
                         new Observer<Podcast>() {
                             @Override
                             public void onSubscribe(Disposable d) {

                             }

                             @Override
                             public void onNext(Podcast podcast) {
                                 GlideApp.with(BaseActivity.this)
                                         .load(podcast.getImgLocalPath())
                                         .into(controlViewHolder.podcastImg);
                                 // set content
                                 controlViewHolder.artist.setText(podcast.getArtistName());
                                 controlViewHolder.title.setText(ep.getTitle());
                                 // set color background for the playback fragment
                                 if (playbackControlsFragment != null){
                                        playbackControlsFragment.getMainLayout()
                                                .setBackgroundColor(Integer.parseInt(podcast.getVibrantColor()));
                                 }
                                 if (state == MediaPlayBackService.MEDIA_PLAYING) {
                                     controlViewHolder.actionBtn.setImageResource(R.mipmap.ic_pause_for_list);
                                 } else {
                                     controlViewHolder.actionBtn.setImageResource(R.mipmap.ic_ep_play);
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

    @Override
    protected void onPause() {
        super.onPause();
        if (controlViewHolder.actionBtnDisposable != null){
            controlViewHolder.actionBtnDisposable.dispose();
            controlViewHolder.actionBtnDisposable = null;
        }

        // cleanup media service
        if (mediaServiceConnection != null && boundToMediaService) {
            this.unbindService(mediaServiceConnection);
            mediaService = null;
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        controlViewHolder.subscribeToMediaService();
    }

    /**
     * This viewholder store all the view information of the fragment playback
     */
    public class PlaybackControlViewHolder {
        View mainView;
        ImageView podcastImg;
        TextView title;
        TextView artist;
        ImageButton actionBtn;
        Disposable actionBtnDisposable;
        MediaPlayBackService mediaService;
        int actionBtnState = MediaPlayBackService.MEDIA_UNKNOWN_STATE;

        public void setMediaService(MediaPlayBackService mediaService) {
            this.mediaService = mediaService;
        }

        public PlaybackControlViewHolder(View mainView) {
            this.mainView = mainView;
            if (mainView == null){
                throw new IllegalStateException("Fragment root view cannot be null");
            }
            podcastImg = (ImageView) mainView.findViewById(R.id.podcastImg);
            title = (TextView) mainView.findViewById(R.id.epTitle);
            artist = (TextView) mainView.findViewById(R.id.artist);
            actionBtn = (ImageButton) mainView.findViewById(R.id.play_pause);
            // set onclick listener for the play/pause button on the playback fragment
            actionBtn.setOnClickListener(v -> {
                switch (actionBtnState){
                    case MediaPlayBackService.MEDIA_PLAYING:
                        actionBtnState = MediaPlayBackService.MEDIA_PAUSE;
                        actionBtn.setImageResource(R.mipmap.ic_ep_play);
                        mediaService.pausePlayback();
                        break;
                    case MediaPlayBackService.MEDIA_PAUSE:
                        actionBtnState  = MediaPlayBackService.MEDIA_PLAYING;
                        actionBtn.setImageResource(R.mipmap.ic_pause_for_list);
                        mediaService.resumePlayback();
                        break;
                }
            });


        }

        public void subscribeToMediaService(){
            MediaPlayBackService.subscribeMediaPlaybackSubject(new Observer<Pair<Episode, Integer>>() {
                @Override
                public void onSubscribe(Disposable d) {
                    actionBtnDisposable = d;
                }

                @Override
                public void onNext(Pair<Episode, Integer> info) {
                    switch(info.second) {
                        case MediaPlayBackService.MEDIA_PLAYING:
                            if (actionBtnState != MediaPlayBackService.MEDIA_PLAYING){
                                actionBtnState = MediaPlayBackService.MEDIA_PLAYING;
                                actionBtn.setImageResource(R.mipmap.ic_pause_for_list);
                                setControlPlaybackInfo(info.first,actionBtnState);
                                if (! BaseActivity.this.isPlaybackShow){
                                    BaseActivity.this.showPlaybackControls();
                                }

                            }

                            break;
                        case MediaPlayBackService.MEDIA_PAUSE:
                        case MediaPlayBackService.MEDIA_ADDED_TO_PLAYLIST:
                        case MediaPlayBackService.MEDIA_STOPPED:
                            if (actionBtnState != MediaPlayBackService.MEDIA_PAUSE){
                                actionBtnState = MediaPlayBackService.MEDIA_PAUSE;
                                actionBtn.setImageResource(R.mipmap.ic_ep_play);
                                setControlPlaybackInfo(info.first, actionBtnState);
                            }
                            if (!BaseActivity.this.isPlaybackShow){
                                BaseActivity.this.showPlaybackControls();
                            }
                            break;
                        case MediaPlayBackService.MEDIA_PLAYLIST_EMPTY:
                            BaseActivity.this.hidePlaybackControls();
                            break;

                    }
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


    /* ============================ CONNECT TO MEDIA SERVICE ========================================= */
    protected boolean boundToMediaService = false;
    protected MediaPlayBackService  mediaService = null;

    // connection to service
    protected ServiceConnection mediaServiceConnection =
            new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    boundToMediaService = true;
                    mediaService = ((MediaPlayBackService.MediaPlayBackServiceBinder)service).getService();
                    controlViewHolder.setMediaService(mediaService);
                    broadcastMediaServiceBound();

                }
                @Override
                public void onServiceDisconnected(ComponentName name) {
                    boundToMediaService = false;
                }
            };



    private void bindMediaService() {
        Intent intent = new Intent(this, MediaPlayBackService.class);
        this.bindService(intent, mediaServiceConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    protected void onResume() {
        super.onResume();
        bindMediaService();
    }

    public void registerMediaServiceBoundListenter(MediaServiceBoundListener listener) {
        mediaBoundListeners.add(listener);
        broadcastMediaServiceBound();
    }

    protected void broadcastMediaServiceBound(){
        if (mediaService != null && boundToMediaService) {
            for (MediaServiceBoundListener l : mediaBoundListeners){
                l.onMediaServiceBound(mediaService);
            }
        }
    }

}
