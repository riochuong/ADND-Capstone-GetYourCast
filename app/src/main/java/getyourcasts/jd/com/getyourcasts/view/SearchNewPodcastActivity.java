package getyourcasts.jd.com.getyourcasts.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import getyourcasts.jd.com.getyourcasts.R;

/**
 * Created by chuondao on 9/10/17.
 */

public class SearchNewPodcastActivity  extends AppCompatActivity {

    private static final String TAG = "GET_YOUR_CASTS";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_podcast_activity);
    }

}
