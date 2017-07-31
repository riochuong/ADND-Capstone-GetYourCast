package getyourcasts.jd.com.getyourcasts.view.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.github.lzyzsd.circleprogress.CircleProgress
import com.tonyodev.fetch.Fetch
import com.tonyodev.fetch.listener.FetchListener
import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.repository.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.util.StorageUtil
import getyourcasts.jd.com.getyourcasts.util.TimeUtil
import getyourcasts.jd.com.getyourcasts.view.EpisodeListFragment
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel


/**
 * Created by chuondao on 7/26/17.
 */

class EpisodesRecyclerViewAdapter(var episodeList: List<Episode>,
                                  val fragment: EpisodeListFragment) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val viewModel: PodcastViewModel

    private val ctx: Context

    init {
        viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(fragment.context))
        ctx = fragment.context
    }

    companion object {
        val TAG = "PocastAdapter"
        val PODCAST_KEY = "podcast_key"
        val ITEM_POS_KEY = "item_pos_key"
        val REQUEST_CODE = 1
    }


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.episode_item_layout, parent, false)
        // set view onClickListener
        val vh = EpisodeItemViewHolder(view)
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
        vh.downPlayImg.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                // check if
                if (episode.downloaded == 0) {
                    // Now start Downloading
                    val url = episode.downloadUrl
                    val pairItems = StorageUtil.getPathToStoreEp(episode.podcastId, episode, fragment.context)
                    if (url != null) {
                        val transactionId = fragment.requestDownload(url, pairItems!!.first, pairItems.second)

                        if (transactionId > 0) {
                            // disable play view and show progress
                            vh.downPlayImg.visibility = View.GONE
                            vh.progressView.visibility = View.VISIBLE
                            // register listener
                            // TODO: refactor and extends this class
                            val listener = object : FetchListener {
                                override fun onUpdate(id: Long, status: Int, progress: Int, downloadedBytes: Long, fileSize: Long, error: Int) {
                                    // if this is the id that we are concerned
                                    if (id == transactionId) {
                                        when (status) {
                                            Fetch.STATUS_DOWNLOADING -> {
                                                vh.progressView.progress = progress
                                            }
                                            Fetch.STATUS_DONE ->{
                                                vh.downPlayImg.setImageResource(R.mipmap.ic_ep_play)
                                                vh.downPlayImg.visibility = View.VISIBLE
                                                vh.progressView.visibility = View.GONE
                                                // TODO: update database that the episode is already downloaded with
                                                // correct local url
                                            }
                                        }
                                    } else if (error != Fetch.NO_ERROR){
                                        Log.e(TAG, "Error happens ${error.toString()}")
                                        // TODO: Show snackbar or sth
                                    }
                                }

                            }
                            fragment.registerListener(transactionId, listener)

                        } else {
                            Log.e(TAG, "Failed to start Download file ${episode.downloadUrl}")

                        }


                        // register listener for progress update and also update DB later on


                    }
                }
            }

        })
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

    val downPlayImg: ImageView
    val monthText: TextView
    val nameText: TextView
    val yearText: TextView
    val fileSize: TextView
    val progressView: CircleProgress

    // bind item to view here
    init {
        downPlayImg = itemView.findViewById(R.id.episode_down_play_img) as ImageView
        nameText = itemView.findViewById(R.id.episode_name) as TextView
        monthText = itemView.findViewById(R.id.episode_month_text) as TextView
        yearText = itemView.findViewById(R.id.episode_year_text) as TextView
        fileSize = itemView.findViewById(R.id.episode_file_size) as TextView
        progressView = itemView.findViewById(R.id.circle_progress) as CircleProgress
    }
}