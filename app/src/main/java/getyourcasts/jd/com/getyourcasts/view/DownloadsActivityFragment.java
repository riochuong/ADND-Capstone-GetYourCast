package getyourcasts.jd.com.getyourcasts.view;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import getyourcasts.jd.com.getyourcasts.R;
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast;
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel;

/**
 * A placeholder fragment containing a simple view.
 */
public class DownloadsActivityFragment extends Fragment {

    public DownloadsActivityFragment() {
    }

    RecyclerView downloaded_eps_recycler_view;
    PodcastViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_downloads, container, false);
        downloaded_eps_recycler_view = (RecyclerView) root.findViewById(R.id.downloads_recycler_view);
        viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(this.getContext()));
        return root;
    }


}
