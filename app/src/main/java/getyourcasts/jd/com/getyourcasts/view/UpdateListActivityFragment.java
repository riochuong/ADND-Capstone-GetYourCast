package getyourcasts.jd.com.getyourcasts.view;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import getyourcasts.jd.com.getyourcasts.R;
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast;
import getyourcasts.jd.com.getyourcasts.update.UpdateInfo;
import getyourcasts.jd.com.getyourcasts.view.adapter.UpateEpisodeListAdapter;
import getyourcasts.jd.com.getyourcasts.viewmodel.NewUpdateLoader;
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * A placeholder fragment containing a simple view.
 */
public class UpdateListActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Map<Podcast,List<Episode>>> {

    RecyclerView listUpdateRecyclerView;
    UpateEpisodeListAdapter adapter;
    PodcastViewModel viewModel;
    Disposable d;
    LoaderManager loaderManager;

    private static final int UPDATE_LOADER_ID = 387;

    static final String UPDATE_ITEM_KEY = "eps_update_key";
    public UpdateListActivityFragment() {
    }

    @Override
    public void onPause() {
        super.onPause();
        if (d != null) {
            d.dispose();
            d = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_update_list, container, false);
        viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(this.getContext()));
        listUpdateRecyclerView = (RecyclerView) rootView.findViewById(R.id.update_ep_recyclerview);
        adapter = new UpateEpisodeListAdapter(new HashMap<>(), this.getContext());
        LinearLayoutManager lm = new LinearLayoutManager(this.getContext());
        lm.setOrientation(LinearLayout.VERTICAL);
        listUpdateRecyclerView.setLayoutManager(lm);
        listUpdateRecyclerView.setAdapter(adapter);
        loaderManager = getActivity().getSupportLoaderManager();
        // get new update from db
        getUpdateFromDb();
        return rootView;
    }



    private void getUpdateFromDb(){
       loaderManager.initLoader(UPDATE_LOADER_ID,new Bundle(), this).forceLoad();
    }


    @Override
    public Loader<Map<Podcast, List<Episode>>> onCreateLoader(int id, Bundle args) {
        if (id != UPDATE_LOADER_ID) {
            throw new IllegalArgumentException("Wrong loader id received...must be weird");
        }
        return new NewUpdateLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Map<Podcast, List<Episode>>> loader, Map<Podcast, List<Episode>> podcastListMap) {
        adapter.setEpUpdateMap(podcastListMap);
    }

    @Override
    public void onLoaderReset(Loader<Map<Podcast, List<Episode>>> loader) {

    }
}
