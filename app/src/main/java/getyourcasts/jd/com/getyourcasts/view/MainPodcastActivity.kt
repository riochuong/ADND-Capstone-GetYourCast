package getyourcasts.jd.com.getyourcasts.view

import android.content.Intent
import android.os.Bundle

import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.exoplayer.MediaPlayBackService
import getyourcasts.jd.com.getyourcasts.repository.remote.network.DownloadService
import getyourcasts.jd.com.getyourcasts.update.UpdateUtilities

/**
 * Created by chuondao on 9/10/17.
 */

class MainPodcastActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_podcast)
        // start media player service here
        startService(Intent(this, MediaPlayBackService::class.java))
        startService(Intent(this, DownloadService::class.java))
        UpdateUtilities.scheduleUpdateTask(this)
    }

    override fun onDestroy() {
        stopService(Intent(this, MediaPlayBackService::class.java))
        stopService(Intent(this, DownloadService::class.java))
        super.onDestroy()
    }

}