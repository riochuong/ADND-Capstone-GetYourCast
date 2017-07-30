package getyourcasts.jd.com.getyourcasts.view

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.view.touchListener.SwipeDetector
import kotlinx.android.synthetic.main.podcast_detail_layout.*

class PodcastDetailLayoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.podcast_detail_layout)
    }
}
