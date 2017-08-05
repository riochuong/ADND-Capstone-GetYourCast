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
import com.tonyodev.fetch.listener.FetchListener
import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.repository.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.repository.local.EpisodeTable
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.repository.remote.network.DownloadService
import getyourcasts.jd.com.getyourcasts.util.DatePub
import getyourcasts.jd.com.getyourcasts.util.StorageUtil
import getyourcasts.jd.com.getyourcasts.util.TimeUtil
import getyourcasts.jd.com.getyourcasts.view.adapter.EpisodeDownloadListener
import getyourcasts.jd.com.getyourcasts.view.adapter.EpisodesRecyclerViewAdapter
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp
import getyourcasts.jd.com.getyourcasts.view.touchListener.SwipeDetector
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
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
    private var isEpDownloading = false
    private var downloadListener : FetchListener? = null
    private var transactionId = -1L

    companion object {
        val DATE_PUB_FORMAT = "%s-%s-%s"
        val MEDIA_INFO_FORMAT = "Size: %s"
        val TAG = EpisodeInfoFragment::class.java.simpleName
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

        // get transaction id to determine downloading status
        transactionId = getDownloadingStatusFromIntent()
        if (transactionId > 0){
            isEpDownloading = true
        }

        setFabButtonOnClickListener()

        // add swipe detector to scroll views
        episode_info_main_layout.setOnTouchListener(SimpleSwipeDetector())
        episode_info_scroll_view.setOnTouchListener(SimpleSwipeDetector())

        // init fab
        initFabState()

        // enable main view
        stopAnim()



    }

    private fun subscribeToEpisodeSubject (ep: Episode){
        PodcastViewModel.subscribeEpisodeSubject(object : Observer<PodcastViewModel.EpisodeState> {
            override fun onNext(epState: PodcastViewModel.EpisodeState) {
                if (epState.uniqueId.equals(ep.getEpisodeUniqueKey())) {

                    when (epState.state){
                        PodcastViewModel.EpisodeState.DOWNLOADING -> {
                            ep_info_fab.visibility = View.INVISIBLE
                            ep_info_fab.setImageResource(R.mipmap.ic_stop_white)
                        }

                        PodcastViewModel.EpisodeState.FETCHED -> {
                            ep_info_fab.visibility = View.VISIBLE
                            ep_info_fab.setImageResource(R.mipmap.ic_fab_tosubscribe)
                        }

                        PodcastViewModel.EpisodeState.DOWNLOADED -> {
                            ep_info_fab.visibility = View.VISIBLE
                            ep_info_fab.setImageResource(R.mipmap.ic_play_white)
                        }
                    }
                }
            }

            override fun onError(e: Throwable) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onSubscribe(d: Disposable) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onComplete() {
                ep_info_fab.visibility = View.VISIBLE
                ep_info_fab.setImageResource(R.mipmap.ic_play_white)
            }

        })
    }

    /**
     * helper to initialize FAB button
     */
    private fun initFabState(){
        // change fab color to red always
        changeFabColor(ContextCompat.getColor(this.context, R.color.unfin_color))

        if (episode.state == PodcastViewModel.EpisodeState.DOWNLOADING){
            ep_info_fab.visibility = View.INVISIBLE
            ep_info_fab.setImageResource(R.mipmap.ic_stop_white)
        }
        else if (episode.state == PodcastViewModel.EpisodeState.FETCHED) {
            ep_info_fab.visibility = View.VISIBLE
            ep_info_fab.setImageResource(R.mipmap.ic_fab_tosubscribe)

        } else {
            ep_info_fab.visibility = View.VISIBLE
            ep_info_fab.setImageResource(R.mipmap.ic_play_white)
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

            if (! isEpDownloading) {

                // check if episode is already state or not
                if (episode.state == 0) {
                    // bind download service
                    ep_info_fab.setImageResource(R.mipmap.ic_stop_white)
                    isEpDownloading = true
                    startDownloadEpisode ()
                }
                // PLAY THE FILE ERE
                else {
                    //TODO : add this when exoplayer is ready
                }
            }

            else{
                // TODO: EPISODE IS DOWNLOADING ... WE CAN SEND REQUEST TO STOP HERE
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
                    episode.uniqueId,
                    episode.downloadUrl!!,
                    downloadsPath!!.first,
                    downloadsPath.second, episode.title)
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
        if (serviceConnection != null && boundToDownload) {
            this.context.unbindService(serviceConnection)
            downloadService = null
        }
    }

    override fun onPause() {
        super.onPause()
        if (this.downloadListener != null
                && downloadService != null){
            downloadService!!.unregisterListener(this.downloadListener!!)
        }

    }


    override fun onResume() {
        super.onResume()
        bindDownloadService()
    }

    private fun bindDownloadService() {
        val intent = Intent(this.context, DownloadService::class.java)
        this.context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
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

    private fun getEpKeyFromIntent(): String {
        val data = activity.intent.extras[EpisodesRecyclerViewAdapter.ITEM_KEY] as String
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
