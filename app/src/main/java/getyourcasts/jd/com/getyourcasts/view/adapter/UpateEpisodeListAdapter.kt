package getyourcasts.jd.com.getyourcasts.view.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import java.util.HashMap

import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import getyourcasts.jd.com.getyourcasts.view.EpisodeListActivity
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp

import getyourcasts.jd.com.getyourcasts.view.adapter.EpisodesRecyclerViewAdapter.PODCAST_KEY

/**
 * Created by chuondao on 8/18/17.
 */

class UpateEpisodeListAdapter(private var epUpdateMap: Map<Podcast, List<Episode>>?, private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // quick hack for episode to acquire parent podcast
    private var podcastMap: MutableMap<String, Podcast>? = null
    private var podIndexMap: MutableMap<Int, Podcast>? = null
    /*map between ep index and ep*/
    private var epIndexMap: MutableMap<Int, Episode>? = null

    init {
        calculatePosition()
    }

    fun setEpUpdateMap(epUpdateMap: Map<Podcast, List<Episode>>) {
        this.epUpdateMap = epUpdateMap
        calculatePosition()
        notifyDataSetChanged()
    }

    private fun calculatePosition() {
        var index = 0
        // add pocast index to map
        podIndexMap = HashMap()
        podcastMap = HashMap()
        epIndexMap = HashMap()
        for (pod in epUpdateMap!!.keys) {
            podIndexMap!!.put(index++, pod)
            podcastMap!!.put(pod.collectionId, pod)
            // add episode index to map
            for (ep in epUpdateMap!![pod]!!) {
                epIndexMap!!.put(index++, ep)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_POD) {
            val podView = LayoutInflater.from(parent.context).inflate(R.layout.update_pod_title_layout, parent,
                    false)
            return UpdatePodViewHolder(podView)
        } else {
            val podView = LayoutInflater.from(parent.context).inflate(R.layout.episode_playlist_item_layout, parent,
                    false)
            // hide the remove button
            val imgBtn = podView.findViewById(R.id.remove_item_img) as ImageView
            imgBtn.visibility = View.GONE
            return UpdateEpViewHolder(podView)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (podIndexMap!!.containsKey(position)) {
            VIEW_TYPE_POD
        } else VIEW_TYPE_EPISODE
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_POD) {
            // view type podcast
            val pod = podIndexMap!![position]
            (holder as UpdatePodViewHolder).podTitle.text = pod!!.collectionName
            if (pod.artistName != null) holder.artistTitle.text = pod.artistName
            GlideApp.with(context)
                    .load(pod.imgLocalPath)
                    .into(holder.podImg)

        } else {
            // view type episode
            val ep = epIndexMap!![position]
            // load podcast image to ep item
            val relatedPodcast = podcastMap!![ep!!.podcastId]
            val epViewHolder = holder as UpdateEpViewHolder
            epViewHolder.epTitle.text = ep.title
            epViewHolder.epPubDate.text = ep.pubDate
            epViewHolder.mainLayout.setOnClickListener { _ ->
                val intent = Intent(this.context, EpisodeListActivity::class.java)
                intent.putExtra(PODCAST_KEY, relatedPodcast)
                this.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        // quick check for error
        val numPod = epUpdateMap!!.keys.size
        var numEp = 0
        if (epUpdateMap == null) {
            return 0
        }

        for (key in epUpdateMap!!.keys) {
            numEp += epUpdateMap!![key]!!.size
        }

        return numEp + numPod
    }


    internal inner class UpdateEpViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgView: ImageView
        var epTitle: TextView
        var epPubDate: TextView
        var mainLayout: CardView

        init {
            imgView = itemView.findViewById(R.id.ep_img) as ImageView
            epTitle = itemView.findViewById(R.id.episode_name) as TextView
            epPubDate = itemView.findViewById(R.id.episode_date) as TextView
            imgView.visibility = View.GONE
            mainLayout = itemView.findViewById(R.id.update_ep_main_view) as CardView
        }
    }

    internal inner class UpdatePodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var podTitle: TextView
        var podImg: ImageView
        var artistTitle: TextView

        init {
            podTitle = itemView.findViewById(R.id.pod_update_title_text) as TextView
            artistTitle = itemView.findViewById(R.id.pod_update_artist_text) as TextView
            podImg = itemView.findViewById(R.id.pod_img) as ImageView
        }
    }

    companion object {
        private val VIEW_TYPE_POD = 0
        private val VIEW_TYPE_EPISODE = 1
    }
}
