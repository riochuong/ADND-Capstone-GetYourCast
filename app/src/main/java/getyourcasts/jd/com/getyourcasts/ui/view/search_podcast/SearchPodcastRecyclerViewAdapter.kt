package getyourcasts.jd.com.getyourcasts.ui.view.search_podcast

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.annotation.GlideModule
import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import getyourcasts.jd.com.getyourcasts.viewmodel.SearchPodcastViewModel
import io.reactivex.android.schedulers.AndroidSchedulers


/**
 * Created by chuondao on 7/26/17.
 */

class SearchPodcastRecyclerViewAdapter(var podcastList: List<Podcast>,
                                       val fragment: SearchPodcastFragment) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val viewModel : SearchPodcastViewModel

    private val ctx: Context

    init {
        viewModel = fragment.searchViewModel
        ctx = fragment.context
    }


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.podcast_item_layout, parent, false)
        val vh = PodcastItemViewHolder(view)
        return vh
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val podcast = podcastList[position]
        val podcastVh = holder as PodcastItemViewHolder
        podcastVh.author.text = podcast.artistName
        podcastVh.title.text = podcast.collectionName
        // need glide to load the image here
        val checkPodcastDbObs = viewModel.getIsPodcastInDbObservable(podcast.collectionId)
        // check to decide where to load image
        checkPodcastDbObs.observeOn(AndroidSchedulers.mainThread()).subscribe(
                {
                    // this true mean podcast is already subscribed
                    if (it){
                        podcastVh.imgView.setImageResource(R.mipmap.ic_downloaded)
                        Glide.with(fragment).load(podcast.imgLocalPath!!.trim()).into(podcastVh.imgView)
                    }
                    else{
                        podcastVh.imgView.setImageResource(R.mipmap.ic_todownload)
                        Glide.with(fragment).load(podcast.imgUrl!!.trim()).into(podcastVh.imgView)
                    }
                },
                {
                    it.printStackTrace()
                } // failed to check
        )
    }

    override fun getItemCount(): Int {
        if (podcastList != null){
            return podcastList.size
        }
        return 0
    }

}

class PodcastItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

    val imgView : ImageView
    val author: TextView
    val title: TextView
    val downloadedView: ImageView

    // bind item to view here
    init {
            imgView = itemView.findViewById(R.id.podcast_image) as ImageView
            author = itemView.findViewById(R.id.podcast_author) as TextView
            title = itemView.findViewById(R.id.podcast_title) as TextView
            downloadedView = itemView.findViewById(R.id.podcast_downloaded_img) as ImageView
    }
}