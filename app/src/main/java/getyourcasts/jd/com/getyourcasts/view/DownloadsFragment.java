package getyourcasts.jd.com.getyourcasts.view;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import getyourcasts.jd.com.getyourcasts.R;
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast;
import getyourcasts.jd.com.getyourcasts.view.adapter.DownloadedEpisodesAdapter;
import getyourcasts.jd.com.getyourcasts.viewmodel.DownloadedEpisodesLoader;
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * A placeholder fragment containing a simple view.
 */
public class DownloadsFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Episode>> {

    public DownloadsFragment() {
    }

    RecyclerView downloaded_eps_recycler_view;
    DownloadedEpisodesAdapter adapter;
    LoaderManager loaderManager;

    private static final int DOWNLOAD_LOADER_ID = 929;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_downloads, container, false);
        downloaded_eps_recycler_view = (RecyclerView) root.findViewById(R.id.downloads_recycler_view);
        adapter = new DownloadedEpisodesAdapter(new ArrayList<>(), this);
        loaderManager = getActivity().getSupportLoaderManager();
        return root;
    }

    private void setUpRecyclerView () {
        LinearLayoutManager lm = new LinearLayoutManager(this.getContext());
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        downloaded_eps_recycler_view.setLayoutManager(lm);
        downloaded_eps_recycler_view.setAdapter(adapter);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        setUpRecyclerView();
        loaderManager.initLoader(DOWNLOAD_LOADER_ID,new Bundle(),this).forceLoad();
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public Loader<List<Episode>> onCreateLoader(int id, Bundle args) {
        if (id != DOWNLOAD_LOADER_ID){
            throw new IllegalArgumentException("Wrong loader id received...must be weird");
        }

        return new DownloadedEpisodesLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Episode>> loader, List<Episode> episodeList) {
        adapter.updateEpisodeList(episodeList);
    }

    @Override
    public void onLoaderReset(Loader<List<Episode>> loader) {

    }
}
