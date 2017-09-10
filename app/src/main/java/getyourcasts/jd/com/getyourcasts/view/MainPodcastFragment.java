package getyourcasts.jd.com.getyourcasts.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import getyourcasts.jd.com.getyourcasts.R;
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast;
import getyourcasts.jd.com.getyourcasts.view.adapter.PodcastMainViewAdapter;
import getyourcasts.jd.com.getyourcasts.view.media.MediaPlayerActivity;
import getyourcasts.jd.com.getyourcasts.view.media.MediaPlayerViewFragment;
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastState;
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainPodcastFragment extends Fragment {
    private PodcastViewModel viewModel;
    private PodcastMainViewAdapter adapter;
    private static final String TAG = MainPodcastFragment.class.getSimpleName();
    RecyclerView subscribed_podcast_recyclerview;
    ImageView search_podcast_btn;
    NavigationView nv_drawer;
    DrawerLayout drawerLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(getContext()));
        adapter = new PodcastMainViewAdapter(new ArrayList<>(), this);
        View root = inflater.inflate(R.layout.fragment_main_podcast, container, false);
        subscribed_podcast_recyclerview =
                (RecyclerView) root.findViewById(R.id.subscribed_podcast_recyclerview);
        search_podcast_btn = (ImageView) root.findViewById(R.id.search_podcast_btn);
        nv_drawer = (NavigationView) root.findViewById(R.id.navigation_pane);
        drawerLayout = (DrawerLayout) root.findViewById(R.id.drawer_layout);
        if (nv_drawer != null) {setupDrawerListener();}
        return root;
    }

    private void setupDrawerListener() {
        nv_drawer.setNavigationItemSelectedListener(
                item -> {
                    switch (item.getItemId()){
                        case R.id.drawer_playlist:
                            getContext().startActivity(new Intent(this.getContext(), MediaPlayerActivity.class));
                            break;
                        case R.id.drawer_search:
                            getContext().startActivity(new Intent(this.getContext(), SearchNewPodcastActivity.class));
                            break;
                        case R.id.drawer_downloads:
                            getContext().startActivity(new Intent(this.getContext(), DownloadsActivity.class));
                            break;
                    }
                    item.setChecked(false);
                    drawerLayout.closeDrawers();
                    return true;
                }
        );
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeRecyclerView();
        updateDataToAdapter();
        setSearchButtonOnClickListener();
        subscribeToPodcastSubject();
    }


    private Pair<Float, Float> getScreenSizes() {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        // assume this method will return correct float
        return new Pair(Float.valueOf(displayMetrics.widthPixels), Float.valueOf(displayMetrics.heightPixels));
    }


    private void initializeRecyclerView() {
        Pair<Float, Float> screenSize = getScreenSizes();
        float podSize = getContext().getResources().getDimension(R.dimen.pod_img_size);
        float numCol = Float.valueOf(screenSize.first/ podSize);
        GridLayoutManager lm = new GridLayoutManager(getContext(), (int) numCol);
        subscribed_podcast_recyclerview.setLayoutManager(lm);
        subscribed_podcast_recyclerview.setAdapter(adapter);
    }

    private void updateAdapterData(List<Podcast> newData) {
        adapter.setPodcastList(newData);
        adapter.notifyDataSetChanged();
    }


    private void updateDataToAdapter() {
        if (viewModel != null) {
            viewModel.getAllSubscribedPodcastObservable()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<Podcast>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(List<Podcast> podcasts) {
                            updateAdapterData(podcasts);
                            Log.d(TAG, "Successfully load  all podcast to main view");
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


    private void setSearchButtonOnClickListener() {
        search_podcast_btn.setOnClickListener(
                (view) -> {
                    Intent launchSearch = new Intent(getContext(),
                            SearchNewPodcastActivity.class);
                    getContext().startActivity(launchSearch);
                }
        );
    }

    private void subscribeToPodcastSubject() {

        PodcastViewModel.subscribePodcastSubject(new Observer<PodcastState>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(PodcastState podcastState) {
                switch (podcastState.getState()) {
                    // need to update the list
                    case PodcastState.UNSUBSCRIBED:
                    case PodcastState.SUBSCRIBED:
                        updateDataToAdapter();
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
