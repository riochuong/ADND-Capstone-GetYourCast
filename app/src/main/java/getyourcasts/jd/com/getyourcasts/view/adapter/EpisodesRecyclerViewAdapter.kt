package getyourcasts.jd.com.getyourcasts.view.adapter

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
import getyourcasts.jd.com.getyourcasts.exoplayer.MediaPlayBackService
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import getyourcasts.jd.com.getyourcasts.util.ButtonStateUtil
import getyourcasts.jd.com.getyourcasts.util.StorageUtil
import getyourcasts.jd.com.getyourcasts.util.TimeUtil
import getyourcasts.jd.com.getyourcasts.view.EpisodeInfoActivity
import getyourcasts.jd.com.getyourcasts.view.EpisodeListFragment
import getyourcasts.jd.com.getyourcasts.viewmodel.EpisodeState
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.*

/**
 * Created by chuondao on 8/14/17.
 */

class EpisodesRecyclerViewAdapter(private var episodeList: MutableList<Episode>,
                                  private val fragment: EpisodeListFragment,
                                  private val podcast: Podcast) : RecyclerView.Adapter<EpisodesRecyclerViewAdapter.EpisodeItemViewHolder>() {

    private val viewModel: PodcastViewModel
            = PodcastViewModel.getInstance(DataSourceRepo.getInstance(fragment.activity.applicationContext))

    private val ctx: Context

    private val bgColor: Int

    /*to keep track of downloaidng items to send it to details view */
    private var disposableList: MutableMap<String, Disposable> = HashMap()

    init {
        ctx = fragment.activity
        // quick hack to get bgColor
        bgColor = (fragment.view!!.findViewById<View>(R.id.podcast_detail_appbar).background as ColorDrawable)
                .color
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.episode_item_layout, parent, false)
        // set view onClickListener
        val vh = EpisodeItemViewHolder(view)

        // always make the progress to be red
        vh.progressView.finishedColor = ContextCompat.getColor(ctx, R.color.unfin_color)

        return vh
    }

    fun cleanUpAllDisposables() {
        for (disposable in disposableList.values) {
            disposable.dispose()
        }
        disposableList = HashMap()
    }


    override fun onBindViewHolder(holder: EpisodeItemViewHolder, position: Int) {
        val episode = episodeList!![position]
        // load episode info
        holder.nameText.text = episode.title
        holder.setEpisode(episode, position)
        // load date
        if (episode.pubDate != null) {
            val dateParsed = TimeUtil.parseDatePub(episode.pubDate)
            if (dateParsed != null) {
                holder.monthText.text = dateParsed.month + "," + dateParsed.dayOfMonth
                holder.yearText.text = dateParsed.year
            }
        }
        // load size
        if (episode.fileSize != null) {
            holder.fileSize.text = StorageUtil.convertToMbRep(episode.fileSize)
        }

        // load download or play icons depends on podcast url link available or not
        loadCorrectDownOrPlayImg(episode, holder)

        // set on click listener to download file and updat progress
        setViewHolderOnClickListener(holder, episode)

        // set on click listener for episode detail info
        setOnClickListenerForEpisodeInfo(holder, episode)
    }

    override fun getItemCount(): Int {
        return episodeList!!.size
    }


    private fun setOnClickListenerForEpisodeInfo(vh: EpisodeItemViewHolder,
                                                 ep: Episode
    ) {
        vh.mainLayout.setOnClickListener { _ ->
            val intent = Intent(ctx, EpisodeInfoActivity::class.java)
            intent.putExtra(BG_COLOR_KEY, bgColor)
            intent.putExtra(EPISODE_KEY, ep)
            intent.putExtra(PODCAST_IMG_KEY, podcast.imgLocalPath)
            intent.putExtra(DL_TRANS_ID, vh.transId)
            // check downloading status
            if (vh.state == ButtonStateUtil.PRESS_TO_STOP_DOWNLOAD) {
                intent.putExtra(IS_DOWNLOADING_KEY, true)
            }
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            ctx.startActivity(intent)
        }

    }


    /**
     * show porgress view and hide button
     */
    private fun showProgressView(vh: EpisodeItemViewHolder) {
        vh.downPlayImg.visibility = View.GONE
        vh.progressView.visibility = View.VISIBLE
    }

    private fun hideProgressView(vh: EpisodeItemViewHolder) {
        vh.downPlayImg.visibility = View.VISIBLE
        vh.progressView.visibility = View.GONE
    }


    /**
     * Set viewholder logic for pressing download button or play
     */
    private fun setViewHolderOnClickListener(vh: EpisodeItemViewHolder,
                                             episode: Episode) {
        if (episode.downloaded == 1) {
            vh.state = ButtonStateUtil.PRESS_TO_PLAY
            vh.downPlayImg.setImageResource(R.mipmap.ic_ep_play)
        } else {
            vh.state = ButtonStateUtil.PRESS_TO_DOWNLOAD
            vh.downPlayImg.setImageResource(R.mipmap.ic_ep_down)
        }
        // set onclick listener for viewholder
        vh.downPlayImg.setOnClickListener { _ ->
            when (vh.state) {
                ButtonStateUtil.PRESS_TO_DOWNLOAD -> {
                    // check if episode is already state or not
                    // bind download service
                    showProgressView(vh)
                    val res = startDownloadEpisodeFile(episode, vh)
                    // if we start download successflly then change state of the button
                    if (res) {
                        vh.state = ButtonStateUtil.PRESS_TO_STOP_DOWNLOAD
                    }
                }

                ButtonStateUtil.PRESS_TO_STOP_DOWNLOAD -> {
                    if (vh.transId > 0) {
                        fragment.requestStopDownload(vh.transId)
                        hideProgressView(vh)
                    }
                    vh.state = ButtonStateUtil.PRESS_TO_DOWNLOAD
                }

                ButtonStateUtil.PRESS_TO_PLAY -> {
                    // limited to downloaded episode only for now
                    vh.state = ButtonStateUtil.PRESS_TO_PAUSE
                    this@EpisodesRecyclerViewAdapter.fragment.requestToPlaySong(episode)
                    vh.downPlayImg.setImageResource(R.mipmap.ic_pause_for_list)
                }

                ButtonStateUtil.PRESS_TO_PAUSE -> {
                    vh.state = ButtonStateUtil.PRESS_TO_UNPAUSE
                    vh.downPlayImg.setImageResource(R.mipmap.ic_ep_play)
                    this@EpisodesRecyclerViewAdapter.fragment.requestToPause()
                }

                ButtonStateUtil.PRESS_TO_UNPAUSE -> {
                    vh.state = ButtonStateUtil.PRESS_TO_PAUSE
                    vh.downPlayImg.setImageResource(R.mipmap.ic_pause_for_list)
                    this@EpisodesRecyclerViewAdapter.fragment.requestToResume()
                }
            }
        }

    }

    /**
     * start to download episode here
     */
    private fun startDownloadEpisodeFile(episode: Episode,
                                         vh: EpisodeItemViewHolder): Boolean {
        // Now start Downloading
        val url = episode.downloadUrl
        val pathItems = StorageUtil.getPathToStoreEp(episode, fragment.activity.applicationContext)
        if (url != null) {
            val transactionId = fragment.requestDownload(episode, url, pathItems.first, pathItems.second)
            vh.transId = transactionId
            if (transactionId < 0) {
                Log.e(TAG, "Failed to start Download: " + episode.title)
                return false
            }
        } else {
            Log.e(TAG, "Episode Url is null for : " + episode.title)
            return false
        }
        return true
    }


    private fun updateItemData(ep: Episode?, pos: Int) {
        viewModel.getEpisodeObsevable(ep)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : Observer<Episode> {
                    override fun onSubscribe(d: Disposable) {
                        disposableList.put(ep!!.uniqueId, d)
                    }

                    override fun onNext(episode: Episode) {
                        episodeList!!.removeAt(pos)
                        episodeList!!.add(pos, episode)
                        notifyItemChanged(pos)
                    }

                    override fun onError(e: Throwable) {

                    }

                    override fun onComplete() {

                    }
                })

    }

    // suggest to download or play episode
    private fun loadCorrectDownOrPlayImg(ep: Episode, vh: EpisodeItemViewHolder) {
        // check if the file is already state or not
        if (ep.downloaded == 0) {
            vh.downPlayImg.setImageResource(R.mipmap.ic_ep_down)
        } else {
            vh.downPlayImg.setImageResource(R.mipmap.ic_ep_play)
        }
        hideProgressView(vh)
    }


    inner class EpisodeItemViewHolder// bind item to view here
    (itemView: View) : RecyclerView.ViewHolder(itemView) {

        var mainLayout: View
        var downPlayImg: ImageView
        var monthText: TextView
        var nameText: TextView
        var yearText: TextView
        var fileSize: TextView
        var progressView: CircleProgress
        var state = ButtonStateUtil.PRESS_TO_DOWNLOAD
        private var episode: Episode? = null
        private var episodeDisposable: Disposable? = null
        private var mediaDisposable: Disposable? = null
        // quick hack for stop download
        var transId: Long = -1

        /**
         * @param episode
         * @param itemPos
         */
        fun setEpisode(episode: Episode, itemPos: Int) {
            if (this.mediaDisposable != null) {
                this.mediaDisposable!!.dispose()
                this.mediaDisposable = null
            }

            if (this.episodeDisposable != null){
                this.episodeDisposable!!.dispose()
                this.episodeDisposable = null
            }
            // set episode
            this.episode = episode
            // if episode is downloaded then set its listener for media service
            if (episode.downloaded == 1) subscribeToMeidaSerivce()
            // subscribe to episode state
            PodcastViewModel.subscribeEpisodeSubject(object : Observer<EpisodeState> {
                override fun onSubscribe(d: Disposable) {
                    // dispose old disposable also
                    if (this@EpisodeItemViewHolder.episodeDisposable != null) {
                        this@EpisodeItemViewHolder
                                .episodeDisposable!!.dispose()
                    }
                    this@EpisodeItemViewHolder.episodeDisposable = d
                }

                override fun onNext(epState: EpisodeState) {
                    if (epState.uniqueId == this@EpisodeItemViewHolder.episode!!.uniqueId) {
                        val ep = this@EpisodeItemViewHolder.episode
                        val vh = this@EpisodeItemViewHolder
                        when (epState.state) {
                            EpisodeState.EPISODE_DOWNLOADING -> {
                                Log.d(TAG, "downloading update for episode" + ep!!.toString())
                                showProgressView(vh)
                                vh.progressView.progress = epState.downloadProgress
                                vh.state = ButtonStateUtil.PRESS_TO_STOP_DOWNLOAD
                            }
                            EpisodeState.EPISODE_FETCHED -> {
                                this@EpisodesRecyclerViewAdapter.updateItemData(ep,
                                        itemPos)
                                vh.state = ButtonStateUtil.PRESS_TO_DOWNLOAD
                            }
                            EpisodeState.EPISODE_DOWNLOADED -> {
                                this@EpisodesRecyclerViewAdapter.updateItemData(ep, itemPos)
                                vh.state = ButtonStateUtil.PRESS_TO_PLAY
                                vh.downPlayImg.setImageResource(R.mipmap.ic_ep_play)
                                // subscribe to media service
                                subscribeToMeidaSerivce()
                            }
                        }
                    }
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                }

                override fun onComplete() {

                }
            })

        }

        private fun subscribeToMeidaSerivce() {
            MediaPlayBackService.subscribeMediaPlaybackSubject(object : Observer<android.util.Pair<Episode?, Int>> {
                override fun onSubscribe(d: Disposable) {
                    if (this@EpisodeItemViewHolder.mediaDisposable != null) this@EpisodeItemViewHolder.mediaDisposable!!.dispose()
                    this@EpisodeItemViewHolder.mediaDisposable = d
                }

                override fun onNext(info: android.util.Pair<Episode?, Int>) {
                    val ep = info.first
                    val vh = this@EpisodeItemViewHolder
                    if (ep != null && ep.uniqueId == vh.episode!!.uniqueId) {
                        val state = info.second
                        when (state) {
                            MediaPlayBackService.MEDIA_PAUSE -> {
                                vh.state = ButtonStateUtil.PRESS_TO_UNPAUSE
                                vh.downPlayImg.setImageResource(R.mipmap.ic_ep_play)
                            }
                            MediaPlayBackService.MEDIA_PLAYING -> {
                                vh.state = ButtonStateUtil.PRESS_TO_PAUSE
                                vh.downPlayImg.setImageResource(R.mipmap.ic_pause_for_list)
                            }
                            MediaPlayBackService.MEDIA_REMOVED_FROM_PLAYLIST, MediaPlayBackService.MEDIA_STOPPED -> {
                                vh.state = ButtonStateUtil.PRESS_TO_PLAY
                                vh.downPlayImg.setImageResource(R.mipmap.ic_ep_play)
                            }
                        }
                    }
                }

                override fun onError(e: Throwable) {

                }

                override fun onComplete() {

                }
            })
        }


        init {
            mainLayout = itemView.findViewById(R.id.episode_main_view_layout)
            downPlayImg = itemView.findViewById<View>(R.id.episode_down_play_img) as ImageView
            nameText = itemView.findViewById<View>(R.id.episode_name) as TextView
            monthText = itemView.findViewById<View>(R.id.episode_month_text) as TextView
            yearText = itemView.findViewById<View>(R.id.episode_year_text) as TextView
            fileSize = itemView.findViewById<View>(R.id.episode_file_size) as TextView
            progressView = itemView.findViewById<View>(R.id.circle_progress) as CircleProgress
        }
    }

    fun setEpisodeList(episodeList: MutableList<Episode>) {
        this.episodeList = episodeList
    }

    companion object {

        val TAG = EpisodesRecyclerViewAdapter::class.java.simpleName
        val PODCAST_KEY = "podcast_key"
        val DL_TRANS_ID = "dl_trans_id"
        val BG_COLOR_KEY = "bg_color"
        val PODCAST_IMG_KEY = "podcast_img"
        val EPISODE_KEY = "episode"
        val IS_DOWNLOADING_KEY = "is_downloading"
    }
}