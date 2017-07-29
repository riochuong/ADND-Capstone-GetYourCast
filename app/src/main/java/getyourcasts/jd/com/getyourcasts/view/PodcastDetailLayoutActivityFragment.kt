package getyourcasts.jd.com.getyourcasts.view

import android.support.v4.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.repository.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Channel
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_podcast_detail_layout.*
/**
 * A placeholder fragment containing a simple view.
 */
class PodcastDetailLayoutActivityFragment : Fragment() {

    companion object {
        val PODCAST_STR = "podcast_key"
        val TAG = "PodcastDetail"
    }

    private lateinit var modelView: PodcastViewModel
    private lateinit var channelInfo: Channel

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        modelView = PodcastViewModel.getInstance(DataSourceRepo.getInstance(this.context))
        return inflater!!.inflate(R.layout.fragment_podcast_detail_layout, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        val podcast = getPodcastFromIntent()
        if (podcast != null){
            // load details info
            loadPodcastImage(podcast)
            loadRssDescription(podcast)
            podcast_detail_title.text = podcast.collectionName
            podcast_detail_artist.text = podcast.artistName
        }

    }

    fun loadRssDescription (pod: Podcast){
        if (pod.description != null){
            podcast_detail_desc.text = pod.description.trim()
        }
        else{
            modelView.getChannelFeedObservable(pod.feedUrl)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {
                                if ((it != null) && (it.channelDescription != null)) {
                                    // set description
                                    podcast_detail_desc.text = it.channelDescription.trim()
                                    // save channelInfo for later use
                                    channelInfo = it
                                }
                            },
                            {
                                it.printStackTrace()
                                Log.e(TAG,"Failed to fetch channel info")
                            }
                    )

        }
    }

    /**
     * load podcast from either local path or from http url
     */
    fun loadPodcastImage(pod: Podcast){
        if (pod.imgLocalPath != null){
            GlideApp.with(this.context).load(pod.imgLocalPath).into(podcast_detail_img)
        }else{
            GlideApp.with(this.context).load(pod.artworkUrl100).into(podcast_detail_img)
        }
    }

    /**
     * get podcast pass from intent
     */
    fun getPodcastFromIntent():Podcast? {
        val podcast = activity.intent.extras[PODCAST_STR]
        if (podcast is Podcast){
            return podcast
        }
        Log.e(TAG, "No Podcast pass to fragment! Something is really wrong here ")
        return null
    }


}
