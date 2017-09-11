package getyourcasts.jd.com.getyourcasts.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.tonyodev.fetch.listener.FetchListener;
import com.wang.avi.AVLoadingIndicatorView;

import getyourcasts.jd.com.getyourcasts.R;
import getyourcasts.jd.com.getyourcasts.exoplayer.MediaPlayBackService;
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode;
import getyourcasts.jd.com.getyourcasts.repository.remote.network.DownloadService;
import getyourcasts.jd.com.getyourcasts.util.StorageUtil;
import getyourcasts.jd.com.getyourcasts.util.TimeUtil;
import getyourcasts.jd.com.getyourcasts.view.adapter.EpisodesRecyclerViewAdapter;
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp;
import getyourcasts.jd.com.getyourcasts.view.media.MediaPlayerActivity;
import getyourcasts.jd.com.getyourcasts.view.touchListener.SwipeDetector;
import getyourcasts.jd.com.getyourcasts.viewmodel.EpisodeState;
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;


/**
 * A placeholder fragment containing a simple view.
 */
public class EpisodeInfoFragment extends Fragment {

    private Episode currInfoEpisode;
    private int bgColor = 0;
    private String imgUrl;
    private TimeUtil.DatePub datePub = null;
    private PodcastViewModel viewModel;
    private int fabState = PRESS_TO_DOWNLOAD;
    private FetchListener downloadListener = null;
    private long transactionId = -1L;
    private Disposable mainObserverDisposable = null;
    private Disposable mediaServiceDisposable = null;


    private static final String DATE_PUB_FORMAT = "%s-%s-%s";
    private static final String MEDIA_INFO_FORMAT = "Size: %s";
    private static final String TAG = EpisodeInfoFragment.class.getSimpleName();


    // STATE OF FAB
    static final int PRESS_TO_DOWNLOAD = 0;
    static final int PRESS_TO_STOP_DOWNLOAD = 1;
    static final int PRESS_TO_PLAY = 2;
    static final int PRESS_TO_PAUSE = 3;
    static final int PRESS_TO_UNPAUSE = 4;

