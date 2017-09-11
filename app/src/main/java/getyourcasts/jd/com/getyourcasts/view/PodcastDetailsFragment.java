package getyourcasts.jd.com.getyourcasts.view;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.wang.avi.AVLoadingIndicatorView;

import getyourcasts.jd.com.getyourcasts.R;
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Channel;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast;
import getyourcasts.jd.com.getyourcasts.util.StorageUtil;
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp;
import getyourcasts.jd.com.getyourcasts.view.touchListener.SwipeDetector;
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastState;
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Created by chuondao on 9/10/17.
 */

public class PodcastDetailsFragment  extends Fragment{

    public static final String PODCAST_KEY = "podcast_key";
    public static final String TAG = "PodcastDetail";

    private PodcastViewModel viewModel;
    private Channel channelInfo;
    private Podcast podcast ;
    private Boolean subscribed = false;
    private Boolean isFullScreen = false;
    private Disposable podDisposable = null;

    // UI Items
    FloatingActionButton subscribe_button;
    ScrollView pocast_detail_scroll_view;
    TextView podcast_detail_title;
    TextView podcast_detail_artist;
    TextView podcast_total_episodes;
    TextView podcast_detail_desc;
    CoordinatorLayout podcast_detail_main_fragment;
    AVLoadingIndicatorView loading_prog_view;
    ImageView podcast_detail_img;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(this.getContext()));
        // disable subscribe button until all views are ready
        View root = inflater.inflate(R.layout.fragment_podcast_detail_layout, container, false);
        subscribe_button = (FloatingActionButton) root.findViewById(R.id.subscribe_button);
        pocast_detail_scroll_view = (ScrollView) root.findViewById(R.id.pocast_detail_scroll_view);
        podcast_detail_title = (TextView) root.findViewById(R.id.podcast_detail_title);
        podcast_detail_artist = (TextView) root.findViewById(R.id.podcast_detail_artist);
        podcast_total_episodes = (TextView) root.findViewById(R.id.podcast_total_episodes);
        podcast_detail_main_fragment = (CoordinatorLayout) root.findViewById(R.id.podcast_detail_main_fragment);
        podcast_detail_desc = (TextView) root.findViewById(R.id.podcast_detail_desc);
        loading_prog_view= (AVLoadingIndicatorView) root.findViewById(R.id.loading_prog_view);
        podcast_detail_img = (ImageView) root.findViewById(R.id.podcast_detail_img);
        return root;
    }




    private void subscribeToPodcastUpdate(Podcast podcast) {
        PodcastViewModel.subscribePodcastSubject(
                new Observer<PodcastState>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        podDisposable = d;
                    }

                    @Override
                    public void onNext(PodcastState t) {
                        if (t.getUniqueId().equals(podcast.getCollectionId())) {
                            // only the button and state have to change
                            PodcastDetailsFragment.this.subscribed = (t.getState() == PodcastState.SUBSCRIBED);
                            PodcastDetailsFragment.this.setSubscribeButtonImg();
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        subscribe_button.setEnabled(false);
        startLoadingAnim();
        podcast = getPodcastFromIntent();
        pocast_detail_scroll_view.setOnTouchListener(new DetailSwipeDetector());
        subscribed = podcast.getDescription() != null;

        // load all podcast details
        if (podcast != null) {
            // load details info
            loadPodcastImage(podcast);
            loadRssDescription(podcast);
            podcast_detail_title.setText(podcast.getCollectionName());
            podcast_detail_artist.setText(podcast.getArtistName());
            podcast_total_episodes.setText(podcast.getTrackCount() + " Episodes");

            // subscribe to any outside change
            subscribeToPodcastUpdate(podcast);
        }

        // enable swipe detector
        podcast_detail_main_fragment.setOnTouchListener( new DetailSwipeDetector());

        // enable subscribe button
        subscribe_button.setOnClickListener(
                viewItem -> {
                    if (!subscribed){
                        // no suscription yet need to subscribed
                        // download image icon
                        StorageUtil.startGlideImageDownload(podcast, getContext());
                        viewModel.getSubscribeObservable(podcast, channelInfo)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        new Observer<Boolean>() {
                                            @Override
                                            public void onSubscribe(Disposable d) {

                                            }

                                            @Override
                                            public void onNext(Boolean res) {
                                                if (res) {
                                                    viewModel.getPodcastObservable(podcast.getCollectionId())
                                                            .observeOn(AndroidSchedulers.mainThread())
                                                            .subscribe(new Observer<Podcast>() {
                                                                @Override
                                                                public void onSubscribe(Disposable d) {

                                                                }

                                                                @Override
                                                                public void onNext(Podcast podcast) {
                                                                    PodcastDetailsFragment.this.podcast = podcast;
                                                                    PodcastDetailsFragment.this.subscribed = true;
                                                                    // change fab logo
                                                                    setSubscribeButtonImg();

                                                                    Log.d(TAG, "Successfully update podcast global " +
                                                                            "var");
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
                                            public void onError(Throwable e) {
                                                e.printStackTrace();
                                                Log.e(TAG,"Failed to subscribe podcast");
                                            }

                                            @Override
                                            public void onComplete() {

                                            }
                                        }
                                );
                    }
                    else {
                        // now we need to start details list of all episodes
                        Intent intent = new Intent(getContext(), EpisodeListActivity.class);
                        intent.putExtra(PODCAST_KEY,podcast);
                        getContext().startActivity(intent);
                        Log.d(TAG, "Start Episode List Activity");
                    }
                }
        );
    }


    @Override
    public void onPause() {
        super.onPause();
        if (podDisposable != null){
            podDisposable.dispose();
            podDisposable = null;
        }
    }



    private void changeFabColor (int color){
        subscribe_button.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    private void setSubscribeButtonImg(){
        if (subscribed){
            changeFabColor(ContextCompat.getColor(this.getContext(),R.color.fab_subscribed_color));
            subscribe_button.setImageResource(R.mipmap.ic_show_episodes);
        }
        else{
            changeFabColor(ContextCompat.getColor(this.getContext(),R.color.fab_tosubscribe_color));
            subscribe_button.setImageResource(R.mipmap.ic_tosubscribe);
        }
    }

    private void loadRssDescription(Podcast pod) {
        if (subscribed && pod != null) {
            podcast_detail_desc.setText(pod.getDescription().trim());
            subscribe_button.setEnabled(true);
            setSubscribeButtonImg();
            podcast_detail_main_fragment.setVisibility(View.VISIBLE);
            loading_prog_view.setVisibility(View.GONE);
            stopLoadingAnim();
        } else {
            subscribe_button.setEnabled(false);
            viewModel.getChannelFeedObservable(pod.getFeedUrl())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            new Observer<Channel>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onNext(Channel channel) {
                                    if ((channel != null) && (channel.getChannelDescription() != null)) {
                                        // set description
                                        PodcastDetailsFragment.this.podcast_detail_desc.setText(channel
                                                .getChannelDescription().trim());
                                        // save channelInfo for later use
                                        PodcastDetailsFragment.this.channelInfo = channel;
                                    }
                                    subscribe_button.setEnabled(true);
                                    setSubscribeButtonImg();
                                    podcast_detail_main_fragment.setVisibility(View.VISIBLE);
                                    loading_prog_view.setVisibility(View.GONE);
                                    stopLoadingAnim();
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

        }
    }

    /**
     * load podcast from either local path or from http url
     */
    private void loadPodcastImage(Podcast pod) {
        if (pod.getImgLocalPath() != null) {
            GlideApp.with(this.getContext()).load(pod.getImgLocalPath()).into(podcast_detail_img);
        } else {
            GlideApp.with(this.getContext()).load(pod.getArtworkUrl100()).into(podcast_detail_img);
        }
    }

    /**
     * get podcast pass from intent
     */
    private Podcast getPodcastFromIntent() {
        Podcast podcast = getActivity().getIntent().getParcelableExtra(PODCAST_KEY);
        if (podcast != null) {
            return podcast;
        }
        Log.e(TAG, "No Podcast pass to fragment! Something is really wrong here ");
        return null;
    }


    /**
     * Swipe detector
     */

    class DetailSwipeDetector extends SwipeDetector {
        Animation expand;
        int MINIMIZE_SIZE = (int)(PodcastDetailsFragment.this.getResources().getDimension(R.dimen
                .podcast_detail_minimize_size));
        long ANIM_DURATION = 300;

        public DetailSwipeDetector() {

            expand = new Animation() {
                @Override
                public boolean willChangeBounds() {
                    return true;
                }

                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    if (interpolatedTime == 1f) {
                        pocast_detail_scroll_view.getLayoutParams().height =
                                FrameLayout.LayoutParams.MATCH_PARENT;
                    } else {
                        int trans = (int) interpolatedTime * MINIMIZE_SIZE;
                        pocast_detail_scroll_view.getLayoutParams().height =
                                trans <= MINIMIZE_SIZE ? MINIMIZE_SIZE :trans;
                    }
                    pocast_detail_scroll_view.requestLayout();
                }
            };

        }

        @Override
        public boolean onSwipeDownward() {
            if (!PodcastDetailsFragment.this.isFullScreen) {
                expand.setDuration(ANIM_DURATION);
                pocast_detail_scroll_view.startAnimation(expand);
                PodcastDetailsFragment.this.isFullScreen = true;
                // allow scrollview to interncept
                return true;
            }
            return false;
        }

        @Override
        public boolean onSwipeRightToLeft() {
            return false;
        }

        @Override
        public boolean onSwipeLeftToRight() {
            PodcastDetailsFragment.this.getActivity().onBackPressed();
            return true;
        }

        @Override
        public boolean onSwipeUpward() {
            return false;
        }
    }

    private void startLoadingAnim() {
        loading_prog_view.show();
    }

    private void stopLoadingAnim() {
        loading_prog_view.hide();
    }

}
