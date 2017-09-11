package getyourcasts.jd.com.getyourcasts.view.media;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

import butterknife.BindView;
import getyourcasts.jd.com.getyourcasts.R;
import getyourcasts.jd.com.getyourcasts.exoplayer.MediaPlayBackService;
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast;
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp;
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Created by chuondao on 8/11/17.
 */

public class MediaPlayerViewFragment extends Fragment {

    private static final int CONTROLLER_TIMEOUT = 1000 ;
    @BindView(R.id.simple_exo_video_view) SimpleExoPlayerView playerView;
    PodcastViewModel viewModel;
    ImageView exoShutter;
    AspectRatioFrameLayout videoSurfaceView;
    private Episode currentEpisode;
    private LinearLayout mainLayout;
    private static final String TAG = MediaPlayerViewFragment.class.getSimpleName();
    private Disposable mediaServiceDisposable = null;
    private String podcastId = null;
    private boolean isVideo = false;
    private TextView episodeTitle;
    TextView emptyView;
    ImageView close_icon;
    PlaybackControlView controlView;
    ImageButton mediaNextBtn;
    ImageButton mediaPrevBtn;

    /* Keys for saving bundle state */
    private static final String PODCAST_ID_KEY  = "podcast_id_key";
    private static final String IS_VIDEO_KEY  = "is_video_key";
    private  static final String CURR_EP_KEY = "curr_ep_key";


