package getyourcasts.jd.com.getyourcasts.view

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.repository.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.viewmodel.SearchPodcastViewModel

import kotlinx.android.synthetic.main.search_podcast_activity.*

class SearchNewPodcastActivity : AppCompatActivity() {

    private lateinit var  viewModel : SearchPodcastViewModel


    companion object {
        val TAG = "GET_YOUR_CASTS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_podcast_activity)
    }

}
