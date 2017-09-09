//package getyourcasts.jd.com.getyourcasts.view
//
//import android.content.Intent
//import android.os.Bundle
//import android.support.v4.app.Fragment
//import android.support.v7.widget.GridLayoutManager
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import getyourcasts.jd.com.getyourcasts.R
//import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo
//import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
//import getyourcasts.jd.com.getyourcasts.view.adapter.PodcastMainViewAdapter
//import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastState
//import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel
//import io.reactivex.Observer
//import io.reactivex.android.schedulers.AndroidSchedulers
//import io.reactivex.disposables.Disposable
//import kotlinx.android.synthetic.main.fragment_main_podcast.*
//
//
///**
// * A placeholder fragment containing a simple view.
// */
//class MainPodcastFragment.java : Fragment() {
//    private lateinit var viewModel: PodcastViewModel
//    private lateinit var adapter: PodcastMainViewAdapter
//
//
//    companion object {
//        val TAG = MainPodcastFragment.java::class.java.simpleName
//    }
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
//                              savedInstanceState: Bundle?): View? {
//        viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(context))
//        adapter = PodcastMainViewAdapter(ArrayList<Podcast>(), this)
//        return inflater.inflate(R.layout.fragment_main_podcast, container, false)
//    }
//
//    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        initializeRecyclerView()
//        updateDataToAdapter()
//        setSearchButtonOnClickListener()
//        subscribeToPodcastSubject()
//    }
//
//    private fun getScreenSizes (): Pair<Float,Float> {
//        val displayMetrics = context.resources.displayMetrics
//        return Pair(displayMetrics.widthPixels.toFloat(), displayMetrics.heightPixels.toFloat())
//    }
//
//
//    private fun initializeRecyclerView(){
//        val screenSize = getScreenSizes()
//        val podSize = context.resources.getDimension(R.dimen.pod_img_size)
//        val numCol = screenSize.first / podSize
//        val lm = GridLayoutManager(context,numCol.toInt())
//        subscribed_podcast_recyclerview.layoutManager = lm
//        subscribed_podcast_recyclerview.adapter = adapter
//    }
//
//    private fun updateAdapterData(newData: MutableList<Podcast>) {
//        adapter.podcastList = newData
//        adapter.notifyDataSetChanged()
//    }
//
//    // load data to adapter
//    private fun updateDataToAdapter() {
//        if (viewModel != null) {
//            viewModel.getAllSubscribedPodcastObservable()
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(
//                            // ON NEXT
//                            {
//                                // bind to adapter
//                                updateAdapterData(it)
//                                Log.d(TAG,"Successfully load  all podcast to main view")
//                            },
//                            {
//                                Log.e(TAG, "Failed to load list of subscribed podcast from DB")
//                                it.printStackTrace()
//                            }
//                    )
//        }
//    }
//
//    private fun setSearchButtonOnClickListener () {
//        search_podcast_btn.setOnClickListener {
//            view ->
//            val launchSearch = Intent(context, SearchNewPodcastActivity::class.java)
//            context.startActivity(launchSearch)
//        }
//    }
//
//    private fun subscribeToPodcastSubject () {
//        PodcastViewModel.subscribePodcastSubject(
//                object : Observer<PodcastState> {
//                    override fun onComplete() {
//
//                    }
//
//                    override fun onError(e: Throwable) {
//
//                    }
//
//                    override fun onNext(t: PodcastState) {
//                        when (t.state) {
//                            // need to update the list
//                            PodcastState.SUBSCRIBED, PodcastState.UNSUBSCRIBED -> {
//                                updateDataToAdapter()
//                            }
//                        }
//                    }
//
//                    override fun onSubscribe(d: Disposable) {
//
//                    }
//
//                }
//        )
//
//    }
//
//}
