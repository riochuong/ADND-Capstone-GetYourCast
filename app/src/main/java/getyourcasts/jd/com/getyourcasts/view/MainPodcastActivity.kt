package getyourcasts.jd.com.getyourcasts.view

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.exoplayer.MediaPlayBackService

import kotlinx.android.synthetic.main.activity_main_podcast.*

class MainPodcastActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_podcast)
        // start media player service here
        startService(Intent(this, MediaPlayBackService::class.java))
    }




    override fun onDestroy() {
        stopService(Intent(this,MediaPlayBackService::class.java))
        super.onDestroy()
    }
}
