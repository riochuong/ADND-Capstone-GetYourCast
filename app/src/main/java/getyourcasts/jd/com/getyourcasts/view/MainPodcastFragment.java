package getyourcasts.jd.com.getyourcasts.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
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
import getyourcasts.jd.com.getyourcasts.viewmodel.AllSubscribedPodcastLoader;
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastState;
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainPodcastFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Podcast>> {
    private PodcastMainViewAdapter adapter;
    private static final String TAG = MainPodcastFragment.class.getSimpleName();
    RecyclerView subscribed_podcast_recyclerview;
    ImageView search_podcast_btn;
    NavigationView nv_drawer;
    DrawerLayout drawerLayout;
    ImageView show_nv_pane_btn;
    LoaderManager loaderManager;

    private static final int MAIN_POD_CAST_LOADER = 384;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        adapter = new PodcastMainViewAdapter(new ArrayList<>(), this);
        View root = inflater.inflate(R.layout.fragment_main_podcast, container, false);
        subscribed_podcast_recyclerview =
                (RecyclerView) root.findViewById(R.id.subscribed_podcast_recyclerview);
        search_podcast_btn = (ImageView) root.findViewById(R.id.search_podcast_btn);
        nv_drawer = (NavigationView) root.findViewById(R.id.navigation_pane);
        drawerLayout = (DrawerLayout) root.findViewById(R.id.drawer_layout);
        show_nv_pane_btn = (ImageView) root.findViewById(R.id.show_nv_pane_btn);
        loaderManager = getActivity().getSupportLoaderManager();

        if (nv_drawer != null) {setupDrawerListener();}
        return root;
    }

    private void setupDrawerListener() {
        nv_drawer.setNavigationItemSelectedListener(
                item -> {
                    item.setChecked(false);
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
                    drawerLayout.closeDrawers();
                    return false;
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
        setOnClickListenerForShowingNvPane();

    }

    private void setOnClickListenerForShowingNvPane() {
        show_nv_pane_btn.setOnClickListener(
                view -> {
                    drawerLayout.openDrawer(Gravity.LEFT);
                }
        );
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
      loaderManager.initLoader(MAIN_POD_CAST_LOADER, new Bundle(),this).forceLoad();
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

    @Override
    public Loader<List<Podcast>> onCreateLoader(int id, Bundle args) {
        if (id != MAIN_POD_CAST_LOADER) {
            throw new IllegalArgumentException("Wrong loader id received...must be weird");
        }
        return new AllSubscribedPodcastLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Podcast>> loader, List<Podcast> podcasts) {
        updateAdapterData(podcasts);
    }

    @Override
    public void onLoaderReset(Loader<List<Podcast>> loader) {

    }
}
