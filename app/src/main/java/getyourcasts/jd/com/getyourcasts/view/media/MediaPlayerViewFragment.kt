package getyourcasts.jd.com.getyourcasts.view.media

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.Fragment
import android.util.Log
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlaybackControlView
import com.google.android.exoplayer2.ui.SimpleExoPlayerView

import butterknife.BindView
import com.github.florent37.glidepalette.BitmapPalette
import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.exoplayer.MediaPlayBackService
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import getyourcasts.jd.com.getyourcasts.util.GlideUtil
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

/**
 * Created by chuondao on 8/11/17.
 */

class MediaPlayerViewFragment : Fragment() {
    @BindView(R.id.simple_exo_video_view)
    internal lateinit var playerView: SimpleExoPlayerView
    internal lateinit var viewModel: PodcastViewModel
    internal var exoShutter: ImageView? = null
    internal var videoSurfaceView: AspectRatioFrameLayout? = null
    private var currentEpisode: Episode? = null
    private var mainLayout: LinearLayout? = null
    private var mediaServiceDisposable: Disposable? = null
    private var podcastId: String? = null
    private var isVideo = false
    private var episodeTitle: TextView? = null
    private lateinit var emptyView: TextView
    private var close_icon: ImageView? = null
    private lateinit var controlView: PlaybackControlView
    private lateinit var mediaNextBtn: ImageButton
    private lateinit var mediaPrevBtn: ImageButton

    private val currentOrientation: Int
        get() = context.resources.configuration.orientation

    /* ============================ CONNECT TO MEDIA SERVICE ========================================= */
    private var boundToMediaService = false
    private var mediaService: MediaPlayBackService? = null

