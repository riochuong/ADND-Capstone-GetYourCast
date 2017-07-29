package getyourcasts.jd.com.getyourcasts.view.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import getyourcasts.jd.com.getyourcasts.util.StorageUtil
import getyourcasts.jd.com.getyourcasts.view.PodcastDetailLayoutActivity
import getyourcasts.jd.com.getyourcasts.view.SearchPodcastFragment
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel
import io.reactivex.android.schedulers.AndroidSchedulers


/**
 * Created by chuondao on 7/26/17.
 */

class SearchPodcastRecyclerViewAdapter(var podcastList: List<Podcast>,
                                       val fragment: SearchPodcastFragment) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val viewModel: PodcastViewModel

    private val ctx: Context

    init {
        viewModel = fragment.searchViewModel
        ctx = fragment.context
    }

    companion object {
        val TAG = "PocastAdapter"
        val PODCAST_KEY = "podcast_key"
    }


    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.podcast_item_layout, parent, false)
        // set view onClickListener
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
                    var podcastToPass : Podcast? = null
                    if (! it.collectionId.trim().equals("")) {
                        podcastVh.downloadedView.setImageResource(R.mipmap.ic_downloaded)
                        // disable on click listener
                        Log.d(TAG,"Load image from local path ${it.imgLocalPath}")
                        GlideApp.with(fragment).load(it.imgLocalPath!!.trim()).into(podcastVh.imgView)
                        podcastToPass = it
                    } else {
                        podcastVh.downloadedView.setImageResource(R.mipmap.ic_subscribe)
                        if (podcast.artworkUrl100 != null) {
                            GlideApp.with(fragment).load(podcast.artworkUrl100.trim()).into(podcastVh.imgView)
                        }
                        podcastToPass = podcast
                    }

                    // set onclickListenter to launch details podcast
                    podcastVh.itemView.setOnClickListener {
                        val intent = Intent(ctx, PodcastDetailLayoutActivity::class.java)
                        intent.putExtra(PODCAST_KEY, podcastToPass)
                        ctx.startActivity(intent)
                    }
                },
                {
                    it.printStackTrace()
                } // failed to check
        )

        // set onclick listener for download image locally and insert podcast to db
        podcastVh.downloadedView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                // insert into db
                viewModel.getSubscribeObservable(podcast)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                {
                                    // On next
                                    if (it){
                                        Log.d(SearchPodcastRecyclerViewAdapter.TAG,
                                                "Insert Podcast To DB Complete ${podcast.collectionName}")

                                        // change the icon
                                        podcastVh.downloadedView.setImageResource(R.mipmap.ic_downloaded)

                                        // start glide to download image to local storage also
                                        StorageUtil.startGlideImageDownload(podcast, ctx)

                                    } else{
                                        Log.e(SearchPodcastRecyclerViewAdapter.TAG, "Insert Podcast to DB " +
                                                "Failed. Maybe a duplicate  ")
                                    }
                                },
                                {
                                    // ON ERROR
                                    it.printStackTrace()

                                })
            }


        })


    }

    override fun getItemCount(): Int {
        return podcastList.size
    }

}

class PodcastItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val imgView: ImageView
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