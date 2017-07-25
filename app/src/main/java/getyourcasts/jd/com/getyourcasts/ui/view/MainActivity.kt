package getyourcasts.jd.com.getyourcasts.ui.view

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.repository.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel
import io.reactivex.android.schedulers.AndroidSchedulers

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var  viewModel : PodcastViewModel

    companion object {
        val TAG = "GET_YOUR_CASTS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        viewModel = PodcastViewModel(DataSourceRepo(this))

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()

            viewModel.getPodcastSearchObservable("Elvis")
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {
                              val name = it.collectionName
                              val data = it.toString()
                              Log.d(TAG,"$name \n $data")
                              viewModel.fetchPodcastEpisode(it.feedUrl)
                                      .observeOn(AndroidSchedulers.mainThread())
                                      .subscribe(
                                              {
                                                  val title = it.title
                                                  val mediaInfo = it.mediaInfo
                                                  if (mediaInfo != null){
                                                      Log.d(TAG, "url : ${mediaInfo.url}")
                                                  }
                                                  else{
                                                      Log.e(TAG, "${title} has no track ")
                                                  }

                                              },

                                              {
                                                  Log.e(TAG,it.message)
                                                  it.printStackTrace()
                                              } // on error
                                      )

                            }, // on next
                            {
                               it.printStackTrace()
                               Log.e(TAG,"")
                            }  // on error
                    )
        }

    }

}
