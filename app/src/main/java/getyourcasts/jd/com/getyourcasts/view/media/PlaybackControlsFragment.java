package getyourcasts.jd.com.getyourcasts.view.media;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import getyourcasts.jd.com.getyourcasts.R;
import getyourcasts.jd.com.getyourcasts.exoplayer.MediaPlayBackService;
import getyourcasts.jd.com.getyourcasts.view.BaseActivity;


public class PlaybackControlsFragment extends Fragment  {

    RelativeLayout mainLayout;
    public PlaybackControlsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.playback_controls_fragment, container, false);
        mainLayout = (RelativeLayout) rootView.findViewById(R.id.playback_control_main_layout);
        setMainLayoutOnClick();
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BaseActivity baseActivity = (BaseActivity) getActivity();
    }

    public RelativeLayout getMainLayout () {
        return mainLayout;
    }

    /**
     * set main layout onlclick listener
     */
    private void setMainLayoutOnClick() {
        mainLayout.setOnClickListener(
                v -> {
                    this.getContext().startActivity(new Intent(this.getContext(), MediaPlayerActivity.class));
                }
        );
    }
}



