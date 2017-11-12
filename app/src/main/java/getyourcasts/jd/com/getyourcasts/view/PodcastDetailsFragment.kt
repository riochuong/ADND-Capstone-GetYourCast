package getyourcasts.jd.com.getyourcasts.view

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView

import com.wang.avi.AVLoadingIndicatorView

import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Channel
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import getyourcasts.jd.com.getyourcasts.repository.remote.network.NetworkHelper
import getyourcasts.jd.com.getyourcasts.util.StorageUtil
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp
import getyourcasts.jd.com.getyourcasts.view.touchListener.SwipeDetector
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastState
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

/**
 * Created by chuondao on 9/10/17.
 */

class PodcastDetailsFragment : Fragment() {

    private var viewModel: PodcastViewModel? = null
    private var channelInfo: Channel? = null
    private var podcast: Podcast? = null
    private var subscribed: Boolean? = false
    private var isFullScreen: Boolean? = false

    // UI Items
    lateinit var subscribe_button: FloatingActionButton
    lateinit var pocast_detail_scroll_view: ScrollView
    lateinit var podcast_detail_title: TextView
    lateinit var podcast_detail_artist: TextView
    lateinit var podcast_total_episodes: TextView
    lateinit var podcast_detail_desc: TextView
    lateinit var podcast_detail_main_fragment: CoordinatorLayout
    lateinit var loading_prog_view: AVLoadingIndicatorView
    lateinit var podcast_detail_img: ImageView

