package getyourcasts.jd.com.getyourcasts.view.adapter

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import getyourcasts.jd.com.getyourcasts.view.EpisodeListActivity
import getyourcasts.jd.com.getyourcasts.view.MainPodcastFragment
import getyourcasts.jd.com.getyourcasts.view.PodcastDetailsFragment
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp

/**
 * Created by chuondao on 9/10/17.
 */

class PodcastMainViewAdapter(internal var podcastList: List<Podcast>, internal var fragment: MainPodcastFragment) :
        RecyclerView.Adapter<PodcastMainViewAdapter.PodcastViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PodcastViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.subscribed_pod_layout_item, parent, false)

        return PodcastViewHolder(view)
    }

    override fun onBindViewHolder(holder: PodcastViewHolder?, position: Int) {
        // load image to img view
        if (holder != null) {
            val podcast = podcastList[position]
            if (podcast.imgLocalPath != null) {
                GlideApp.with(this.fragment.context).load(podcast.imgLocalPath).into(holder.imgView)
            }
            setImgViewOnClick(holder, podcast)
        }
    }

    override fun getItemCount(): Int {
        return podcastList.size
    }

    private fun setImgViewOnClick(vh: PodcastViewHolder, podcast: Podcast) {
        vh.imgView.setOnClickListener { _ ->
            val intent = Intent(fragment.context, EpisodeListActivity::class.java)
            intent.putExtra(PodcastDetailsFragment.PODCAST_KEY, podcast)
            fragment.context.startActivity(intent)
        }

    }

    fun setPodcastList(podcastList: List<Podcast>) {
        this.podcastList = podcastList
    }

    inner class PodcastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgView: ImageView

        init {
            imgView = itemView.findViewById(R.id.subscribed_pod_img) as ImageView
        }
    }
}