    // UI items
    TextView ep_info_title;
    ImageView ep_info_img;
    AppBarLayout ep_info_app_bar;
    TextView ep_info_desc;
    TextView ep_info_release;
    TextView ep_info_media_info;
    CoordinatorLayout episode_info_main_layout;
    ScrollView episode_info_scroll_view;
    FloatingActionButton ep_info_fab;
    ImageView add_to_playlist;
    AVLoadingIndicatorView episode_info_loading_anim;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        currInfoEpisode = getEpisodeFromIntent();
        bgColor = getBgColorFromIntent();
        imgUrl = getImageUrlFromIntent();
        viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(this.getContext()));
        transactionId = getDownloadTransId();
        if (currInfoEpisode.getPubDate() != null) {
            datePub = TimeUtil.parseDatePub(currInfoEpisode.getPubDate());
        }
        View root = inflater.inflate(R.layout.fragment_episode_info, container, false);
        ep_info_title = (TextView) root.findViewById(R.id.ep_info_title);
        ep_info_app_bar = (AppBarLayout) root.findViewById(R.id.ep_info_app_bar);
        ep_info_desc = (TextView) root.findViewById(R.id.ep_info_desc);
        ep_info_release = (TextView) root.findViewById(R.id.ep_info_release);
        ep_info_media_info = (TextView) root.findViewById(R.id.ep_info_media_info);
        episode_info_main_layout = (CoordinatorLayout) root.findViewById(R.id.episode_info_main_layout);
        episode_info_scroll_view = (ScrollView) root.findViewById(R.id.episode_info_scroll_view);
        ep_info_fab = (FloatingActionButton) root.findViewById(R.id.ep_info_fab);
        add_to_playlist = (ImageView) root.findViewById(R.id.add_to_playlist);
        episode_info_loading_anim = (AVLoadingIndicatorView) root.findViewById(R.id.episode_info_loading_anim);
        ep_info_img = (ImageView) root.findViewById(R.id.ep_info_img);
        return root;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startAnim();
        initViews();
    }


    private void initViews() {
        // load title
        ep_info_title.setText(currInfoEpisode.getTitle());

        // load image view
        loadEpisodeImage();

        // change bg color
        ep_info_app_bar.setBackgroundColor(bgColor);

        // load more infos
        ep_info_desc.setText(currInfoEpisode.getDescription());
        ep_info_release.setText(currInfoEpisode.getPubDate());

        // published date might not be available
        if (datePub != null) {
            ep_info_release.setText(String.format(DATE_PUB_FORMAT, datePub.getMonth(), datePub.getDayOfMonth(), datePub
                    .getYear()));
        }

        if (currInfoEpisode.getFileSize() != null) {
            ep_info_media_info.setText(
                    String.format(MEDIA_INFO_FORMAT, StorageUtil.convertToMbRep(currInfoEpisode.getFileSize())));
        }

        setFabButtonOnClickListener();

        // init fab
        adjustFabState();

        // add swipe detector to scroll views
        episode_info_main_layout.setOnTouchListener(new SimpleSwipeDetector());
        episode_info_scroll_view.setOnTouchListener(new SimpleSwipeDetector());

        // enable main view
        stopAnim();
    }


    private void subscribeToMediaServiceSubject() {

        MediaPlayBackService.subscribeMediaPlaybackSubject(
                new Observer<Pair<Episode, Integer>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mediaServiceDisposable = d;
                    }

                    @Override
                    public void onNext(Pair<Episode, Integer> t) {
                        Episode episode = t.first;
                        Integer state = t.second;
                        if (episode != null && currInfoEpisode.getUniqueId().equals(episode.getUniqueId())) {
                            // check which state we should set the fab
                            switch (state) {
                                // only need to change if this is already playing
                                case MediaPlayBackService.MEDIA_PLAYING:
                                    fabState = PRESS_TO_PAUSE;
                                    ep_info_fab.setImageResource(R.mipmap.ic_pause);
                                    break;

                                case MediaPlayBackService.MEDIA_STOPPED:
                                    fabState = PRESS_TO_PLAY;
                                    ep_info_fab.setImageResource(R.mipmap.ic_play_white);
                                    break;

                                case MediaPlayBackService.MEDIA_PAUSE:
                                    fabState = PRESS_TO_UNPAUSE;
                                    ep_info_fab.setImageResource(R.mipmap.ic_play_white);
                                    break;

                                // if episode removed from playlist we can allow it to be added back
                                case MediaPlayBackService.MEDIA_REMOVED_FROM_PLAYLIST:
                                    // need to update the list icon
                                    add_to_playlist.setImageResource(R.mipmap.ic_add_to_play_list);
                                    break;

                            }

                        } else {
                            if (fabState != PRESS_TO_DOWNLOAD && fabState != PRESS_TO_PLAY) {
                                fabState = PRESS_TO_PLAY;
                                ep_info_fab.setImageResource(R.mipmap.ic_play_white);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Error during receiving media playback service info ");
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                }
        );
    }

    private void subscribeToEpisodeSubject(Episode ep) {

        PodcastViewModel.subscribeEpisodeSubject(new Observer<EpisodeState>() {
            @Override
            public void onSubscribe(Disposable d) {
                mainObserverDisposable = d;
            }

            @Override
            public void onNext(EpisodeState epState) {
                if (epState.getUniqueId().equals(ep.getEpisodeUniqueKey())) {
                    // state changes..update episode
                    viewModel.getEpisodeObsevable(ep)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    new Observer<Episode>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {

                                        }

                                        @Override
                                        public void onNext(Episode episode) {
                                            currInfoEpisode = episode;
                                            switch (epState.getState()) {
                                                case EpisodeState.DOWNLOADING:
                                                    fabState = PRESS_TO_STOP_DOWNLOAD;
                                                    ep_info_fab.setVisibility(View.VISIBLE);
                                                    ep_info_fab.setImageResource(R.mipmap.ic_stop_white);
                                                    break;
                                                case EpisodeState.FETCHED:
                                                    fabState = PRESS_TO_DOWNLOAD;
                                                    ep_info_fab.setVisibility(View.VISIBLE);
                                                    ep_info_fab.setImageResource(R.mipmap.ic_fab_tosubscribe);
                                                    currInfoEpisode.setLocalUrl(null);
                                                    break;

                                                case EpisodeState.DOWNLOADED:
                                                    fabState = PRESS_TO_PLAY;
                                                    ep_info_fab.setVisibility(View.VISIBLE);
                                                    ep_info_fab.setImageResource(R.mipmap.ic_play_white);
                                                    add_to_playlist.setVisibility(View.VISIBLE);
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
                                    }
                            );
                    // on Next

                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                ep_info_fab.setVisibility(View.VISIBLE);
                ep_info_fab.setImageResource(R.mipmap.ic_play_white);
            }
        });
    }


    private void initAddToPlayListListener() {
        // first initialize add to playlist icon
        // if already in the list then we should not show the add icon
        if (mediaService != null && mediaService.isEpisodeInPlayList(currInfoEpisode.getUniqueId())) {
            add_to_playlist.setImageResource(R.mipmap.ic_already_add_to_playlist);
        } else {
            add_to_playlist.setImageResource(R.mipmap.ic_add_to_play_list);
        }

        add_to_playlist.setOnClickListener(
                view -> {
                    // ADD song to play list
                    Log.d(TAG, "ADD TO PLAYLIST !!! ");
                    if (mediaService != null && !mediaService.isEpisodeInPlayList(currInfoEpisode.getUniqueId())) {
                        mediaService.addTrackToEndPlaylist(currInfoEpisode);
                        add_to_playlist.setImageResource(R.mipmap.ic_already_add_to_playlist);
                    }
                });
    }

    /**
     * helper to initialize FAB button
     */
    private void adjustFabState() {
        // change fab color to red always
        changeFabColor(ContextCompat.getColor(this.getContext(), R.color.unfin_color));

        if (getIsDownloadingFromIntent()) {
            fabState = PRESS_TO_STOP_DOWNLOAD;
            ep_info_fab.setVisibility(View.INVISIBLE);
            ep_info_fab.setImageResource(R.mipmap.ic_stop_dl);
        } else if (currInfoEpisode.getDownloaded() == EpisodeState.DOWNLOADED) {
            fabState = PRESS_TO_PLAY;
            ep_info_fab.setVisibility(View.VISIBLE);
            ep_info_fab.setImageResource(R.mipmap.ic_play_white);
            add_to_playlist.setVisibility(View.VISIBLE);
        } else {
            fabState = PRESS_TO_DOWNLOAD;
            ep_info_fab.setVisibility(View.VISIBLE);
            ep_info_fab.setImageResource(R.mipmap.ic_fab_tosubscribe);
        }
    }


    // start loading animation
    private void startAnim() {
        episode_info_loading_anim.show();
        episode_info_loading_anim.setVisibility(View.VISIBLE);
        episode_info_main_layout.setVisibility(View.INVISIBLE);
    }

    // stop loading animation and show main screen
    private void stopAnim() {
        episode_info_loading_anim.show();
        episode_info_loading_anim.setVisibility(View.GONE);
        episode_info_main_layout.setVisibility(View.VISIBLE);
    }

    private void changeFabColor(int color) {
        ep_info_fab.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    private void setFabButtonOnClickListener() {
        // set on click listener
        ep_info_fab.setOnClickListener(
                // check if the ep is donwloading
                view -> {
                    switch (fabState) {
                        case PRESS_TO_DOWNLOAD:
                            // check if episode is already state or not
                            // bind download service
                            ep_info_fab.setImageResource(R.mipmap.ic_stop_dl);
                            fabState = PRESS_TO_STOP_DOWNLOAD;
                            startDownloadEpisode();
                            break;
                        case PRESS_TO_STOP_DOWNLOAD:
                            fabState = PRESS_TO_DOWNLOAD;
                            ep_info_fab.setImageResource(R.mipmap.ic_fab_tosubscribe);
                            requestStopDownloadAndCleanup();
                            break;

                        case PRESS_TO_PLAY:
                            // limited to downloaded episode only for now
                            if (mediaService != null && currInfoEpisode != null && currInfoEpisode.getLocalUrl() != null) {
                                fabState = PRESS_TO_PAUSE;
                                ep_info_fab.setImageResource(R.mipmap.ic_pause);
                                // start playing here
                                mediaService.playMediaFile(currInfoEpisode);
                                getContext().startActivity(new Intent(getContext(), MediaPlayerActivity.class));
                            }
                            break;


                        case PRESS_TO_PAUSE:
                            if (mediaService != null && currInfoEpisode != null) {
                                fabState = PRESS_TO_UNPAUSE;
                                ep_info_fab.setImageResource(R.mipmap.ic_play_white);
                                mediaService.pausePlayback();
                            }
                            break;


                        case PRESS_TO_UNPAUSE:
                            if (mediaService != null && currInfoEpisode != null) {
                                fabState = PRESS_TO_PAUSE;
                                ep_info_fab.setImageResource(R.mipmap.ic_pause);
                                mediaService.resumePlayback();
                                getContext().startActivity(new Intent(getContext(), MediaPlayerActivity.class));
                            }
                            break;
                    }

                });
    }

    private void requestStopDownloadAndCleanup() {
        if ((serviceConnection != null)
                && boundToDownload
                && (downloadService != null)
                && (currInfoEpisode.getDownloadUrl() != null)
                && !currInfoEpisode.getDownloadUrl().trim().isEmpty())
        {
            downloadService.requestStopDownload(transactionId);
        }
    }

    private void startDownloadEpisode() {
        if ((serviceConnection != null)
                && boundToDownload
                && (downloadService != null)
                && (currInfoEpisode.getDownloadUrl() != null)
                && !currInfoEpisode.getDownloadUrl().trim().isEmpty())
        {

            // get download path and filename
            Pair<String, String> downloadsPath = StorageUtil.getPathToStoreEp(currInfoEpisode, this.getContext());

            // get transaction id for
            transactionId = downloadService.requestDownLoad(
                    currInfoEpisode,
                    currInfoEpisode.getDownloadUrl(),
                    downloadsPath.first,
                    downloadsPath.second);
        } else{
            Log.e(TAG, "Download Service is not bound or Download URL is bad ${currInfoEpisode.toString()} ");
        }
    }


    private void loadEpisodeImage() {
        GlideApp.with(getContext())
                .load(imgUrl)
                .into(ep_info_img);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // clean up download service
        if (serviceConnection != null && boundToDownload) {
            this.getContext().unbindService(serviceConnection);
            downloadService = null;
        }

        // cleanup media service
        if (mediaServiceConnection != null && boundToMediaService) {
            this.getContext().unbindService(mediaServiceConnection);
            mediaService = null;
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (this.downloadListener != null
                && downloadService != null) {
            downloadService.unregisterListener(this.downloadListener);
        }
        // destroy main observer
        if (mainObserverDisposable != null) {
            mainObserverDisposable.dispose();
            mainObserverDisposable = null;
        }

        if (mediaServiceDisposable != null) {
            mediaServiceDisposable.dispose();
            mediaServiceDisposable = null;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        bindDownloadService();
        bindMediaService();
        subscribeToEpisodeSubject(currInfoEpisode);
        subscribeToMediaServiceSubject();
        if (mediaService != null) {
            initAddToPlayListListener();
        }
    }



    private void bindDownloadService() {
        Intent intent = new Intent(this.getContext(), DownloadService.class);
        this.getContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void bindMediaService() {
        Intent intent = new Intent(this.getContext(), MediaPlayBackService.class);
        this.getContext().bindService(intent, mediaServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /* ============================ CONNECT TO DOWNLOAD SERVICE ========================================= */
    private boolean boundToDownload = false;
    private DownloadService  downloadService;

    // connection to service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            boundToDownload = true;
            downloadService = ((DownloadService.DownloadServiceBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            boundToDownload = false;
        }
    };



    /* ============================ CONNECT TO MEDIA SERVICE ========================================= */
    private boolean boundToMediaService = false;
    private MediaPlayBackService mediaService = null;

    // connection to service
    private ServiceConnection mediaServiceConnection  = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            boundToMediaService = true;
            mediaService = ((MediaPlayBackService.MediaPlayBackServiceBinder)service).getService();
            initAddToPlayListListener();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            boundToMediaService = false;
        }
    };






    /**
     * retreived episode passed from the fragment list
     */
    private Episode  getEpisodeFromIntent()

    {
        return getActivity().getIntent().getParcelableExtra(EpisodesRecyclerViewAdapter.EPISODE_KEY);
    }

    /**
     * retreive back ground color
     */
    private int getBgColorFromIntent()

    {
        return getActivity().getIntent().getIntExtra(EpisodesRecyclerViewAdapter.BG_COLOR_KEY,0);
    }

    private long getDownloadTransId()
    {
        return getActivity().getIntent().getLongExtra(EpisodesRecyclerViewAdapter.DL_TRANS_ID,-1);
    }

    private String getImageUrlFromIntent()

    {
        return this.getActivity().getIntent().getStringExtra(EpisodesRecyclerViewAdapter.PODAST_IMG_KEY);
    }

    private boolean getIsDownloadingFromIntent()

    {
        return this.getActivity().getIntent().getBooleanExtra(
                EpisodesRecyclerViewAdapter.IS_DOWNLOADING, false);
    }

    private Long getDownloadingStatusFromIntent()

    {
        return this.getActivity().getIntent().getLongExtra(
                EpisodesRecyclerViewAdapter.IS_DOWNLOADING_KEY, -1);
    }


    class SimpleSwipeDetector extends SwipeDetector {

        @Override
        public boolean onSwipeDownward() {
            return false;
        }

        @Override
        public boolean onSwipeRightToLeft() {
            return false;
        }

        @Override
        public boolean onSwipeLeftToRight() {
            EpisodeInfoFragment.this.getActivity().onBackPressed();
            return true;
        }

        @Override
        public boolean onSwipeUpward() {
            return false;
        }
    }


}