    /**
     * get podcast pass from intent
     */
    private val podcastFromIntent: Podcast?
        get() {
            val podcast = activity.intent.getParcelableExtra<Podcast>(PODCAST_KEY)
            if (podcast != null) {
                return podcast
            }
            Log.e(TAG, "No Podcast pass to fragment! Something is really wrong here ")
            return null
        }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(this.context))
        // disable subscribe button until all views are ready
        val root = inflater!!.inflate(R.layout.fragment_podcast_detail_layout, container, false)
        subscribe_button = root.findViewById(R.id.subscribe_button)
        pocast_detail_scroll_view = root.findViewById(R.id.pocast_detail_scroll_view)
        podcast_detail_title = root.findViewById(R.id.podcast_detail_title)
        podcast_detail_artist = root.findViewById(R.id.podcast_detail_artist)
        podcast_total_episodes = root.findViewById(R.id.podcast_total_episodes)
        podcast_detail_main_fragment = root.findViewById(R.id.podcast_detail_main_fragment)
        podcast_detail_desc = root.findViewById(R.id.podcast_detail_desc)
        loading_prog_view = root.findViewById(R.id.loading_prog_view)
        podcast_detail_img = root.findViewById(R.id.podcast_detail_img)
        return root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        subscribe_button.isEnabled = false
        startLoadingAnim()
        podcast = podcastFromIntent
        pocast_detail_scroll_view.setOnTouchListener(DetailSwipeDetector())
        subscribed = podcast!!.description != null

        // load all podcast details
        if (podcast != null) {
            // load details info
            loadPodcastImage(podcast!!)
            loadRssDescription(podcast)
            podcast_detail_title.text = podcast!!.collectionName
            podcast_detail_artist.text = podcast!!.artistName
            podcast_total_episodes.text = podcast!!.trackCount.toString() + " " + context.getString(R.string
                    .episode_str)
        }

        // enable swipe detector
        podcast_detail_main_fragment.setOnTouchListener(DetailSwipeDetector())

        // enable subscribe button
        subscribe_button.setOnClickListener { _ ->
            if (!NetworkHelper.isConnectedToNetwork(context)) {
                NetworkHelper.showNetworkErrorDialog(context)
                return@setOnClickListener
            }
            if (!subscribed!!) {
                // no suscription yet need to subscribed
                // download image icon
                viewModel!!.getSubscribeObservable(podcast, channelInfo)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                object : Observer<Boolean> {
                                    override fun onSubscribe(d: Disposable) {

                                    }

                                    override fun onNext(res: Boolean) {
                                        if (res!!) {
                                            viewModel!!.getPodcastObservable(podcast!!.collectionId)
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(object : Observer<Podcast> {
                                                        override fun onSubscribe(d: Disposable) {

                                                        }

                                                        override fun onNext(podcast: Podcast) {
                                                            this@PodcastDetailsFragment.podcast = podcast
                                                            this@PodcastDetailsFragment.subscribed = true
                                                            // change fab logo
                                                            setSubscribeButtonImg()

                                                            Log.d(TAG, "Successfully update podcast global " + "var")
                                                        }

                                                        override fun onError(e: Throwable) {
                                                            e.printStackTrace()
                                                        }

                                                        override fun onComplete() {

                                                        }
                                                    })
                                        }
                                    }

                                    override fun onError(e: Throwable) {
                                        e.printStackTrace()
                                        Log.e(TAG, "Failed to subscribe podcast")
                                    }

                                    override fun onComplete() {

                                    }
                                }
                        )
            } else {
                // now we need to start details list of all episodes
                val intent = Intent(context, EpisodeListActivity::class.java)
                intent.putExtra(PODCAST_KEY, podcast)
                context.startActivity(intent)
                Log.d(TAG, "Start Episode List Activity")
            }
        }
    }


    override fun onResume() {
        super.onResume()
        // check if podcast state changed
        if (podcast != null) {
            viewModel!!.getPodcastObservable(podcast!!.collectionId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            // On Next
                            {
                                subscribed = it.description.trim() != ""
                                setSubscribeButtonImg()
                            },
                            // On Error
                            {
                                it.printStackTrace()
                            }
                    )
        }
    }

    private fun changeFabColor(color: Int) {
        subscribe_button.backgroundTintList = ColorStateList.valueOf(color)
    }

    private fun setSubscribeButtonImg() {
        if (subscribed!!) {
            changeFabColor(ContextCompat.getColor(this.context, R.color.fab_subscribed_color))
            subscribe_button.setImageResource(R.mipmap.ic_show_episodes)
        } else {
            changeFabColor(ContextCompat.getColor(this.context, R.color.fab_tosubscribe_color))
            subscribe_button.setImageResource(R.mipmap.ic_tosubscribe)
        }
    }

    private fun loadRssDescription(pod: Podcast?) {
        if (subscribed!! && pod != null) {
            podcast_detail_desc.text = pod.description.trim { it <= ' ' }
            subscribe_button.isEnabled = true
            setSubscribeButtonImg()
            podcast_detail_main_fragment.visibility = View.VISIBLE
            loading_prog_view.visibility = View.GONE
            stopLoadingAnim()
        } else {
            subscribe_button.isEnabled = false
            viewModel!!.getChannelFeedObservable(pod!!.feedUrl)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            object : Observer<Channel> {
                                override fun onSubscribe(d: Disposable) {

                                }

                                override fun onNext(channel: Channel) {
                                    if (channel.channelDescription != null) {
                                        // set description
                                        this@PodcastDetailsFragment.podcast_detail_desc.text = channel
                                                .channelDescription.trim { it <= ' ' }
                                        // save channelInfo for later use
                                        this@PodcastDetailsFragment.channelInfo = channel
                                    }
                                    subscribe_button.isEnabled = true
                                    setSubscribeButtonImg()
                                    podcast_detail_main_fragment.visibility = View.VISIBLE
                                    loading_prog_view.visibility = View.GONE
                                    stopLoadingAnim()
                                }

                                override fun onError(e: Throwable) {
                                    e.printStackTrace()
                                }

                                override fun onComplete() {

                                }
                            }
                    )

        }
    }

    /**
     * load podcast from either local path or from http url
     */
    private fun loadPodcastImage(pod: Podcast) {
        if (pod.imgLocalPath != null) {
            GlideApp.with(this.context).load(pod.imgLocalPath).into(podcast_detail_img)
        } else {
            GlideApp.with(this.context).load(pod.artworkUrl100).into(podcast_detail_img)
        }
    }


    /**
     * Swipe detector
     */

    internal inner class DetailSwipeDetector : SwipeDetector() {
        var expand: Animation
        var MINIMIZE_SIZE = this@PodcastDetailsFragment.resources.getDimension(R.dimen
                .podcast_detail_minimize_size).toInt()
        var ANIM_DURATION: Long = 300

        init {

            expand = object : Animation() {
                override fun willChangeBounds(): Boolean {
                    return true
                }

                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                    if (interpolatedTime == 1f) {
                        pocast_detail_scroll_view.layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT
                    } else {
                        val trans = interpolatedTime.toInt() * MINIMIZE_SIZE
                        pocast_detail_scroll_view.layoutParams.height = if (trans <= MINIMIZE_SIZE) MINIMIZE_SIZE else trans
                    }
                    pocast_detail_scroll_view.requestLayout()
                }
            }

        }

        override fun onSwipeDownward(): Boolean {
            if (!this@PodcastDetailsFragment.isFullScreen!!) {
                expand.duration = ANIM_DURATION
                pocast_detail_scroll_view.startAnimation(expand)
                this@PodcastDetailsFragment.isFullScreen = true
                // allow scrollview to interncept
                return true
            }
            return false
        }

        override fun onSwipeRightToLeft(): Boolean {
            return false
        }

        override fun onSwipeLeftToRight(): Boolean {
            this@PodcastDetailsFragment.activity.onBackPressed()
            return true
        }

        override fun onSwipeUpward(): Boolean {
            return false
        }
    }

    private fun startLoadingAnim() {
        loading_prog_view.show()
    }

    private fun stopLoadingAnim() {
        loading_prog_view.hide()
    }

    companion object {

        val PODCAST_KEY = "podcast_key"
        val TAG = "PodcastDetail"
    }

}
