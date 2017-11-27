package getyourcasts.jd.com.getyourcasts.view.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.github.florent37.glidepalette.BitmapPalette
import com.wang.avi.AVLoadingIndicatorView
import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import getyourcasts.jd.com.getyourcasts.repository.remote.network.NetworkHelper
import getyourcasts.jd.com.getyourcasts.util.GlideUtil
import getyourcasts.jd.com.getyourcasts.view.PodcastDetailsActivity
import getyourcasts.jd.com.getyourcasts.view.SearchPodcastFragment
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastState
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.*

/**
 * Created by chuondao on 9/10/17.
 */

class SearchPodcastRecyclerViewAdapter(podcastList: ArrayList<Podcast>, internal var fragment: SearchPodcastFragment) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    var podcastList: List<Podcast> = podcastList
    private val viewModel: PodcastViewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(fragment.context))
    private val ctx: Context = fragment.context
    private var disposableList: MutableList<Disposable> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.podcast_item_layout, parent, false)
        // set view onClickListener
        return PodcastItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val podcast = podcastList[position]
        val podcastVh = holder as PodcastItemViewHolder
        podcastVh.podcast = podcast
        podcastVh.author.text = podcast.artistName
        podcastVh.title.text = podcast.collectionName
        podcastVh.itemPos = position
        podcastVh.progressView.visibility = View.GONE
        podcastVh.subscribeView.visibility = View.VISIBLE


    }

    override fun getItemCount(): Int {
        return podcastList.size
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
        var subscribeView: ImageView
        var progressView: AVLoadingIndicatorView
        var backgroundView: CardView
        var itemPos: Int = -1
        var status = UNSUBSCRIBED
        private var disposable : Disposable? = null
        var podcast : Podcast? = null
            set(value) {
                // reset disposable
                if (disposable != null) disposable!!.dispose()
                disposable = null
                val podcastVh = PodcastItemViewHolder@this
                if (value != null) {
                    viewModel.getPodcastObservable(value.collectionId)
                            .observeOn(AndroidSchedulers.mainThread()).subscribe(
                            object : Observer<Podcast> {
                                override fun onSubscribe(d: Disposable) {

                                }

                                override fun onNext(it: Podcast) {
                                    // this true mean podcast is already subscribed
                                    if (it.collectionId.trim { it <= ' ' } != "") {
                                        podcastVh.subscribeView.setImageResource(R.mipmap.ic_downloaded)
                                        // disable on click listener
                                        Log.d(TAG, "Load image from local path ${it.imgLocalPath}")
//                                        GlideApp.with(fragment).load(it.imgLocalPath.trim { it <= ' ' }).into(podcastVh.imgView)
                                        GlideUtil.loadImageAndSetColorOfViews(
                                                fragment.context,
                                                it.imgLocalPath.trim(),
                                                podcastVh.imgView,
                                                podcastVh.backgroundView,
                                                BitmapPalette.Profile.MUTED_DARK
                                        )
                                        field = it
                                        status = SUBSCRIBED
                                    } else {
                                        podcastVh.subscribeView.setImageResource(R.mipmap.ic_subscribe)
                                        if (value.artworkUrl100 != null) {
                                            GlideUtil.loadImageAndSetColorOfViews(
                                                    fragment.context,
                                                    value.artworkUrl100,
                                                    podcastVh.imgView,
                                                    podcastVh.backgroundView,
                                                    BitmapPalette.Profile.MUTED_DARK
                                                    )
                                        }
                                        field = value
                                        status = UNSUBSCRIBED
                                    }
                                    // set onclickListenter to launch details podcast
                                    podcastVh.itemView.setOnClickListener {
                                        // need to get the podcast from db to make sure it's updated
                                        _ -> viewModel.getPodcastObservable(field!!.collectionId)
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(
                                                    object : Observer<Podcast> {
                                                        override fun onSubscribe(d: Disposable) {

                                                        }

                                                        override fun onNext(it: Podcast) {
                                                            val intent = Intent(this@SearchPodcastRecyclerViewAdapter
                                                                    .ctx, PodcastDetailsActivity::class.java)
                                                            // just in case the podcast is not in the db
                                                            if (it.collectionId.trim() == "") {
                                                                intent.putExtra(PODCAST_KEY, podcast)
                                                            } else {
                                                                intent.putExtra(PODCAST_KEY, it)
                                                            }
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
                                    // set subscriber for listener
                                    PodcastViewModel.subscribePodcastSubject(
                                            object : Observer<PodcastState> {
                                                override fun onSubscribe(d: Disposable) {
                                                    disposable = d
                                                }
                                                override fun onNext(t: PodcastState) {
                                                    if (t.uniqueId == field!!.collectionId) {
                                                        // only the button and state have to change
                                                        this@SearchPodcastRecyclerViewAdapter.notifyItemChanged(itemPos)
                                                    }
                                                }

                                                override fun onError(e: Throwable) {
                                                    e.printStackTrace()
                                                }

                                                override fun onComplete() {

                                                }
                                            }
                                    )
                                }

                                override fun onError(e: Throwable) {
                                    e.printStackTrace()
                                }

                                override fun onComplete() {
                                }
                            }
                    )
                }

            }

        init {
            imgView = itemView.findViewById(R.id.podcast_image)
            author = itemView.findViewById(R.id.podcast_author)
            title = itemView.findViewById(R.id.podcast_title)
            subscribeView = itemView.findViewById(R.id.podcast_downloaded_img)
            progressView = itemView.findViewById(R.id.subscribing_progress_view)
            backgroundView = itemView.findViewById(R.id.movie_detail_fragment)
            subscribeView.setOnClickListener { view ->
                // check if network avaialble
                if (!NetworkHelper.isConnectedToNetwork(this@SearchPodcastRecyclerViewAdapter.ctx)) {
                    NetworkHelper.showNetworkErrorDialog(ctx)
                    return@setOnClickListener
                }
                // if podcast view is not subscribed
                if (status == SUBSCRIBED) {
                    return@setOnClickListener
                }
                // show progress view
                progressView.visibility = View.VISIBLE
                subscribeView.visibility = View.GONE
                viewModel.getSubscribeObservable(podcast)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : Observer<Boolean> {
                            override fun onSubscribe(d: Disposable) {

                            }

                            override fun onNext(it: Boolean) {
                                if (it) {
                                    Log.d(SearchPodcastRecyclerViewAdapter.TAG,
                                            "Insert Podcast To DB Complete ${podcast!!.collectionName}")
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
    }

    companion object {
        const val TAG = "PocastAdapter"
        const val PODCAST_KEY = "podcast_key"
        const val REQUEST_CODE = 1
        const val SUBSCRIBED = 1
        const val UNSUBSCRIBED = 0
    }
}
