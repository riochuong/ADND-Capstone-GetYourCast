package getyourcasts.jd.com.getyourcasts.view.media;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import getyourcasts.jd.com.getyourcasts.R;

/**
 * Created by chuondao on 8/11/17.
 */

public class PlayListFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }
}
