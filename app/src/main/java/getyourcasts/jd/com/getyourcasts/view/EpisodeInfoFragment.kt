package getyourcasts.jd.com.getyourcasts.view

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.IBinder
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView

import com.tonyodev.fetch.listener.FetchListener
import com.wang.avi.AVLoadingIndicatorView

import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.exoplayer.MediaPlayBackService
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.repository.remote.network.DownloadService
import getyourcasts.jd.com.getyourcasts.util.StorageUtil
import getyourcasts.jd.com.getyourcasts.util.TimeUtil
import getyourcasts.jd.com.getyourcasts.view.adapter.EpisodesRecyclerViewAdapter
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp
import getyourcasts.jd.com.getyourcasts.view.media.MediaPlayerActivity
import getyourcasts.jd.com.getyourcasts.view.touchListener.SwipeDetector
import getyourcasts.jd.com.getyourcasts.viewmodel.EpisodeState
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable


/**
 * A placeholder fragment containing a simple view.
 */
class EpisodeInfoFragment : Fragment() {

    private var currInfoEpisode: Episode? = null
    private var bgColor = 0
    private var imgUrl: String? = null
    private var datePub: TimeUtil.DatePub? = null
    private var viewModel: PodcastViewModel? = null
    private var fabState = PRESS_TO_DOWNLOAD
    private val downloadListener: FetchListener? = null
    private var transactionId = -1L
    private var mainObserverDisposable: Disposable? = null
    private var mediaServiceDisposable: Disposable? = null

    // UI items
    private lateinit var ep_info_title: TextView
    private lateinit var ep_info_img: ImageView
    private lateinit var ep_info_app_bar: AppBarLayout
    private lateinit var ep_info_desc: TextView
    private lateinit var ep_info_release: TextView
    private lateinit var ep_info_media_info: TextView
    private lateinit var episode_info_main_layout: CoordinatorLayout
    private lateinit var episode_info_scroll_view: ScrollView
    internal lateinit var ep_info_fab: FloatingActionButton
    internal lateinit var add_to_playlist: ImageView
    private lateinit var episode_info_loading_anim: AVLoadingIndicatorView

    /* ============================ CONNECT TO DOWNLOAD SERVICE ========================================= */
    private var boundToDownload = false
    private var downloadService: DownloadService? = null

