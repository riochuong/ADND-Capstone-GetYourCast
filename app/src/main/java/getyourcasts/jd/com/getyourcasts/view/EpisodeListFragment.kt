package getyourcasts.jd.com.getyourcasts.view


import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.IBinder
import android.support.design.widget.AppBarLayout
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.graphics.Palette
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView

import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.tonyodev.fetch.listener.FetchListener
import com.wang.avi.AVLoadingIndicatorView

import java.util.ArrayList

import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.exoplayer.MediaPlayBackService
import getyourcasts.jd.com.getyourcasts.repository.local.Contract
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import getyourcasts.jd.com.getyourcasts.repository.remote.network.DownloadService
import getyourcasts.jd.com.getyourcasts.view.adapter.EpisodesRecyclerViewAdapter
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp
import getyourcasts.jd.com.getyourcasts.view.media.MediaServiceBoundListener
import getyourcasts.jd.com.getyourcasts.viewmodel.EpisodeOfPodcastLoader
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

/**
 * A placeholder fragment containing a simple view.
 */
class EpisodeListFragment : Fragment(), MediaServiceBoundListener, PopupMenu.OnMenuItemClickListener, LoaderManager.LoaderCallbacks<List<Episode>> {


    private var podcast: Podcast? = null

    private var viewModel: PodcastViewModel? = null

    private var episodeAdapter: EpisodesRecyclerViewAdapter? = null

    private var mediaService: MediaPlayBackService? = null

    internal lateinit var loaderManager: LoaderManager

    // UI ITEMs
    private lateinit var show_menu_btn: ImageButton
    internal lateinit var episode_podcast_title: TextView
    private lateinit var episode_list_recylcer_view: RecyclerView
    internal lateinit var podcast_detail_appbar: AppBarLayout
    private lateinit var episode_podcast_img: ImageView
    private lateinit var episode_list_loading_prog_view: AVLoadingIndicatorView


    private val podcastFromIntent: Podcast?
        get() = activity.intent.getParcelableExtra(PODCAST_KEY)

    /* ============================ CONNECT TO DOWNLOAD SERVICE ========================================= */
    private var boundToDownload = false
    private var downloadService: DownloadService? = null

