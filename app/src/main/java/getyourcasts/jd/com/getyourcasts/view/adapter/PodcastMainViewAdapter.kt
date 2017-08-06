package getyourcasts.jd.com.getyourcasts.view.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp

/**
 * Created by chuondao on 8/5/17.
 */


class PodcastMainViewAdapter(var podcastList: MutableList<Podcast>, val ctx: Context): RecyclerView
.Adapter<PodcastViewHolder>
()
{
    
    override fun onBindViewHolder(holder: PodcastViewHolder?, position: Int) {
        // load image to img view
        val ep = podcastList[position]
        if (holder != null && ep.imgLocalPath != null) {
            GlideApp.with(this.ctx).load(ep.imgLocalPath).into(holder.imgView)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): PodcastViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.subscribed_pod_layout_item, parent, false)
        val vh = PodcastViewHolder(view)
        return vh
    }

    override fun getItemCount(): Int {
            return podcastList.size
    }


}



class PodcastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    val imgView: ImageView
    init {
        imgView = itemView.findViewById(R.id.subscribed_pod_img) as ImageView
    }
}