    // connection to service
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            boundToDownload = true
            downloadService = (service as DownloadService.DownloadServiceBinder).service
        }

        override fun onServiceDisconnected(name: ComponentName) {
            boundToDownload = false
        }
    }


    /* ============================ CONNECT TO MEDIA SERVICE ========================================= */
    private var boundToMediaService = false
    private var mediaService: MediaPlayBackService? = null

    // connection to service
    private val mediaServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            boundToMediaService = true
            mediaService = (service as MediaPlayBackService.MediaPlayBackServiceBinder).service
            initAddToPlayListListener()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            boundToMediaService = false
        }
    }


    /**
     * retreived episode passed from the fragment list
     */
    private val episodeFromIntent: Episode
        get()

        = activity.intent.getParcelableExtra(EpisodesRecyclerViewAdapter.EPISODE_KEY)

    /**
     * retreive back ground color
     */
    private val bgColorFromIntent: Int
        get()

        = activity.intent.getIntExtra(EpisodesRecyclerViewAdapter.BG_COLOR_KEY, 0)

    private val downloadTransId: Long
        get() = activity.intent.getLongExtra(EpisodesRecyclerViewAdapter.DL_TRANS_ID, -1)

    private val imageUrlFromIntent: String
        get()

        = this.activity.intent.getStringExtra(EpisodesRecyclerViewAdapter.PODCAST_IMG_KEY)

    private val isDownloadingFromIntent: Boolean
        get()

        = this.activity.intent.getBooleanExtra(
                EpisodesRecyclerViewAdapter.IS_DOWNLOADING_KEY, false)

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        currInfoEpisode = episodeFromIntent
        bgColor = bgColorFromIntent
        imgUrl = imageUrlFromIntent
        viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(this.context))
        transactionId = downloadTransId
        if (currInfoEpisode!!.pubDate != null) {
            datePub = TimeUtil.parseDatePub(currInfoEpisode!!.pubDate)
        }
        val root = inflater!!.inflate(R.layout.fragment_episode_info, container, false)
        ep_info_title = root.findViewById<TextView>(R.id.ep_info_title)
        ep_info_app_bar = root.findViewById<AppBarLayout>(R.id.ep_info_app_bar)
        ep_info_desc = root.findViewById<TextView>(R.id.ep_info_desc)
        ep_info_release = root.findViewById<TextView>(R.id.ep_info_release)
        ep_info_media_info = root.findViewById<TextView>(R.id.ep_info_media_info)
        episode_info_main_layout = root.findViewById<CoordinatorLayout>(R.id.episode_info_main_layout)
        episode_info_scroll_view = root.findViewById(R.id.episode_info_scroll_view)
        ep_info_fab = root.findViewById(R.id.ep_info_fab)
        add_to_playlist = root.findViewById(R.id.add_to_playlist)
        episode_info_loading_anim = root.findViewById(R.id.episode_info_loading_anim)
        ep_info_img = root.findViewById(R.id.ep_info_img)
        return root
    }


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startAnim()
        initViews()
    }


    private fun initViews() {
        // load title
        ep_info_title.text = currInfoEpisode!!.title

        // load image view
        loadEpisodeImage()

        // change bg color
        ep_info_app_bar.setBackgroundColor(bgColor)

        // load more infos
        ep_info_desc.text = currInfoEpisode!!.description
        ep_info_release.text = currInfoEpisode!!.pubDate

        // published date might not be available
        if (datePub != null) {
            ep_info_release.text = String.format(DATE_PUB_FORMAT, datePub!!.month, datePub!!.dayOfMonth, datePub!!
                    .year)
        }

        if (currInfoEpisode!!.fileSize != null) {
            ep_info_media_info.text = String.format(MEDIA_INFO_FORMAT, StorageUtil.convertToMbRep(currInfoEpisode!!.fileSize))
        }

        setFabButtonOnClickListener()

        // init fab
        adjustFabState()

        // add swipe detector to scroll views
        episode_info_main_layout.setOnTouchListener(SimpleSwipeDetector())
        episode_info_scroll_view.setOnTouchListener(SimpleSwipeDetector())

        // enable main view
        stopAnim()
    }


    private fun subscribeToMediaServiceSubject() {

        MediaPlayBackService.subscribeMediaPlaybackSubject(
                object : Observer<Pair<Episode?, Int>> {
                    override fun onSubscribe(d: Disposable) {
                        mediaServiceDisposable = d
                    }

                    override fun onNext(t: Pair<Episode?, Int>) {
                        val episode = t.first
                        val state = t.second
                        if (episode != null && currInfoEpisode!!.uniqueId == episode.uniqueId) {
                            // check which state we should set the fab
                            when (state) {
                            // only need to change if this is already playing
                                MediaPlayBackService.MEDIA_PLAYING -> {
                                    fabState = PRESS_TO_PAUSE
                                    ep_info_fab.setImageResource(R.mipmap.ic_pause)
                                }

                                MediaPlayBackService.MEDIA_STOPPED -> {
                                    fabState = PRESS_TO_PLAY
                                    ep_info_fab.setImageResource(R.mipmap.ic_play_white)
                                }

                                MediaPlayBackService.MEDIA_PAUSE -> {
                                    fabState = PRESS_TO_UNPAUSE
                                    ep_info_fab.setImageResource(R.mipmap.ic_play_white)
                                }

                            // if episode removed from playlist we can allow it to be added back
                                MediaPlayBackService.MEDIA_REMOVED_FROM_PLAYLIST ->
                                    // need to update the list icon
                                    add_to_playlist.setImageResource(R.mipmap.ic_add_to_play_list)
                            }

                        } else {
                            if (fabState != PRESS_TO_DOWNLOAD && fabState != PRESS_TO_PLAY) {
                                fabState = PRESS_TO_PLAY
                                ep_info_fab.setImageResource(R.mipmap.ic_play_white)
                            }
                        }
                    }

                    override fun onError(e: Throwable) {
                        Log.e(TAG, "Error during receiving media playback service info ")
                        e.printStackTrace()
                    }

                    override fun onComplete() {

                    }
                }
        )
    }

    private fun subscribeToEpisodeSubject(ep: Episode?) {

        PodcastViewModel.subscribeEpisodeSubject(object : Observer<EpisodeState> {
            override fun onSubscribe(d: Disposable) {
                mainObserverDisposable = d
            }

            override fun onNext(epState: EpisodeState) {
                if (epState.uniqueId == ep!!.episodeUniqueKey) {
                    // state changes..update episode
                    viewModel!!.getEpisodeObsevable(ep)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    object : Observer<Episode> {
                                        override fun onSubscribe(d: Disposable) {

                                        }

                                        override fun onNext(episode: Episode) {
                                            currInfoEpisode = episode
                                            when (epState.state) {
                                                EpisodeState.DOWNLOADING -> {
                                                    fabState = PRESS_TO_STOP_DOWNLOAD
                                                    ep_info_fab.visibility = View.VISIBLE
                                                    ep_info_fab.setImageResource(R.mipmap.ic_stop_white)
                                                }
                                                EpisodeState.FETCHED -> {
                                                    fabState = PRESS_TO_DOWNLOAD
                                                    ep_info_fab.visibility = View.VISIBLE
                                                    ep_info_fab.setImageResource(R.mipmap.ic_fab_tosubscribe)
                                                    currInfoEpisode!!.localUrl = null
                                                }

                                                EpisodeState.DOWNLOADED -> {
                                                    fabState = PRESS_TO_PLAY
                                                    ep_info_fab.visibility = View.VISIBLE
                                                    ep_info_fab.setImageResource(R.mipmap.ic_play_white)
                                                    add_to_playlist.visibility = View.VISIBLE
                                                }
                                            }
                                        }

                                        override fun onError(e: Throwable) {
                                            e.printStackTrace()
                                        }

                                        override fun onComplete() {

                                        }
                                    }
                            )
                    // on Next

                }
            }

            override fun onError(e: Throwable) {

            }

            override fun onComplete() {
                ep_info_fab.visibility = View.VISIBLE
                ep_info_fab.setImageResource(R.mipmap.ic_play_white)
            }
        })
    }


    private fun initAddToPlayListListener() {
        // first initialize add to playlist icon
        // if already in the list then we should not show the add icon
        if (mediaService != null && mediaService!!.isEpisodeInPlayList(currInfoEpisode!!.uniqueId)) {
            add_to_playlist.setImageResource(R.mipmap.ic_already_add_to_playlist)
        } else {
            add_to_playlist.setImageResource(R.mipmap.ic_add_to_play_list)
        }

        add_to_playlist.setOnClickListener { view ->
            // ADD song to play list
            Log.d(TAG, "ADD TO PLAYLIST !!! ")
            if (mediaService != null && !mediaService!!.isEpisodeInPlayList(currInfoEpisode!!.uniqueId)) {
                mediaService!!.addTrackToEndPlaylist(currInfoEpisode!!)
                add_to_playlist.setImageResource(R.mipmap.ic_already_add_to_playlist)
            }
        }
    }

    /**
     * helper to initialize FAB button
     */
    private fun adjustFabState() {
        // change fab color to red always
        changeFabColor(ContextCompat.getColor(this.context, R.color.unfin_color))
        when {
            // downloading
            isDownloadingFromIntent -> {
                fabState = PRESS_TO_STOP_DOWNLOAD
                ep_info_fab.visibility = View.INVISIBLE
                ep_info_fab.setImageResource(R.mipmap.ic_stop_dl)
            }
            // downloaded
            currInfoEpisode!!.downloaded == EpisodeState.DOWNLOADED -> {
                fabState = PRESS_TO_PLAY
                ep_info_fab.visibility = View.VISIBLE
                ep_info_fab.setImageResource(R.mipmap.ic_play_white)
                add_to_playlist.visibility = View.VISIBLE
            }
            // press to download
            else -> {
                fabState = PRESS_TO_DOWNLOAD
                ep_info_fab.visibility = View.VISIBLE
                ep_info_fab.setImageResource(R.mipmap.ic_fab_tosubscribe)
            }
        }
    }


    // start loading animation
    private fun startAnim() {
        episode_info_loading_anim.show()
        episode_info_loading_anim.visibility = View.VISIBLE
        episode_info_main_layout.visibility = View.INVISIBLE
    }

    // stop loading animation and show main screen
    private fun stopAnim() {
        episode_info_loading_anim.show()
        episode_info_loading_anim.visibility = View.GONE
        episode_info_main_layout.visibility = View.VISIBLE
    }

    private fun changeFabColor(color: Int) {
        ep_info_fab.backgroundTintList = ColorStateList.valueOf(color)
    }

    private fun setFabButtonOnClickListener() {
        // set on click listener
        ep_info_fab.setOnClickListener { _ ->
            when (fabState) {
                PRESS_TO_DOWNLOAD -> {
                    // check if episode is already state or not
                    // bind download service
                    ep_info_fab.setImageResource(R.mipmap.ic_stop_dl)
                    fabState = PRESS_TO_STOP_DOWNLOAD
                    startDownloadEpisode()
                }
                PRESS_TO_STOP_DOWNLOAD -> {
                    fabState = PRESS_TO_DOWNLOAD
                    ep_info_fab.setImageResource(R.mipmap.ic_fab_tosubscribe)
                    requestStopDownloadAndCleanup()
                }

                PRESS_TO_PLAY ->
                    // limited to downloaded episode only for now
                    if (mediaService != null && currInfoEpisode != null && currInfoEpisode!!.localUrl != null) {
                        fabState = PRESS_TO_PAUSE
                        ep_info_fab.setImageResource(R.mipmap.ic_pause)
                        // start playing here
                        mediaService!!.playMediaFile(currInfoEpisode)
                        context.startActivity(Intent(context, MediaPlayerActivity::class.java))
                    }


                PRESS_TO_PAUSE -> if (mediaService != null && currInfoEpisode != null) {
                    fabState = PRESS_TO_UNPAUSE
                    ep_info_fab.setImageResource(R.mipmap.ic_play_white)
                    mediaService!!.pausePlayback()
                }


                PRESS_TO_UNPAUSE -> if (mediaService != null && currInfoEpisode != null) {
                    fabState = PRESS_TO_PAUSE
                    ep_info_fab.setImageResource(R.mipmap.ic_pause)
                    mediaService!!.resumePlayback()
                    context.startActivity(Intent(context, MediaPlayerActivity::class.java))
                }
            }

        }
    }

    private fun requestStopDownloadAndCleanup() {
        if (boundToDownload
                && downloadService != null
                && currInfoEpisode!!.downloadUrl != null
                && !currInfoEpisode!!.downloadUrl.trim { it <= ' ' }.isEmpty()) {
            downloadService!!.requestStopDownload(transactionId)
        }
    }

    private fun startDownloadEpisode() {
        if (boundToDownload
                && downloadService != null
                && currInfoEpisode!!.downloadUrl != null
                && !currInfoEpisode!!.downloadUrl.trim { it <= ' ' }.isEmpty()) {

            // get download path and filename
            val downloadsPath = StorageUtil.getPathToStoreEp(currInfoEpisode!!, this.context)

            // get transaction id for
            transactionId = downloadService!!.requestDownLoad(
                    currInfoEpisode!!,
                    downloadsPath.first,
                    downloadsPath.second)!!
        } else {
            Log.e(TAG, "Download Service is not bound or Download URL is bad ${currInfoEpisode.toString()} ")
        }
    }


    private fun loadEpisodeImage() {
        GlideApp.with(context)
                .load(imgUrl)
                .into(ep_info_img)
    }

    override fun onDestroy() {
        super.onDestroy()
        // clean up download service
        if (boundToDownload) {
            this.context.unbindService(serviceConnection)
            downloadService = null
        }

        // cleanup media service
        if (boundToMediaService) {
            this.context.unbindService(mediaServiceConnection)
            mediaService = null
        }
    }


    override fun onPause() {
        super.onPause()
        if (this.downloadListener != null && downloadService != null) {
            downloadService!!.unregisterListener(this.downloadListener)
        }
        // destroy main observer
        if (mainObserverDisposable != null) {
            mainObserverDisposable!!.dispose()
            mainObserverDisposable = null
        }

        if (mediaServiceDisposable != null) {
            mediaServiceDisposable!!.dispose()
            mediaServiceDisposable = null
        }
    }


    override fun onResume() {
        super.onResume()
        bindDownloadService()
        bindMediaService()
        subscribeToEpisodeSubject(currInfoEpisode)
        subscribeToMediaServiceSubject()
        if (mediaService != null) {
            initAddToPlayListListener()
        }
    }


    private fun bindDownloadService() {
        val intent = Intent(this.context, DownloadService::class.java)
        this.context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun bindMediaService() {
        val intent = Intent(this.context, MediaPlayBackService::class.java)
        this.context.bindService(intent, mediaServiceConnection, Context.BIND_AUTO_CREATE)
    }


    internal inner class SimpleSwipeDetector : SwipeDetector() {

        override fun onSwipeDownward(): Boolean {
            return false
        }

        override fun onSwipeRightToLeft(): Boolean {
            return false
        }

        override fun onSwipeLeftToRight(): Boolean {
            this@EpisodeInfoFragment.activity.onBackPressed()
            return true
        }

        override fun onSwipeUpward(): Boolean {
            return false
        }
    }

    companion object {


        private val DATE_PUB_FORMAT = "%s-%s-%s"
        private val MEDIA_INFO_FORMAT = "Size: %s"
        private val TAG = EpisodeInfoFragment::class.java.simpleName


        // STATE OF FAB
        internal val PRESS_TO_DOWNLOAD = 0
        internal val PRESS_TO_STOP_DOWNLOAD = 1
        internal val PRESS_TO_PLAY = 2
        internal val PRESS_TO_PAUSE = 3
        internal val PRESS_TO_UNPAUSE = 4
    }


}
