package getyourcasts.jd.com.getyourcasts.view.media;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.request.target.Target;
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

    @BindView(R.id.simple_exo_video_view) SimpleExoPlayerView playerView;
    ImageView imgView;
    PodcastViewModel viewModel;
    private Episode currentEpisode;
    private RelativeLayout mainLayout;
    private static final String TAG = MediaPlayerViewFragment.class.getSimpleName();
    private Disposable mediaServiceDisposable = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(getContext()));
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_media_player_view, container, false);
        //ButterKnife.bind(this, root);
        playerView = (SimpleExoPlayerView) root.findViewById(R.id.simple_exo_video_view);
        imgView = (ImageView) root.findViewById(R.id.ep_podcast_img_view);
        mainLayout = (RelativeLayout) root.findViewById(R.id.media_player_view_main_layout);
        initControllerView();
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }





    private void initMediaServiceSubscribe() {
        MediaPlayBackService.subscribeMediaPlaybackSubject(new Observer<Pair<Episode, Integer>>() {
            @Override
            public void onSubscribe(Disposable d) {
                mediaServiceDisposable = d;
            }

            @Override
            public void onNext(Pair<Episode, Integer> info) {
                String epId = info.first.getUniqueId();
                Integer state = info.second;

                switch(state){
                    case MediaPlayBackService.MEDIA_PLAYING:
                        if (currentEpisode == null
                                || (!currentEpisode.getUniqueId().equals(epId))) {
                            currentEpisode = info.first;
                            // load album podcast image to the view
                            loadImgViewForPodcast(info.first.getPodcastId());
                        }
                        break;
                    case MediaPlayBackService.MEDIA_PAUSE:
                    case MediaPlayBackService.MEDIA_STOPPED:
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


    private void loadImgViewForPodcast (String podcastId) {
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
                                        GlideApp.with(MediaPlayerViewFragment.this.getActivity().getApplicationContext())
                                                .load(podcast.getImgLocalPath()).into(imgView);

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

    private void initControllerView(){
        playerView.showController();
        playerView.setControllerHideOnTouch(false);
        playerView.setControllerShowTimeoutMs(-1);
    }


    @Override
    public void onResume() {
        super.onResume();
        bindMediaService();
        initMediaServiceSubscribe();
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
