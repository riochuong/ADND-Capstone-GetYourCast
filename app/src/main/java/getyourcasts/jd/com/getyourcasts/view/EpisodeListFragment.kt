package getyourcasts.jd.com.getyourcasts.view


import android.content.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.Fragment
import android.support.v7.graphics.Palette
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.tonyodev.fetch.listener.FetchListener
import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.repository.local.Contract
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import getyourcasts.jd.com.getyourcasts.repository.remote.network.DownloadService
import getyourcasts.jd.com.getyourcasts.view.adapter.EpisodesRecyclerViewAdapter
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_episode_list.*

/**
 * A placeholder fragment containing a simple view.
 */
class EpisodeListFragment : Fragment() {

    private lateinit var podcast: Podcast

    private lateinit var viewModel: PodcastViewModel

    private lateinit var  episodeAdapter: EpisodesRecyclerViewAdapter



    companion object {
        val PODCAST_KEY = "podcast_key"
        val TAG = EpisodeListFragment.javaClass.simpleName
        val PALETTE_BG_MASK = 0x00555555
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        podcast = getPodcastFromIntent()!!
        viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(context))
        return inflater.inflate(R.layout.fragment_episode_list, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        startAnim()
        super.onViewCreated(view, savedInstanceState)
        // now load image
        initViews()
        bindDownloadService()
    }

    override fun onResume() {
        super.onResume()
        bindDownloadService()
    }

    private fun initViews(){
        initRecyclerView()
        loadPodcastImage()
        // now load title
        episode_podcast_title.text = podcast.collectionName
        // now fetch data from DB
        viewModel.getAllEpisodesOfPodcastObservable(podcast.collectionId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        // On next
                        {
                            // now we can update adapter
                            if (it.size > 0){
                                episodeAdapter.episodeList = it.toMutableList()
                                episodeAdapter.notifyDataSetChanged()
                                // now show the image
                                stopAnim()
                                // make the main view visible now
                                Log.d(TAG,
                                        "Successfully fetched all Episodes of Podcast : ${podcast.collectionName}")
                            }
                        },
                        {
                            it.printStackTrace()
                            Log.e(TAG, it.message)
                        }
                )

    }

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        episode_list_recylcer_view.layoutManager = layoutManager
        // initialize with empty list for now
        episodeAdapter = EpisodesRecyclerViewAdapter(ArrayList<Episode>(),this, podcast)
        episode_list_recylcer_view.adapter = episodeAdapter
    }

    private fun loadPodcastImage() {
        GlideApp.with(context)
                .load(podcast.imgLocalPath)
                .listener(object: RequestListener<Drawable> {
                    override fun onResourceReady(resource: Drawable?,
                                                 model: Any?, target:
                                                 Target<Drawable>?, dataSource:
                                                 DataSource?, isFirstResource: Boolean): Boolean
                    {
                        if (resource != null && resource is BitmapDrawable){
                            val bitmap = resource.bitmap
                            // this will done in background thread with
                            val palette = Palette.from(bitmap).generate(
                                    Palette.PaletteAsyncListener {
                                        val vibrantColor = it.getDarkVibrantColor(PALETTE_BG_MASK)
                                        podcast_detail_appbar.setBackgroundColor(vibrantColor)
                                        if (it.darkVibrantSwatch != null){
                                            episode_podcast_title.setTextColor(it.darkVibrantSwatch!!.titleTextColor)
                                        }
                                        val cv = ContentValues()
                                        cv.put(Contract.PodcastTable.VIBRANT_COLOR, vibrantColor.toString())
                                        viewModel.getUpdatePodcastObservable(podcast, cv)
                                                .subscribe(
                                                        {
                                                            Log.d(TAG," Finish update vibrant color for podcast ")
                                                        },
                                                        {
                                                            it.printStackTrace()
                                                        }
                                                )

                            });
                        }
                        return false
                    }

                    override fun onLoadFailed(e: GlideException?,
                                              model: Any?,
                                              target: Target<Drawable>?,
                                              isFirstResource: Boolean): Boolean {
                        if (e != null){
                            e.printStackTrace()
                        }
                        return false
                    }

                }).into(episode_podcast_img)

    }


    private fun getPodcastFromIntent () :Podcast? {
        val podcast = activity.intent.extras[PODCAST_KEY] as Podcast
        if (podcast != null){
            return podcast
        }
        return null
    }

    /* ============================ CONNECT TO DOWNLOAD SERVICE ========================================= */
    private var boundToDownload = false
    private  var downloadService : DownloadService? = null

    // connection to service
    private val serviceConnection : ServiceConnection = object: ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName?) {
            boundToDownload = false
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            boundToDownload = true
            downloadService = (service as DownloadService.DownloadServiceBinder).getService()
        }

    }

    fun requestDownload(ep: Episode,
                        url: String,
                        dir: String,
                        fileName:String): Long{
        if (downloadService != null){
            return downloadService!!.requestDownLoad(ep,url,dir,fileName)
        }
        return -1
    }

    fun registerListener(listener: FetchListener){
        if (downloadService != null){
            downloadService!!.registerListener(listener)
        }
    }

    fun unRegisterListener(listener: FetchListener){
        if (downloadService != null){
            downloadService!!.unregisterListener(listener)
        }
    }


    override fun onDestroy() {
        if (downloadService != null && boundToDownload){
            boundToDownload = false
            context.unbindService(serviceConnection)
        }
        super.onDestroy()
        episodeAdapter.cleanUpAllDisposables()
    }

    private fun bindDownloadService(){
        val intent = Intent(this.context, DownloadService::class.java)
        this.context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun startAnim(){
        episode_list_loading_prog_view.show()
        episode_list_loading_prog_view.visibility = View.VISIBLE
        podcast_detail_appbar.visibility = View.INVISIBLE
    }

    private fun stopAnim() {
        episode_list_loading_prog_view.hide()
        episode_list_loading_prog_view.visibility = View.GONE
        podcast_detail_appbar.visibility = View.VISIBLE
    }

}
