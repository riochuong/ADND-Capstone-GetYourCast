package getyourcasts.jd.com.getyourcasts.view

import android.content.*
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.exoplayer2.ExoPlayer
import com.tonyodev.fetch.listener.FetchListener
import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.exoplayer.MediaPlayBackService
import getyourcasts.jd.com.getyourcasts.repository.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.repository.remote.network.DownloadService
import getyourcasts.jd.com.getyourcasts.util.DatePub
import getyourcasts.jd.com.getyourcasts.util.StorageUtil
import getyourcasts.jd.com.getyourcasts.util.TimeUtil
import getyourcasts.jd.com.getyourcasts.view.adapter.EpisodesRecyclerViewAdapter
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp
import getyourcasts.jd.com.getyourcasts.view.touchListener.SwipeDetector
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_episode_info.*

/**
 * A placeholder fragment containing a simple view.
 */
class EpisodeInfoFragment : Fragment() {

    private lateinit var episode: Episode
    private var bgColor: Int = 0
    private lateinit var imgUrl: String
    private var datePub: DatePub? = null
    private lateinit var viewModel: PodcastViewModel
    private var fabState = PRESS_TO_DOWNLOAD
    private var downloadListener : FetchListener? = null
    private var transactionId = -1L
    private  var mainObserverDisposable : Disposable? = null
    private var mediaServiceDisposable : Disposable? = null

    companion object {
        val DATE_PUB_FORMAT = "%s-%s-%s"
        val MEDIA_INFO_FORMAT = "Size: %s"
        val TAG = EpisodeInfoFragment::class.java.simpleName


        // STATE OF FAB
        const val PRESS_TO_DOWNLOAD = 0
        const val PRESS_TO_STOP_DOWNLOAD =1
        const val PRESS_TO_PLAY = 2
        const val PRESS_TO_PAUSE = 3
        const val PRESS_TO_UNPAUSE = 4
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        episode = getEpisodeFromIntent()
        bgColor = getBgColorFromIntent()
        imgUrl = getImageUrlFromIntent()
        viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(context))
        if (episode.pubDate != null) {
            datePub = TimeUtil.parseDatePub(episode.pubDate!!)
        }

        return inflater.inflate(R.layout.fragment_episode_info, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startAnim()
        initViews()
    }