    // connection to service
    private val downloadServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            boundToDownload = true
            downloadService = (service as DownloadService.DownloadServiceBinder).service
        }

        override fun onServiceDisconnected(name: ComponentName) {
            boundToDownload = false
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        podcast = podcastFromIntent
        viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(context))
        // register listener for when media service is bound
        if (mediaService == null) {
            (activity as EpisodeListActivity).registerMediaServiceBoundListenter(
                   this
            )
        }
        val root = inflater!!.inflate(R.layout.fragment_episode_list, container, false)
        show_menu_btn = root.findViewById(R.id.show_menu_btn) as ImageButton
        episode_podcast_title = root.findViewById(R.id.episode_podcast_title) as TextView
        episode_list_recylcer_view = root.findViewById(R.id.episode_list_recylcer_view) as RecyclerView
        podcast_detail_appbar = root.findViewById(R.id.podcast_detail_appbar) as AppBarLayout
        episode_podcast_img = root.findViewById(R.id.episode_podcast_img) as ImageView
        episode_list_loading_prog_view = root.findViewById(R.id
                .episode_list_loading_prog_view) as AVLoadingIndicatorView
        loaderManager = activity.supportLoaderManager
        return root
    }


    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_unsubscribe) {
            Log.d(TAG, "Unsubscribe from Podcast \${podcast.collectionName}")
            viewModel!!.getUnsubscribeObservable(podcast!!.collectionId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<Boolean> {
                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onNext(res: Boolean) {
                            if (res!!) {
                                activity.finish()
                            }
                        }

                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                        }

                        override fun onComplete() {

                        }
                    })
            // on next here


            return true
        }
        return false
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        startAnim()
        super.onViewCreated(view, savedInstanceState)
        // now load image
        initViews()
        bindDownloadService()

        // set menu options for unsubscribed
        show_menu_btn.setOnClickListener { viewItem ->
            val popup = PopupMenu(this.context, viewItem)
            // This activity implements OnMenuItemClickListener
            popup.setOnMenuItemClickListener(this)
            popup.inflate(R.menu.menu_episode_list_details)
            popup.show()
        }
    }


    override fun onResume() {
        super.onResume()
        (activity as EpisodeListActivity).registerMediaServiceBoundListenter(this)
        bindDownloadService()
    }


    override fun onPause() {
        super.onPause()
        mediaService = null
    }

    private fun initViews() {
        initRecyclerView()
        loadPodcastImage()
        // now load title
        episode_podcast_title.text = podcast!!.collectionName
        loaderManager.initLoader(EPISODE_LIST_LOADER_ID_, Bundle(), this).forceLoad()
    }

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        episode_list_recylcer_view.layoutManager = layoutManager
        // initialize with empty list for now
        episodeAdapter = EpisodesRecyclerViewAdapter(ArrayList(), this, podcast)
        episode_list_recylcer_view.adapter = episodeAdapter
    }

    private fun loadPodcastImage() {
        GlideApp.with(context)
                .load(podcast!!.imgLocalPath)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?,
                                              model: Any, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                        e?.printStackTrace()
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?,
                                                 model: Any,
                                                 target: Target<Drawable>,
                                                 dataSource: DataSource,
                                                 isFirstResource: Boolean): Boolean {

                        if (resource != null && resource is BitmapDrawable) {
                            val bitmap = resource.bitmap
                            // this will done in background thread with
                            Palette.from(bitmap).generate { palette ->
                                val vibrantColor = palette.getDarkVibrantColor(PALETTE_BG_MASK)
                                podcast_detail_appbar.setBackgroundColor(vibrantColor)
                                if (palette.darkVibrantSwatch != null) {
                                    episode_podcast_title.setTextColor(palette.darkVibrantSwatch!!
                                            .titleTextColor)
                                }
                                val cv = ContentValues()
                                cv.put(Contract.PodcastTable.VIBRANT_COLOR, vibrantColor.toString() + "")
                                viewModel!!.getUpdatePodcastObservable(podcast, cv)
                                        .subscribe(
                                                object : Observer<Boolean> {
                                                    override fun onSubscribe(d: Disposable) {

                                                    }

                                                    override fun onNext(aBoolean: Boolean) {
                                                        Log.d(TAG, " Finish update vibrant color for podcast ")
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
                        return false
                    }
                }).into(episode_podcast_img)


    }

    fun requestDownload(ep: Episode,
                        url: String,
                        dir: String,
                        fileName: String): Long? {
        return if (downloadService != null) {
            downloadService!!.requestDownLoad(ep, url, dir, fileName)
        } else -1L
    }

    fun requestStopDownload(id: Long?) {
        if (downloadService != null) {

        }
    }

    fun registerListener(listener: FetchListener) {
        if (downloadService != null) {
            downloadService!!.registerListener(listener)
        }
    }

    fun unRegisterListener(listener: FetchListener) {
        if (downloadService != null) {
            downloadService!!.unregisterListener(listener)
        }
    }

    private fun bindDownloadService() {
        val intent = Intent(this.context, DownloadService::class.java)
        this.context.bindService(intent, downloadServiceConnection, Context.BIND_AUTO_CREATE)
    }


    fun requestToPlaySong(episode: Episode) {
        if (mediaService != null) {
            mediaService!!.playMediaFile(episode)
        }

    }

    fun requestToPause() {
        if (mediaService != null) {
            mediaService!!.pausePlayback()
        }

    }

    fun requestToResume() {
        if (mediaService != null) {
            mediaService!!.resumePlayback()
        }

    }


    /*=================================================================================================== */


    override fun onDestroy() {
        if (downloadService != null && boundToDownload) {
            boundToDownload = false
            context.unbindService(downloadServiceConnection)
        }
        super.onDestroy()
        episodeAdapter!!.cleanUpAllDisposables()
    }

    override fun onMediaServiceBound(service: MediaPlayBackService) {
        stopAnim()
        mediaService = service
    }


    private fun startAnim() {
        episode_list_loading_prog_view.show()
        episode_list_loading_prog_view.visibility = View.VISIBLE
        podcast_detail_appbar.visibility = View.INVISIBLE
    }

    private fun stopAnim() {
        episode_list_loading_prog_view.hide()
        episode_list_loading_prog_view.visibility = View.GONE
        podcast_detail_appbar.visibility = View.VISIBLE
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<List<Episode>> {
        if (id != EPISODE_LIST_LOADER_ID_) {
            throw IllegalArgumentException("Wrong loader id received...must be weird")
        }
        return EpisodeOfPodcastLoader(activity, podcast!!.collectionId)
    }

    override fun onLoadFinished(loader: Loader<List<Episode>>, episodeList: List<Episode>) {
        if (episodeList.size > 0) {
            episodeAdapter!!.episodeList = episodeList
            episodeAdapter!!.notifyDataSetChanged()
            // now show the image
            stopAnim()
            // make the main view visible now
            Log.d(TAG,
                    "Successfully fetched all Episodes of Podcast : \${podcast" + ".collectionName}")
        }
    }

    override fun onLoaderReset(loader: Loader<List<Episode>>) {

    }

    companion object {


        private val PODCAST_KEY = "podcast_key"
        private val TAG = EpisodeListFragment::class.java.simpleName
        private val PALETTE_BG_MASK = 0x00555555
        private val EPISODE_LIST_LOADER_ID_ = 852
    }
}
