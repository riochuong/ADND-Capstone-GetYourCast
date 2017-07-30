package getyourcasts.jd.com.getyourcasts.view

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.FrameLayout
import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.repository.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Channel
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import getyourcasts.jd.com.getyourcasts.util.StorageUtil
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp
import getyourcasts.jd.com.getyourcasts.view.touchListener.SwipeDetector
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_podcast_detail_layout.*


/**
 * A placeholder fragment containing a simple view.
 */
class PodcastDetailLayoutActivityFragment : Fragment() {

    companion object {
        val PODCAST_KEY = "podcast_key"
        val ITEM_POS_KEY = "item_pos_key"
        val TAG = "PodcastDetail"
        val SUBSCRIBED_KEY = "subscribed"

    }

    private lateinit var viewModel: PodcastViewModel
    private lateinit var channelInfo: Channel
    private lateinit var podcast : Podcast
    private var subscribed : Boolean = false
    private var isFullScreen: Boolean = false
    private lateinit var itemSyncSubject : PublishSubject<Pair<Int, String>>

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        itemSyncSubject = PodcastViewModel.getItemSyncSubject()
        viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(this.context))
        // disable subscribe button until all views are ready
        return inflater!!.inflate(R.layout.fragment_podcast_detail_layout, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        subscribe_button.isEnabled = false
        startLoadingAnim()
        podcast = getPodcastFromIntent()!!
        pocast_detail_scroll_view.setOnTouchListener(DetailSwipeDetector())
        subscribed = if (podcast.description != null) true else false
        if (podcast != null) {
            // load details info
            loadPodcastImage(podcast)
            loadRssDescription(podcast)
            podcast_detail_title.text = podcast.collectionName
            podcast_detail_artist.text = podcast.artistName
            podcast_detail_total_episodes.text = podcast.trackCount.toString()
        }
        // enable swipe detector
        podcast_detail_main_fragment.setOnTouchListener(DetailSwipeDetector())

        // enable subscribe button
        subscribe_button.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                if (!subscribed){
                    // no suscription yet need to subscribed
                    viewModel.getSubscribeObservable(podcast, channelInfo)
                             .observeOn(AndroidSchedulers.mainThread())
                             .subscribe(
                                     {
                                         // TODO: Change fab logo here
                                         subscribed = true
                                         setSubscribeButtonImg()
                                         itemSyncSubject.onNext(Pair(getItemPosFromIntent(),podcast.collectionId))
                                     },
                                     {
                                         it.printStackTrace()
                                         Log.e(TAG,"Failed to subscribe podcast")
                                     }
                             )
                }
            }

        })
    }

    override fun onPause() {
        super.onPause()
        this.activity.setResult(Activity.RESULT_OK, getIntentResult())
    }

    private fun getIntentResult(): Intent {
        val result = Intent()
        result.putExtra(SUBSCRIBED_KEY,subscribed)
        return result
    }

    private fun changeFabColor (color :Int){
        subscribe_button.backgroundTintList = (ColorStateList.valueOf(color))
    }

    private fun setSubscribeButtonImg(){
        if (subscribed){
            changeFabColor(ContextCompat.getColor(this.context,R.color.fab_subscribed_color))
            subscribe_button.setImageResource(R.mipmap.ic_fab_done_white)
        }
        else{
           changeFabColor(ContextCompat.getColor(this.context,R.color.fab_tosubscribe_color))
            subscribe_button.setImageResource(R.mipmap.ic_tosubscribe)
        }
    }

    fun loadRssDescription(pod: Podcast) {
        if (subscribed && pod != null) {
            podcast_detail_desc.text = pod.description!!.trim()
            subscribe_button.isEnabled = true
            setSubscribeButtonImg()
            podcast_detail_main_fragment.visibility = View.VISIBLE
            loading_prog_view.visibility = View.GONE
            stopLoadingAnim()
        } else {
            subscribe_button.isEnabled = false
            viewModel.getChannelFeedObservable(pod.feedUrl)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {
                                if ((it != null) && (it.channelDescription != null)) {
                                    // set description
                                    podcast_detail_desc.text = it.channelDescription.trim()
                                    // save channelInfo for later use
                                    channelInfo = it
                                }
                                subscribe_button.isEnabled = true
                                setSubscribeButtonImg()
                                podcast_detail_main_fragment.visibility = View.VISIBLE
                                loading_prog_view.visibility = View.GONE
                                stopLoadingAnim()
                            },
                            {
                                it.printStackTrace()
                                Log.e(TAG, "Failed to fetch channel info")
                            }
                    )

        }
    }

    /**
     * load podcast from either local path or from http url
     */
    fun loadPodcastImage(pod: Podcast) {
        if (pod.imgLocalPath != null) {
            GlideApp.with(this.context).load(pod.imgLocalPath).into(podcast_detail_img)
        } else {
            GlideApp.with(this.context).load(pod.artworkUrl100).into(podcast_detail_img)
        }
    }

    /**
     * get podcast pass from intent
     */
    fun getPodcastFromIntent(): Podcast? {
        val podcast = activity.intent.extras[PODCAST_KEY]
        if (podcast is Podcast) {
            return podcast
        }
        Log.e(TAG, "No Podcast pass to fragment! Something is really wrong here ")
        return null
    }

    fun getItemPosFromIntent(): Int{
        val pos = activity.intent.extras[ITEM_POS_KEY]
        if (pos != null){
            return pos as Int
        }
        return -1
    }

    /**
     * Swipe detector
     */

    inner class DetailSwipeDetector : SwipeDetector() {
        val expand: Animation
        val MINIMIZE_SIZE = this@PodcastDetailLayoutActivityFragment.resources
                .getDimension(R.dimen.podcast_detail_minimize_size).toInt()
        val ANIM_DURATION : Long = 300

        init {
            expand = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                    if (interpolatedTime == 1f) {
                        pocast_detail_scroll_view.layoutParams.height =
                                FrameLayout.LayoutParams.MATCH_PARENT
                    } else {
                        val trans: Int = interpolatedTime.toInt() * MINIMIZE_SIZE
                        pocast_detail_scroll_view.layoutParams.height = if (trans <= MINIMIZE_SIZE)
                            MINIMIZE_SIZE else trans
                    }
                    pocast_detail_scroll_view.requestLayout()
                }

                override fun willChangeBounds(): Boolean {
                    return true
                }
            }


        }

        override fun onSwipeRightToLeft(): Boolean {
            return false
        }

        override fun onSwipeLeftToRight(): Boolean {
            this@PodcastDetailLayoutActivityFragment.activity.onBackPressed()
            return true
        }

        override fun onSwipeUpward(): Boolean {
//            pocast_detail_description_layout.layoutParams.height = MINIMIZE_SIZE
//            pocast_detail_description_layout.requestLayout()
            return false
        }

        override fun onSwipeDownward(): Boolean {
            if (!this@PodcastDetailLayoutActivityFragment.isFullScreen){
                expand.duration = ANIM_DURATION
                pocast_detail_scroll_view.startAnimation(expand)
                this@PodcastDetailLayoutActivityFragment.isFullScreen = true
                // allow scrollview to interncept
                pocast_detail_scroll_view.setOnTouchListener(null)
                return true
            }
            return false
        }
    }

    private fun startLoadingAnim() {
        loading_prog_view.show()
    }

    private fun stopLoadingAnim() {
        loading_prog_view.hide()
    }

}
