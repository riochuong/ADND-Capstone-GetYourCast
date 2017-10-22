package getyourcasts.jd.com.getyourcasts.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import java.util.ArrayList

import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.exoplayer.MediaPlayBackService
import getyourcasts.jd.com.getyourcasts.exoplayer.MediaPlayBackService.MEDIA_REMOVED_FROM_PLAYLIST
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import getyourcasts.jd.com.getyourcasts.util.TimeUtil
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp
import getyourcasts.jd.com.getyourcasts.view.media.PlayListFragment
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable


/**
 * Created by chuondao on 8/12/17.
 */

class MediaPlaylistRecyclerAdapter(private val playListFragment: PlayListFragment) :
        RecyclerView.Adapter<MediaPlaylistRecyclerAdapter.PlaylistItemViewHolder>() {
    private var episodeList: MutableList<Episode>? = null
    private val viewModel: PodcastViewModel

    init {
        episodeList = ArrayList()
        viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(this.playListFragment.context))
        // attach touch helper for swipe to remove effects
    }

    /*helper to add / remove item from list */
    fun setEpisodeList(newList: MutableList<Episode>) {
        episodeList = newList
        this.notifyDataSetChanged()
    }

    fun addItemToTopList(ep: Episode) {
        episodeList!!.add(0, ep)
        this.notifyDataSetChanged()
    }

    fun addItemToEndList(ep: Episode) {
        episodeList!!.add(ep)
        this.notifyItemChanged(episodeList!!.size - 1)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistItemViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.episode_playlist_item_layout,
                parent, false)
        return PlaylistItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PlaylistItemViewHolder, position: Int) {
        val ep = episodeList!![position]
        val datePub = TimeUtil.parseDatePub(ep.pubDate)
        holder.epDate.text = datePub!!.month + "," + datePub.dayOfMonth + " " + datePub.year
        holder.epName.text = ep.title
        // get the local podcast image from db and load it
        viewModel.getPodcastObservable(ep.podcastId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        object : Observer<Podcast> {
                            override fun onSubscribe(d: Disposable) {

                            }

                            override fun onNext(podcast: Podcast) {
                                // now load image to imgview
                                GlideApp.with(playListFragment.context)
                                        .load(podcast.imgLocalPath)
                                        .into(holder.podcastImg)
                            }

                            override fun onError(e: Throwable) {

                            }

                            override fun onComplete() {

                            }
                        }
                )

        // set listenter
        setItemRemoveListener(holder, ep)
    }


    private fun setItemRemoveListener(vh: PlaylistItemViewHolder, ep: Episode) {
        vh.itemRemove.setOnClickListener { v ->
            MediaPlayBackService.publishMediaPlaybackSubject(ep, MEDIA_REMOVED_FROM_PLAYLIST)
            episodeList!!.removeAt(vh.adapterPosition)
            if (episodeList!!.size == 0) {
                MediaPlayBackService.publishMediaPlaybackSubject(null, MediaPlayBackService
                        .MEDIA_PLAYLIST_EMPTY)
            }
            notifyItemRemoved(vh.adapterPosition)
        }
    }

    override fun getItemCount(): Int {
        return episodeList!!.size
    }


    inner class PlaylistItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var epDate: TextView
        var podcastImg: ImageView
        var epName: TextView
        var itemRemove: ImageView


        init {
            epDate = itemView.findViewById(R.id.episode_date) as TextView
            epName = itemView.findViewById(R.id.episode_name) as TextView
            podcastImg = itemView.findViewById(R.id.ep_img) as ImageView
            itemRemove = itemView.findViewById(R.id.remove_item_img) as ImageView
        }
    }


}
