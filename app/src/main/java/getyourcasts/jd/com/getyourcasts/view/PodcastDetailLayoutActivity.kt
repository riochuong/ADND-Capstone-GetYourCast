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
        podcast_detail_main_activity.setOnTouchListener(DetailSwipeDetector())

    }


    inner class DetailSwipeDetector : SwipeDetector() {
        override fun onSwipeRightToLeft(): Boolean {
            return false
        }

        override fun onSwipeLeftToRight(): Boolean {
            this@PodcastDetailLayoutActivity.onBackPressed()
            return true
        }

        override fun onSwipeUpward(): Boolean {
            return false
        }

        override fun onSwipeDownward(): Boolean {
            return false
        }
    }

}
