package getyourcasts.jd.com.getyourcasts.view.adapter

import android.content.Intent
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import getyourcasts.jd.com.getyourcasts.view.DownloadsFragment
import getyourcasts.jd.com.getyourcasts.view.EpisodeInfoActivity
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp
import getyourcasts.jd.com.getyourcasts.viewmodel.EpisodeState
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

/**
 * Created by chuondao on 9/9/17.
 */

class DownloadedEpisodesAdapter(internal var episodeList: MutableList<Episode>?, internal var fragment: DownloadsFragment) :
                RecyclerView.Adapter<DownloadedEpisodesAdapter.DownloadedEpViewHolder>() {
    internal var viewModel: PodcastViewModel

    init {
        this.viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(fragment.context))
    }

    fun updateEpisodeList(updatedData: MutableList<Episode>) {
        this.episodeList = updatedData
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadedEpViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.download_item_layout, parent, false)
        return DownloadedEpViewHolder(view)
    }

    override fun onBindViewHolder(holder: DownloadedEpViewHolder, position: Int) {
        val ep = episodeList!![position]
        holder.epTitle.text = ep.title
        // remove item from list
        holder.removeImg.setOnClickListener {
            episodeList!!.removeAt(holder.adapterPosition)
            notifyItemRemoved(holder.adapterPosition)
            viewModel.deleteDownloadedEpisode(ep)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<Boolean> {
                        override fun onSubscribe(d: Disposable) {

                        }
                        override fun onNext(res: Boolean) {
                            PodcastViewModel.updateEpisodeSubject(EpisodeState(ep.uniqueId,
                                    EpisodeState.DELETED, 0))
                            Log.d(TAG, "Successfully remove downloaded episode " + ep.title)
                        }

                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                        }

                        override fun onComplete() {

                        }
                    })
        }
        // get podcast from db to load image
        viewModel.getPodcastObservable(ep.podcastId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        object : Observer<Podcast> {
                            override fun onSubscribe(d: Disposable) {

                            }

                            override fun onNext(podcast: Podcast) {
                                // now load image to imgview
                                GlideApp.with(this@DownloadedEpisodesAdapter.fragment.context)
                                        .load(podcast.imgLocalPath)
                                        .into(holder.podImg)
                                holder.mainLayout.setOnClickListener {
                                    // start episode info details layout.
                                    val startEpInfo = Intent(fragment.context,
                                            EpisodeInfoActivity::class.java)
                                    startEpInfo.putExtra(EPISODE_KEY, ep)
                                    startEpInfo.putExtra(BG_COLOR_KEY, Integer.parseInt(podcast
                                            .vibrantColor))
                                    startEpInfo.putExtra(PODAST_IMG_KEY, podcast.imgLocalPath)
                                    fragment.context.startActivity(startEpInfo)
                                }
                            }

                            override fun onError(e: Throwable) {

                            }

                            override fun onComplete() {

                            }
                        }
                )

    }

    override fun getItemCount(): Int {
        return if (episodeList != null) {
            episodeList!!.size
        } else 0
    }

    inner class DownloadedEpViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var podImg: ImageView
        var epTitle: TextView
        var removeImg: ImageView
        var mainLayout: CardView

        init {
            podImg = itemView.findViewById(R.id.download_ep_img)
            epTitle = itemView.findViewById(R.id.download_ep_title)
            removeImg = itemView.findViewById(R.id.download_remove_img)
            mainLayout = itemView.findViewById(R.id.download_item_main_layout) 
        }
    }

    companion object {

        private val TAG = DownloadedEpisodesAdapter::class.java.simpleName

        private val EPISODE_KEY = "episode"
        private val BG_COLOR_KEY = "bg_color"
        private val PODAST_IMG_KEY = "podcast_img"
    }
}
