package getyourcasts.jd.com.getyourcasts.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel

class SearchNewPodcastActivity : AppCompatActivity() {


    companion object {
        val TAG = "GET_YOUR_CASTS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_podcast_activity)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode){
            Activity.RESULT_OK -> {

            }
        }
    }
}
