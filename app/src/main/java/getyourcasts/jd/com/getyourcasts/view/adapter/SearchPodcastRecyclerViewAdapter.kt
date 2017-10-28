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

import java.util.ArrayList

import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import getyourcasts.jd.com.getyourcasts.repository.remote.network.NetworkHelper
import getyourcasts.jd.com.getyourcasts.util.StorageUtil
import getyourcasts.jd.com.getyourcasts.view.PodcastDetailsActivity
import getyourcasts.jd.com.getyourcasts.view.SearchPodcastFragment
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastState
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

/**
 * Created by chuondao on 9/10/17.
 */

class SearchPodcastRecyclerViewAdapter(podcastList: ArrayList<Podcast>, internal var fragment: SearchPodcastFragment) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    var podcastList: List<Podcast>
    private val viewModel: PodcastViewModel
    private val ctx: Context
    private var disposableList: MutableList<Disposable> = ArrayList()


    init {
        this.podcastList = podcastList
        viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(fragment.context))
        ctx = fragment.context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.podcast_item_layout, parent, false)
        // set view onClickListener
        return PodcastItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val podcast = podcastList[position]
        val podcastVh = holder as PodcastItemViewHolder
        podcastVh.author.text = podcast.artistName
        podcastVh.title.text = podcast.collectionName
        // need glide to load the image here
        val checkPodcastDbObs = viewModel.getPodcastObservable(podcast.collectionId)
        // check to decide where to load image
        checkPodcastDbObs.observeOn(AndroidSchedulers.mainThread()).subscribe(
                object : Observer<Podcast> {
                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onNext(it: Podcast) {
                        // this true mean podcast is already subscribed
                        var podcastToPass: Podcast? = null
                        if (it.collectionId.trim { it <= ' ' } != "") {
                            podcastVh.downloadedView.setImageResource(R.mipmap.ic_downloaded)
                            // disable on click listener
                            Log.d(TAG, "Load image from local path \${it.imgLocalPath}")
                            GlideApp.with(fragment).load(it.imgLocalPath.trim { it <= ' ' }).into(podcastVh.imgView)
                            podcastToPass = it
                        } else {
                            podcastVh.downloadedView.setImageResource(R.mipmap.ic_subscribe)
                            if (podcast.artworkUrl100 != null) {
                                GlideApp.with(fragment).load(podcast.artworkUrl100.trim { it <= ' ' }).into(podcastVh.imgView)
                            }
                            podcastToPass = podcast
                        }

                        // subscribe to listen to change in podcast
                        subscribeToPodcastUpdate(podcastToPass.collectionId, position)
                        // set onclickListenter to launch details podcast
                        val finalPodcastToPass = podcastToPass
                        podcastVh.itemView.setOnClickListener { viewItem ->

                            // need to get the podcast from db to make sure it's updated
                            viewModel.getPodcastObservable(finalPodcastToPass.collectionId)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                            object : Observer<Podcast> {
                                                override fun onSubscribe(d: Disposable) {

                                                }

                                                override fun onNext(it: Podcast) {
                                                    val intent = Intent(this@SearchPodcastRecyclerViewAdapter
                                                            .ctx, PodcastDetailsActivity::class.java)
                                                    // just in case the podcast is not in the db
                                                    if (it.collectionId == "") {
                                                        intent.putExtra(PODCAST_KEY, podcast)
                                                    } else {
                                                        intent.putExtra(PODCAST_KEY, it)
                                                    }

                                                    intent.putExtra(ITEM_POS_KEY, position)
                                                    fragment.activity.startActivityForResult(intent, REQUEST_CODE)
                                                }

                                                override fun onError(e: Throwable) {
                                                    e.printStackTrace()
                                                    Log.e(TAG, "Unexpected Error before launch detailed podcast " + "activity")
                                                }

                                                override fun onComplete() {

                                                }
                                            }
                                    )


                        }


                    }

                    override fun onError(e: Throwable) {
                        e.printStackTrace()
                    }

                    override fun onComplete() {

                    }
                }
        )

        // set onclick listener for download image locally and insert podcast to db
        podcastVh.downloadedView.setOnClickListener { view ->
            if (!NetworkHelper.isConnectedToNetwork(this@SearchPodcastRecyclerViewAdapter.ctx)) {
                NetworkHelper.showNetworkErrorDialog(ctx)
                return@setOnClickListener
            }
            viewModel.getSubscribeObservable(podcast)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<Boolean> {
                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onNext(it: Boolean) {
                            if (it!!) {
                                Log.d(SearchPodcastRecyclerViewAdapter.TAG,
                                        "Insert Podcast To DB Complete \${podcast.collectionName}")
                                StorageUtil.startGlideImageDownload(podcast, ctx)
                                // change the icon
                                podcastVh.downloadedView.setImageResource(R.mipmap.ic_downloaded)

                            } else {
                                Log.e(SearchPodcastRecyclerViewAdapter.TAG, "Insert Podcast to DB " + "Failed. Maybe a duplicate  ")
                            }
                        }

                        override fun onError(it: Throwable) {
                            it.printStackTrace()
                        }

                        override fun onComplete() {

                        }
                    })
        }

    }

    override fun getItemCount(): Int {
        return podcastList.size
    }

    private fun subscribeToPodcastUpdate(podcastId: String, pos: Int) {
        PodcastViewModel.subscribePodcastSubject(
                object : Observer<PodcastState> {
                    override fun onSubscribe(d: Disposable) {
                        disposableList.add(d)
                    }

                    override fun onNext(t: PodcastState) {
                        if (t.uniqueId == podcastId) {
                            // only the button and state have to change
                            this@SearchPodcastRecyclerViewAdapter.notifyItemChanged(pos)
                        }
                    }

                    override fun onError(e: Throwable) {

                    }

                    override fun onComplete() {

                    }
                }
        )
    }

    fun cleanUpAllDisposable() {
        for (d in disposableList) {
            d.dispose()
        }
        disposableList = ArrayList()
    }

    internal inner class PodcastItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var imgView: ImageView
        var author: TextView
        var title: TextView
        var downloadedView: ImageView

        init {
            imgView = itemView.findViewById(R.id.podcast_image)
            author = itemView.findViewById(R.id.podcast_author)
            title = itemView.findViewById(R.id.podcast_title)
            downloadedView = itemView.findViewById(R.id.podcast_downloaded_img)
        }
    }

    companion object {

        private val TAG = "PocastAdapter"
        private val PODCAST_KEY = "podcast_key"
        private val ITEM_POS_KEY = "item_pos_key"
        private val REQUEST_CODE = 1
    }
}
