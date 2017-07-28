package getyourcasts.jd.com.getyourcasts.view;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import getyourcasts.jd.com.getyourcasts.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class PodcastDetailLayoutActivityFragment extends Fragment {

    public PodcastDetailLayoutActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_podcast_detail_layout, container, false);
    }
}