    private fun initViews() {
        // load title
        ep_info_title.text = episode.title

        // load image view
        loadEpisodeImage()

        // change bg color
        ep_info_app_bar.setBackgroundColor(bgColor)

        // load more infos
        ep_info_desc.text = episode.description
        ep_info_release.text = episode.pubDate

        // published date might not be available
        if (datePub != null) {
            ep_info_release.text = DATE_PUB_FORMAT.format(datePub!!.month, datePub!!.dayOfMonth, datePub!!.year)
        }

        if (episode.fileSize != null) {
            ep_info_media_info.text = MEDIA_INFO_FORMAT.format(StorageUtil.convertToMbRep(episode.fileSize!!))
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


    private fun subscribeToMediaServiceSubject (episode: Episode) {
        MediaPlayBackService.subscribeMediaPlaybackSubject(object : Observer<Pair<String,Int>> {
            override fun onNext(t: Pair<String, Int>) {
                val episodeId = t.first
                val state = t.second
                if (episode.uniqueId.equals(episodeId)){
                    // check which state we should set the fab
                    when(state) {
                        // only need to change if this is already playing
                        MediaPlayBackService.MEDIA_PLAYING -> {
                            fabState = PRESS_TO_PAUSE
                            ep_info_fab.setImageResource(R.mipmap.ic_pause)
                        }
                        MediaPlayBackService.MEDIA_STOPPED -> {
                            fabState = PRESS_TO_PLAY
                            ep_info_fab.setImageResource(R.mipmap.ic_play_white)
                        }
                    }

                }
            }

            override fun onSubscribe(d: Disposable) {
                mediaServiceDisposable = d
            }

            override fun onError(e: Throwable) {
                Log.e(TAG, "Error during receiving media playback service info ")
                e.printStackTrace()
            }

            override fun onComplete() {

            }

        })
    }

    private fun subscribeToEpisodeSubject (ep: Episode){
        val observer = object : Observer<PodcastViewModel.EpisodeState> {

            override fun onSubscribe(d: Disposable) {
                mainObserverDisposable = d
            }

            override fun onNext(epState: PodcastViewModel.EpisodeState) {
                if (epState.uniqueId.equals(ep.getEpisodeUniqueKey())) {

                    when (epState.state){
                        PodcastViewModel.EpisodeState.DOWNLOADING -> {
                            fabState = PRESS_TO_STOP_DOWNLOAD
                            ep_info_fab.visibility = View.VISIBLE
                            ep_info_fab.setImageResource(R.mipmap.ic_stop_white)
                        }

                        PodcastViewModel.EpisodeState.FETCHED -> {
                            fabState = PRESS_TO_DOWNLOAD
                            ep_info_fab.visibility = View.VISIBLE
                            ep_info_fab.setImageResource(R.mipmap.ic_fab_tosubscribe)
                        }

                        PodcastViewModel.EpisodeState.DOWNLOADED -> {
                            fabState = PRESS_TO_PLAY
                            ep_info_fab.visibility = View.VISIBLE
                            ep_info_fab.setImageResource(R.mipmap.ic_play_white)
                        }
                    }
                }
            }

            override fun onError(e: Throwable) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onComplete() {
                ep_info_fab.visibility = View.VISIBLE
                ep_info_fab.setImageResource(R.mipmap.ic_play_white)
            }

        }
        PodcastViewModel.subscribeEpisodeSubject(observer)
    }

    /**
     * helper to initialize FAB button
     */
    private fun adjustFabState(){
        // change fab color to red always
        changeFabColor(ContextCompat.getColor(this.context, R.color.unfin_color))

        if (getIsDownloadingFromIntent()){
            fabState = PRESS_TO_STOP_DOWNLOAD
            ep_info_fab.visibility = View.INVISIBLE
            ep_info_fab.setImageResource(R.mipmap.ic_stop_dl)
        }
        else if (episode.downloaded == PodcastViewModel.EpisodeState.DOWNLOADED) {
            fabState = PRESS_TO_PLAY
            ep_info_fab.visibility = View.VISIBLE
            ep_info_fab.setImageResource(R.mipmap.ic_play_white)

        } else {
            fabState = PRESS_TO_DOWNLOAD
            ep_info_fab.visibility = View.VISIBLE
            ep_info_fab.setImageResource(R.mipmap.ic_fab_tosubscribe)
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
        ep_info_fab.backgroundTintList = (ColorStateList.valueOf(color))
    }

    private fun setFabButtonOnClickListener() {
        // set on click listener
        ep_info_fab.setOnClickListener {
            // check if the ep is donwloading

            when (fabState){
                PRESS_TO_DOWNLOAD ->{
                    // check if episode is already state or not
                    // bind download service
                    ep_info_fab.setImageResource(R.mipmap.ic_stop_dl)
                    fabState = PRESS_TO_STOP_DOWNLOAD
                    startDownloadEpisode ()

                }

                PRESS_TO_STOP_DOWNLOAD ->{
                    // TODO : need to implement stop download
                    fabState = PRESS_TO_DOWNLOAD
                    ep_info_fab.setImageResource(R.mipmap.ic_todownload)
                }

                PRESS_TO_PLAY -> {
                    // limited to downloaded episode only for now
                    if (mediaService != null && episode != null && episode.localUrl != null){
                        fabState = PRESS_TO_PAUSE
                        ep_info_fab.setImageResource(R.mipmap.ic_pause)
                        // quick hack for fun
                        if (episode.type!!.contains("audio")){
                            mediaService!!.playLocalUrlAudio(episode)
                        } else{

                        }



                    }

                }

                PRESS_TO_PAUSE -> {
                    if (mediaService != null && episode != null){
                        fabState = PRESS_TO_UNPAUSE
                        ep_info_fab.setImageResource(R.mipmap.ic_play_white)
                        mediaService!!.pausePlayback()
                    }

                }

                PRESS_TO_UNPAUSE -> {
                    if (mediaService != null && episode != null){
                        fabState = PRESS_TO_PAUSE
                        ep_info_fab.setImageResource(R.mipmap.ic_pause)
                        mediaService!!.resumePlayback()
                    }

                }
            }

        }
    }

    private fun requestStopDownloadAndCleanup() {

    }

    private fun startDownloadEpisode (){
        if (serviceConnection != null
                && boundToDownload
                && downloadService != null
                && episode.downloadUrl != null
                && episode.downloadUrl!!.trim().length > 0) {

            // get download path and filename
            val downloadsPath = StorageUtil.getPathToStoreEp(episode, this.context)

            // get transaction id for
            transactionId = downloadService!!.requestDownLoad(
                    episode,
                    episode.downloadUrl!!,
                    downloadsPath!!.first,
                    downloadsPath.second)
        } else {
            Log.e(TAG, "Download Service is not bound or Download URL is bad ${episode.toString()} ")
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
        if (serviceConnection != null && boundToDownload) {
            this.context.unbindService(serviceConnection)
            downloadService = null
        }

        // cleanup media service
        if (mediaServiceConnection != null && boundToMediaService) {
            this.context.unbindService(mediaServiceConnection)
            mediaService = null
        }
    }

    override fun onPause() {
        super.onPause()
        if (this.downloadListener != null
                && downloadService != null){
            downloadService!!.unregisterListener(this.downloadListener!!)
        }
        // destroy main observer
        if (mainObserverDisposable != null){
            mainObserverDisposable!!.dispose()
            mainObserverDisposable = null
        }

        if (mediaServiceDisposable != null ){
            mediaServiceDisposable!!.dispose()
            mediaServiceDisposable = null
        }

    }


    override fun onResume() {
        super.onResume()
        bindDownloadService()
        bindMediaService()
        subscribeToEpisodeSubject(episode)
        subscribeToMediaServiceSubject(episode)
    }

    private fun bindDownloadService() {
        val intent = Intent(this.context, DownloadService::class.java)
        this.context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun bindMediaService() {
        val intent = Intent(this.context, MediaPlayBackService::class.java)
        this.context.bindService(intent, mediaServiceConnection, Context.BIND_AUTO_CREATE)
    }

    /* ============================ CONNECT TO DOWNLOAD SERVICE ========================================= */
    private var boundToDownload = false
    private var downloadService: DownloadService? = null

    // connection to service
    private val serviceConnection: ServiceConnection = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName?) {
            boundToDownload = false
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            boundToDownload = true
            downloadService = (service as DownloadService.DownloadServiceBinder).getService()
        }

    }

    /* ============================ CONNECT TO MEDIA SERVICE ========================================= */
    private var boundToMediaService = false
    private var mediaService: MediaPlayBackService? = null

    // connection to service
    private val mediaServiceConnection: ServiceConnection = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName?) {
            boundToMediaService = false
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            boundToMediaService = true
            mediaService = (service as MediaPlayBackService.MediaPlayBackServiceBinder).getService()
        }

    }

    /**
     * retreived episode passed from the fragment list
     */
    private fun getEpisodeFromIntent(): Episode {
        val data = activity.intent.extras[EpisodesRecyclerViewAdapter.EPISODE_KEY] as Episode
        return data
    }

    /**
     * retreive back ground color
     */
    private fun getBgColorFromIntent(): Int {
        val data = activity.intent.extras[EpisodesRecyclerViewAdapter.BG_COLOR_KEY] as Int
        return data
    }

    private fun getImageUrlFromIntent(): String {
        val data = activity.intent.extras[EpisodesRecyclerViewAdapter.PODAST_IMG_KEY] as String
        return data
    }

    private fun getIsDownloadingFromIntent(): Boolean {
        val data = activity.intent.getBooleanExtra(EpisodesRecyclerViewAdapter.IS_DOWNLOADING,false)
        return data
    }

    private fun getDownloadingStatusFromIntent(): Long {
        val data = activity.intent.getLongExtra(EpisodesRecyclerViewAdapter.IS_DOWNLOADING_KEY, -1)
        return data
    }

    inner class SimpleSwipeDetector : SwipeDetector() {
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

        override fun onSwipeDownward(): Boolean {
            return false
        }

    }
}