    public static MediaPlayerViewFragment newInstance() {
        MediaPlayerViewFragment fragment = new MediaPlayerViewFragment();
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(getContext()));
        // if we need to load some saved data
        if (savedInstanceState != null){
            loadInstanceState(savedInstanceState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        savePlayerState(outState);
        super.onSaveInstanceState(outState);
    }

    /**
     * Save the state of the playback
     * @param bundle
     */
    private void savePlayerState(Bundle bundle){
        if (currentEpisode != null && podcastId != null){
            bundle.putString(PODCAST_ID_KEY,podcastId);
            bundle.putParcelable(CURR_EP_KEY,currentEpisode);
            bundle.putBoolean(IS_VIDEO_KEY, isVideo);
        }
    }

    private void loadInstanceState(Bundle bundle){
        String podcastId = bundle.getString(PODCAST_ID_KEY, null);
        if (podcastId != null){
            this.podcastId = podcastId;
            currentEpisode = bundle.getParcelable(CURR_EP_KEY);
            this.isVideo = bundle.getBoolean(IS_VIDEO_KEY);
        }

    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // check configuration
        View root = null;
        int orientation = getCurrentOrientation();
        if (orientation == Configuration.ORIENTATION_LANDSCAPE && isVideo){
            Log.d(TAG, "In Landscape mode");
            root = inflater.inflate(R.layout.fragment_media_player_view_horizontal_video, container, false);
            playerView = (SimpleExoPlayerView) root.findViewById(R.id.simple_exo_video_view);
        }
        else{ // IN PORTRAIT MODE && not landscape video
            root = inflater.inflate(R.layout.fragment_media_player_view_vertical, container, false);
            playerView = (SimpleExoPlayerView) root.findViewById(R.id.simple_exo_video_view);
            close_icon = (ImageView) root.findViewById(R.id.close_icon);
            // set X icon to go back to previous screen
            if (close_icon != null) {
                close_icon.setOnClickListener(
                        view -> {
                            getActivity().onBackPressed();
                        }
                );
            }
        }

        // find common features
        videoSurfaceView = (AspectRatioFrameLayout) playerView.findViewById(R.id.exo_content_frame);
        mainLayout = (LinearLayout) root.findViewById(R.id.media_player_view_main_layout);
        // some of the below might be null
        exoShutter = (ImageView) playerView.findViewById(R.id.exo_shutter);
        episodeTitle = (TextView) playerView.findViewById(R.id.media_player_view_episode_title);
        emptyView =(TextView) root.findViewById(R.id.media_player_empty_view);
        controlView = (PlaybackControlView) playerView.findViewById(R.id.exo_controller);
        mediaNextBtn = (ImageButton) controlView.findViewById(R.id.playback_exo_next);
        mediaPrevBtn = (ImageButton) controlView.findViewById(R.id.playback_exo_prev);
        // set this to enable text marquee
        if (episodeTitle != null) {episodeTitle.setSelected(true);}

        return root;
    }

    private int getCurrentOrientation () {
        return  getContext().getResources().getConfiguration().orientation;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // set on click listener for the custom next / prev btn
        mediaNextBtn.setOnClickListener(
                nextView -> {
                     if (mediaService != null && boundToMediaService){
                         mediaService.playNextSongInPlaylist();
                     }
                }
        );

        mediaPrevBtn.setOnClickListener(
                prevView ->{
                        if(mediaService != null && boundToMediaService){
                            mediaService.playPreviousSongInPlaylist();
                        }
                }
        );
    }

    private void initMediaServiceSubscribe() {
        MediaPlayBackService.subscribeMediaPlaybackSubject(new Observer<Pair<Episode, Integer>>() {
            @Override
            public void onSubscribe(Disposable d) {
                mediaServiceDisposable = d;
            }

            @Override
            public void onNext(Pair<Episode, Integer> info) {
                Integer state = info.second;

                switch(state){
                    case MediaPlayBackService.MEDIA_STOPPED:
                    case MediaPlayBackService.MEDIA_PAUSE:
                    case MediaPlayBackService.MEDIA_PLAYING:
                        String epId = info.first.getUniqueId();
                        if (currentEpisode == null
                                || (!currentEpisode.getUniqueId().equals(epId))) {

                            reloadCorrectDataForFragment(info);
                        }
                        break;
                    case MediaPlayBackService.MEDIA_ADDED_TO_PLAYLIST:
                    case MediaPlayBackService.MEDIA_TRACK_CHANGED:
                        reloadCorrectDataForFragment(info);
                        break;
                    case MediaPlayBackService.MEDIA_PLAYLIST_EMPTY:
                        enablePlayerView(false);
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

    private void enablePlayerView(boolean enable){
        if (! enable) {
            playerView.setVisibility(View.GONE);
            // show empty view
            emptyView.setVisibility(View.VISIBLE);
        } else{
            playerView.setVisibility(View.VISIBLE);
            // show empty view
            emptyView.setVisibility(View.GONE);
        }
    }

    private void reloadCorrectDataForFragment(Pair<Episode, Integer>info) {
        currentEpisode = info.first;
        podcastId = info.first.getPodcastId();
        enablePlayerView(true);
        // load album podcast image to the view
        if (info.first.getType().contains("audio")){
            isVideo = false;
        }
        else{
            isVideo = true;
        }
        // load image
        loadImgViewForPodcast(info.first.getPodcastId(),  isVideo);
        if (mediaService != null){
            mediaService.setPlayerView(playerView);
        }
    }



    private void loadImgViewForPodcast (String podcastId, boolean isVideo) {
        try {
            viewModel.getPodcastObservable(podcastId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            new Observer<Podcast>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onNext(Podcast podcast) {
                                    try {
                                        if (!isVideo){
                                            GlideApp.with(MediaPlayerViewFragment.this.getActivity().getApplicationContext())
                                                    .load(podcast.getImgLocalPath()).into(exoShutter);
                                            if (videoSurfaceView != null && exoShutter != null){
                                                videoSurfaceView.setVisibility(View.GONE);
                                                exoShutter.setVisibility(View.VISIBLE);
                                            }
                                            initControllerView(false);
                                        } else{
                                            videoSurfaceView.setVisibility(View.VISIBLE);
                                            if (exoShutter != null){
                                                exoShutter.setVisibility(View.GONE);
                                            }
                                            initControllerView(true);
                                        }
                                        if (episodeTitle != null){
                                            episodeTitle.setText(currentEpisode.getTitle());
                                        }
                                        mainLayout.setBackgroundColor(Integer.parseInt(podcast.getVibrantColor()));
                                    } catch (NumberFormatException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    e.printStackTrace();
                                }

                                @Override
                                public void onComplete() {

                                }
                            }
                    );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initControllerView(boolean autohide){
        playerView.showController();
        if (!autohide){
            playerView.setControllerHideOnTouch(false);
            playerView.setControllerShowTimeoutMs(-1);
        }
        else{
            playerView.setControllerHideOnTouch(true);
            playerView.setControllerShowTimeoutMs(CONTROLLER_TIMEOUT);
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        bindMediaService();
        initMediaServiceSubscribe();
        if (currentEpisode != null && podcastId != null){
            loadImgViewForPodcast(podcastId, isVideo);
            if (episodeTitle != null){
                episodeTitle.setText(currentEpisode.getTitle());
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mediaService != null){
            getContext().unbindService(mediaServiceConnection);
            mediaService = null;
        }

        if (mediaServiceDisposable != null){
            mediaServiceDisposable.dispose();
            mediaServiceDisposable = null;
        }
    }


    private void bindMediaService() {
        Intent intent = new Intent(this.getContext(), MediaPlayBackService.class);
        this.getContext().bindService(intent, mediaServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /* ============================ CONNECT TO MEDIA SERVICE ========================================= */
    private boolean boundToMediaService = false;
    private MediaPlayBackService mediaService = null;

    // connection to service
    private ServiceConnection mediaServiceConnection =
            new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    boundToMediaService = true;
                    mediaService = ((MediaPlayBackService.MediaPlayBackServiceBinder) (service)).getService();
                    mediaService.setPlayerView(playerView);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    boundToMediaService = false;
                }
            };

}
