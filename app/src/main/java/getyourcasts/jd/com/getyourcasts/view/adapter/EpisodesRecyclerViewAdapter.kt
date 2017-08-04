package getyourcasts.jd.com.getyourcasts.view.adapter

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.github.lzyzsd.circleprogress.CircleProgress
import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.repository.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.repository.local.EpisodeTable
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import getyourcasts.jd.com.getyourcasts.util.StorageUtil
import getyourcasts.jd.com.getyourcasts.util.TimeUtil
import getyourcasts.jd.com.getyourcasts.view.EpisodeInfoActivity
import getyourcasts.jd.com.getyourcasts.view.EpisodeListFragment
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable


/**
 * Created by chuondao on 7/26/17.
 */

class EpisodesRecyclerViewAdapter(var episodeList: MutableList<Episode>,
                                  val fragment: EpisodeListFragment,
                                  val podcast: Podcast) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val viewModel: PodcastViewModel

    private val ctx: Context

    private val bgColor: Int

    /*to keep track of downloaidng items to send it to details view */
    private var mapLock = Object()
    private var downloadItemMaps = HashMap<String, Long>()

    init {
        viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(fragment.activity.applicationContext))
        ctx = fragment.activity.applicationContext
        // quick hack to get bgColor
        bgColor =
                ((fragment.view!!.findViewById(R.id.podcast_detail_appbar) as View).background as ColorDrawable).color
    }

    companion object {
        val TAG = "PocastAdapter"
        val PODCAST_KEY = "podcast_key"
        val ITEM_KEY = "item_key"
        val BG_COLOR_KEY = "bg_color"
        val PODAST_IMG_KEY = "podcast_img"
        val EPISODE_KEY = "episode"
        val REQUEST_CODE = 1
        val IS_DOWNLOADING_KEY = "is_downloading"
    }


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.episode_item_layout, parent, false)
        // set view onClickListener
        val vh = EpisodeItemViewHolder(view)

        // always make the progress to be red
        vh.progressView.finishedColor = ContextCompat.getColor(ctx, R.color.unfin_color)

        return vh
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val episode = episodeList[position]
        // load episode info
        val vh = holder as EpisodeItemViewHolder
        vh.nameText.text = episode.title
        // load date
        if (episode.pubDate != null) {
            val dateParsed = TimeUtil.parseDatePub(episode.pubDate)
            if (dateParsed != null) {
                vh.monthText.text = "${dateParsed.month},${dateParsed.dayOfMonth}"
                vh.yearText.text = dateParsed.year
            }
        }
        // load size
        if (episode.fileSize != null) {
            vh.fileSize.text = (StorageUtil.convertToMbRep(episode.fileSize))
        }

        // load download or play icons depends on podcast url link available or not
        loadCorrectDownOrPlayImg(episode, vh)

        // set on click listener to download file and updat progress
        setViewholderListenerForDowmload(vh, episode, position)

        // set on click listener for episode detail info
        setOnClickListenerForEpisodeInfo(vh, episode, position)
    }

    private fun setOnClickListenerForEpisodeInfo(vh: EpisodeItemViewHolder,
                                                 ep: Episode,
                                                 itemPos: Int
                                                ) {

        vh.mainLayout.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val intent = Intent(ctx, EpisodeInfoActivity::class.java)
                intent.putExtra(BG_COLOR_KEY, bgColor)
                intent.putExtra(EPISODE_KEY, ep)
                intent.putExtra(PODAST_IMG_KEY, podcast.imgLocalPath)
                intent.putExtra(ITEM_KEY, ep.getEpisodeUniqueKey())
                // check if this item is being downloaded
                synchronized(mapLock) {
                    if (downloadItemMaps.containsKey(ep.getEpisodeUniqueKey())) {
                        intent.putExtra(IS_DOWNLOADING_KEY, downloadItemMaps[ep.getEpisodeUniqueKey()])
                    }
                }
                ctx.startActivity(intent)


                // subscribe to item sync just incase user start
                // download episode from the details screen
                if (ep.downloaded == 0) {
                    // have to subscribe
                    PodcastViewModel.subscribeEpisodeDownload(object : Observer<Pair<String, Long>> {
                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                        }

                        override fun onComplete() {

                        }

                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onNext(t: Pair<String, Long>) {
                            val pos = t.first
                            val transactionId = t.second
                            val paths = StorageUtil.getPathToStoreEp(ep, fragment.activity.applicationContext)
                            val fullUrl = "${paths!!.first}/${paths.second}"
                            if (pos.equals(ep.getEpisodeUniqueKey())) {
                                // need to update the downloading table
                                synchronized(mapLock) {
                                    downloadItemMaps.put(ep.getEpisodeUniqueKey(), transactionId)
                                }

                                val listener = getListenerForDownload(transactionId,
                                        vh,
                                        fullUrl,
                                        ep,
                                        itemPos)

                                // start showing progress
                                showProgressView(vh)
                                this@EpisodesRecyclerViewAdapter.fragment.registerListener(transactionId, listener)

                            }
                        }

                    })
                }
            }

        })

    }

    /**
     * show porgress view and hide button
     */
    private fun showProgressView(vh : EpisodeItemViewHolder){
        vh.downPlayImg.visibility = View.GONE
        vh.progressView.visibility = View.VISIBLE
    }

    private fun hideProgressView(vh: EpisodeItemViewHolder){
        vh.downPlayImg.visibility = View.VISIBLE
        vh.progressView.visibility = View.GONE
    }


    /**
     * Set viewholder logic for pressing download button or play
     */
    private fun setViewholderListenerForDowmload(vh: EpisodeItemViewHolder,
                                                 episode: Episode,
                                                 pos: Int) {

        vh.downPlayImg.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                // check if
                if (episode.downloaded == 0) {
                    startDownloadEpisodeFile(episode, vh ,pos)
                } else {
                    // TODO : add play feature here after implementing exoplayer
                }
            }
        }
        )

    }

    /**
     * start to download episode here
     */
    private fun startDownloadEpisodeFile(episode: Episode,
                                         vh: EpisodeItemViewHolder,
                                         itempos: Int
                                         ) {
        // Now start Downloading
        val url = episode.downloadUrl
        val pathItems = StorageUtil.getPathToStoreEp(episode, fragment.activity.applicationContext)
        // TODO :detect duplicate here to avoid crash
        if (url != null) {
            val transactionId = fragment.requestDownload(url, pathItems!!.first, pathItems.second, episode.title)

            // if transaction is valid we can start listener to updat progress here
            if (transactionId > 0) {
                // stored it in the map
                synchronized(mapLock) {
                    downloadItemMaps.put(episode.getEpisodeUniqueKey(), transactionId)
                }
                // synchronize with other views if it's availabe
                PodcastViewModel.getEpisodeDownloadSubject().onNext(Pair(episode.getEpisodeUniqueKey(),
                        transactionId))
                // disable play view and show progress
                showProgressView(vh)

                // register listener for progress update
                val fullUrl = "${pathItems.first}/${pathItems}"
                val listener = getListenerForDownload(transactionId, vh, fullUrl, episode,itempos)
                // register listener to do the update progress
                fragment.registerListener(transactionId, listener)
            } else {
                Log.e(EpisodesRecyclerViewAdapter.TAG, "Failed to start Download file from Fetch ${episode.downloadUrl}")
            }
        }
    }


    private fun getListenerForDownload(transactionId: Long,
                                       vh: EpisodeItemViewHolder,
                                       localUrl: String?,
                                       episode: Episode,
                                       itemPos: Int
                                      ): EpisodeDownloadListener {
        val listener = object : EpisodeDownloadListener(transactionId) {

            override fun onProgressUpdate(progress: Int) {
                vh.progressView.progress = progress
            }

            override fun onComplete() {
                // remove downloaded item from map
                synchronized(mapLock) {
                    downloadItemMaps.remove(episode.getEpisodeUniqueKey())
                }
                // now update db with new path to the audio file and downloaded

                    val cvUpdate = ContentValues()
                    cvUpdate.put(EpisodeTable.LOCAL_URL, localUrl)
                    cvUpdate.put(EpisodeTable.DOWNLOADED, 1)
                    val updateDbObsv = viewModel.getUpdateEpisodeObservable(episode, cvUpdate)
                    // update db with new local url and downloaded columns
                    updateDbObsv.observeOn(AndroidSchedulers.mainThread()).subscribe(
                            {
                                if (it) {
                                    // remove downloaded item from list
                                    vh.downPlayImg.setImageResource(R.mipmap.ic_ep_play)
                                    hideProgressView(vh)
                                    updateItemData(episode, itemPos)
                                } else {
                                    // failed here also call onError
                                    Log.e(TAG," Failed to update the db after download. The media file should be " +
                                            "good  ")
                                }
                            },
                            {
                                Log.e(EpisodesRecyclerViewAdapter.TAG, "Failed to update episode DB data ${episode.title}")
                                onError()
                            }
                    )


            }

            override fun onError() {
                Log.e(EpisodesRecyclerViewAdapter.TAG, "Failed to download episode ${episode.title}")
                vh.downPlayImg.setImageResource(R.mipmap.ic_ep_down)
                vh.downPlayImg.visibility = View.VISIBLE
                vh.progressView.visibility = View.GONE
            }

        }

        return listener
    }


    private fun updateItemData (ep: Episode, pos: Int) {
        viewModel.getEpisodeObsevatble(ep)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it.downloaded == 1){
                        episodeList[pos] = it
                        notifyItemChanged(pos)
                    } else{
                        // something wrong with db update
                        Log.e(TAG,"Failed DB update very Unexepected")
                    }
                }

    }

    // suggest to download or play episode
    private fun loadCorrectDownOrPlayImg(ep: Episode, vh: EpisodeItemViewHolder) {
        // check if the file is already downloaded or not
        if (ep.localUrl == null) {
            vh.downPlayImg.setImageResource(R.mipmap.ic_ep_down)
        } else {
            vh.downPlayImg.setImageResource(R.mipmap.ic_ep_play)
        }
    }


    override fun getItemCount(): Int {
        return episodeList.size
    }

}


class EpisodeItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val mainLayout: View
    val downPlayImg: ImageView
    val monthText: TextView
    val nameText: TextView
    val yearText: TextView
    val fileSize: TextView
    val progressView: CircleProgress

    // bind item to view here
    init {
        mainLayout = itemView.findViewById(R.id.episode_main_view_layout)
        downPlayImg = itemView.findViewById(R.id.episode_down_play_img) as ImageView
        nameText = itemView.findViewById(R.id.episode_name) as TextView
        monthText = itemView.findViewById(R.id.episode_month_text) as TextView
        yearText = itemView.findViewById(R.id.episode_year_text) as TextView
        fileSize = itemView.findViewById(R.id.episode_file_size) as TextView
        progressView = itemView.findViewById(R.id.circle_progress) as CircleProgress
    }
}