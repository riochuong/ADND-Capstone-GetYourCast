package getyourcasts.jd.com.getyourcasts.view;

import android.content.Intent;
import android.os.Bundle;

import getyourcasts.jd.com.getyourcasts.R;
import getyourcasts.jd.com.getyourcasts.exoplayer.MediaPlayBackService;
import getyourcasts.jd.com.getyourcasts.repository.remote.network.DownloadService;
import getyourcasts.jd.com.getyourcasts.update.UpdateUtilities;

/**
 * Created by chuondao on 9/10/17.
 */

public class MainPodcastActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_podcast);
        // start media player service here
        startService(new Intent(this, MediaPlayBackService.class));
        startService(new Intent(this, DownloadService.class));
        UpdateUtilities.scheduleUpdateTask(this);
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, MediaPlayBackService.class));
        stopService(new Intent(this, DownloadService.class));
        super.onDestroy();
    }
}