    // connection to service
    private val mediaServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            boundToMediaService = true
            mediaService = (service as MediaPlayBackService.MediaPlayBackServiceBinder).service
            mediaService!!.setPlayerView(playerView)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            boundToMediaService = false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(context))
        // if we need to load some saved data
        if (savedInstanceState != null) {
            loadInstanceState(savedInstanceState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        savePlayerState(outState)
        super.onSaveInstanceState(outState)
    }

    /**
     * Save the state of the playback
     * @param bundle
     */
    private fun savePlayerState(bundle: Bundle?) {
        if (currentEpisode != null && podcastId != null) {
            bundle!!.putString(PODCAST_ID_KEY, podcastId)
            bundle.putParcelable(CURR_EP_KEY, currentEpisode)
            bundle.putBoolean(IS_VIDEO_KEY, isVideo)
        }
    }

    private fun loadInstanceState(bundle: Bundle) {
        val podcastId = bundle.getString(PODCAST_ID_KEY, null)
        if (podcastId != null) {
            this.podcastId = podcastId
            currentEpisode = bundle.getParcelable(CURR_EP_KEY)
            this.isVideo = bundle.getBoolean(IS_VIDEO_KEY)
        }

    }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // check configuration
        var root: View? = null
        val orientation = currentOrientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE && isVideo) {
            Log.d(TAG, "In Landscape mode")
            root = inflater!!.inflate(R.layout.fragment_media_player_view_horizontal_video, container, false)
            playerView = root!!.findViewById(R.id.simple_exo_video_view)
        } else { // IN PORTRAIT MODE && not landscape video
            root = inflater!!.inflate(R.layout.fragment_media_player_view_vertical, container, false)
            playerView = root!!.findViewById(R.id.simple_exo_video_view)
            close_icon = root.findViewById(R.id.close_icon)
            // set X icon to go back to previous screen
            if (close_icon != null) {
                close_icon!!.setOnClickListener { activity.onBackPressed() }
            }
        }

        // find common features
        videoSurfaceView = playerView.findViewById(R.id.exo_content_frame)
        mainLayout = root.findViewById(R.id.media_player_view_main_layout)
        // some of the below might be null
        exoShutter = playerView.findViewById(R.id.exo_shutter)
        episodeTitle = playerView.findViewById(R.id.media_player_view_episode_title)
        emptyView = root.findViewById(R.id.media_player_empty_view)
        controlView = playerView.findViewById(R.id.exo_controller)
        mediaNextBtn = controlView.findViewById(R.id.playback_exo_next)
        mediaPrevBtn = controlView.findViewById(R.id.playback_exo_prev)
        // set this to enable text marquee
        if (episodeTitle != null) {
            episodeTitle!!.isSelected = true
        }

        return root
    }


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // set on click listener for the custom next / prev btn
        mediaNextBtn.setOnClickListener {
            if (mediaService != null && boundToMediaService) {
                mediaService!!.playNextSongInPlaylist()
            }
        }

        mediaPrevBtn.setOnClickListener {
            if (mediaService != null && boundToMediaService) {
                mediaService!!.playPreviousSongInPlaylist()
            }
        }
    }

    private fun initMediaServiceSubscribe() {
        MediaPlayBackService.subscribeMediaPlaybackSubject(object : Observer<Pair<Episode?, Int>> {
            override fun onSubscribe(d: Disposable) {
                mediaServiceDisposable = d
            }

            override fun onNext(info: Pair<Episode?, Int>) {
                val state = info.second
                when (state) {
                    MediaPlayBackService.MEDIA_STOPPED,
                    MediaPlayBackService.MEDIA_PAUSE,
                    MediaPlayBackService.MEDIA_PLAYING -> {
                        if (currentEpisode == null || currentEpisode!!.uniqueId != info.first?.uniqueId ?:false ) {
                            reloadCorrectDataForFragment(info)
                        }
                    }
                    MediaPlayBackService.MEDIA_ADDED_TO_PLAYLIST, MediaPlayBackService.MEDIA_TRACK_CHANGED -> reloadCorrectDataForFragment(info)
                    MediaPlayBackService.MEDIA_PLAYLIST_EMPTY -> enablePlayerView(false)
                }

            }

            override fun onError(e: Throwable) {

            }

            override fun onComplete() {

            }
        })
    }

    private fun enablePlayerView(enable: Boolean) {
        if (!enable) {
            playerView.visibility = View.GONE
            // show empty view
            emptyView.visibility = View.VISIBLE
        } else {
            playerView.visibility = View.VISIBLE
            // show empty view
            emptyView.visibility = View.GONE
        }
    }

    private fun reloadCorrectDataForFragment(info: Pair<Episode?, Int>) {
        if (info.first == null ){
            return
        }
        currentEpisode = info.first
        podcastId = info.first!!.podcastId
        enablePlayerView(true)
        // load album podcast image to the view
        isVideo = !info.first!!.type.contains("audio")
        // load image
        loadImgViewForPodcast(info.first!!.podcastId, isVideo)
        if (mediaService != null) {
            mediaService!!.setPlayerView(playerView)
        }
    }


    private fun loadImgViewForPodcast(podcastId: String, isVideo: Boolean) {
        try {
            viewModel.getPodcastObservable(podcastId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            object : Observer<Podcast> {
                                override fun onSubscribe(d: Disposable) {

                                }

                                override fun onNext(podcast: Podcast) {
                                    try {
                                        if (!isVideo) {
                                            GlideUtil.loadImageAndSetColorOfViews(
                                                    this@MediaPlayerViewFragment.activity.applicationContext,
                                                    podcast.imgLocalPath,
                                                    exoShutter!!,
                                                    mainLayout!!,
                                                    BitmapPalette.Profile.VIBRANT_DARK
                                            )
                                            if (videoSurfaceView != null && exoShutter != null) {
                                                videoSurfaceView!!.visibility = View.GONE
                                                exoShutter!!.visibility = View.VISIBLE
                                            }
                                            initControllerView(false)
                                        } else {
                                            videoSurfaceView!!.visibility = View.VISIBLE
                                            if (exoShutter != null) {
                                                exoShutter!!.visibility = View.GONE
                                            }
                                            initControllerView(true)
                                        }
                                        if (episodeTitle != null) {
                                            episodeTitle!!.text = currentEpisode!!.title
                                        }

                                    } catch (e: NumberFormatException) {
                                        e.printStackTrace()
                                    }

                                }

                                override fun onError(e: Throwable) {
                                    e.printStackTrace()
                                }

                                override fun onComplete() {

                                }
                            }
                    )
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun initControllerView(autohide: Boolean) {
        playerView.showController()
        if (!autohide) {
            playerView.controllerHideOnTouch = false
            playerView.controllerShowTimeoutMs = -1
        } else {
            playerView.controllerHideOnTouch = true
            playerView.controllerShowTimeoutMs = CONTROLLER_TIMEOUT
        }

    }


    override fun onResume() {
        super.onResume()
        bindMediaService()
        initMediaServiceSubscribe()
        if (currentEpisode != null && podcastId != null) {
            loadImgViewForPodcast(podcastId!!, isVideo)
            if (episodeTitle != null) {
                episodeTitle!!.text = currentEpisode!!.title
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (mediaService != null) {
            context.unbindService(mediaServiceConnection)
            mediaService = null
        }

        if (mediaServiceDisposable != null) {
            mediaServiceDisposable!!.dispose()
            mediaServiceDisposable = null
        }
    }


    private fun bindMediaService() {
        val intent = Intent(this.context, MediaPlayBackService::class.java)
        this.context.bindService(intent, mediaServiceConnection, Context.BIND_AUTO_CREATE)
    }

    companion object {

        private val CONTROLLER_TIMEOUT = 1000
        private val TAG = MediaPlayerViewFragment::class.java.simpleName

        /* Keys for saving bundle state */
        private val PODCAST_ID_KEY = "podcast_id_key"
        private val IS_VIDEO_KEY = "is_video_key"
        private val CURR_EP_KEY = "curr_ep_key"


        fun newInstance(): MediaPlayerViewFragment {
            return MediaPlayerViewFragment()
        }
    }

}
