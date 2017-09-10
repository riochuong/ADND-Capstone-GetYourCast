package getyourcasts.jd.com.getyourcasts.view;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
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
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * A placeholder fragment containing a simple view.
 */
public class DownloadsFragment extends Fragment {

    public DownloadsFragment() {
    }

    RecyclerView downloaded_eps_recycler_view;
    PodcastViewModel viewModel;
    DownloadedEpisodesAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_downloads, container, false);
        downloaded_eps_recycler_view = (RecyclerView) root.findViewById(R.id.downloads_recycler_view);
        viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(this.getContext()));
        adapter = new DownloadedEpisodesAdapter(new ArrayList<>(), this);
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
        viewModel.getDownloadedEpisodes().observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Observer<List<Episode>>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(List<Episode> episodeList) {
                                adapter.updateEpisodeList(episodeList);
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

        super.onViewCreated(view, savedInstanceState);
    }
}
