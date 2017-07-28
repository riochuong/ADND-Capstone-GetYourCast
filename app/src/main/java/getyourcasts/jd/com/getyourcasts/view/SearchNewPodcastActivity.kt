package getyourcasts.jd.com.getyourcasts.view

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel

class SearchNewPodcastActivity : AppCompatActivity() {

    private lateinit var  viewModel : PodcastViewModel


    companion object {
        val TAG = "GET_YOUR_CASTS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_podcast_activity)
    }

}
