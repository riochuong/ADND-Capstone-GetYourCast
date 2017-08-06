package getyourcasts.jd.com.getyourcasts.view

import android.opengl.Visibility
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.repository.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import getyourcasts.jd.com.getyourcasts.view.adapter.SearchPodcastRecyclerViewAdapter
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_podcast_detail_layout.*
import kotlinx.android.synthetic.main.search_podcast_fragment.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the [SearchPodcastFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchPodcastFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    lateinit var searchViewModel: PodcastViewModel
    private lateinit var searchAdapter: SearchPodcastRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.search_podcast_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchViewModel = PodcastViewModel(DataSourceRepo.getInstance(this.context))
        recyclerView = podcast_list_recycler_view
        setupRecyclerView(recyclerView)
        searchAdapter = SearchPodcastRecyclerViewAdapter(ArrayList<Podcast>(), this)
        recyclerView.adapter = searchAdapter

        // SEARCH SUMISSION
        search_term_text.setOnEditorActionListener(
                object : TextView.OnEditorActionListener {
                    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {

                        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                            // search for podcast here
                            startLoadingAnim()
                            val searchTerm = search_term_text.text.toString()
                            val searchObsrv = searchViewModel.getPodcastSearchObservable(searchTerm)
                            searchObsrv.observeOn(AndroidSchedulers.mainThread()).subscribe(
                                    // OnNext
                                    {
                                        updatePodcastList(it)
                                        stopLoadingAnim()
                                    },
                                    // OnError
                                    {
                                        Log.e(TAG, "Error on retreiving podcast results")
                                        it.printStackTrace()
                                    }
                            )
                            return true
                        }
                        return false

                    }
                })


    }

    /**
     * update adapter with newdata from search
     * @newData : newData passed from the results of network fetching
     */
    fun updatePodcastList(newData: List<Podcast>) {
        searchAdapter.podcastList = newData
        searchAdapter.notifyDataSetChanged()
        if (newData != null && newData.size > 0){
            search_empty_view.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
        else{
            recyclerView.visibility = View.INVISIBLE
            search_empty_view.visibility = View.VISIBLE
        }

    }

    fun setupRecyclerView(recyclerView: RecyclerView) {
        val layoutManager = LinearLayoutManager(this.context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.

         * @return A new instance of fragment SearchPodcastFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(): SearchPodcastFragment {
            val fragment = SearchPodcastFragment()
            return fragment
        }

        val TAG = "SEARCH_PODCAST"
    }

    private fun startLoadingAnim() {
        recyclerView.visibility = View.INVISIBLE
        search_empty_view.visibility = View.GONE
        searching_prog_view.visibility = View.VISIBLE
        searching_prog_view.show()
    }

    private fun stopLoadingAnim() {
        searching_prog_view.hide()
        searching_prog_view.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        searchAdapter.cleanUpAllDisposable()
    }
}// Required empty public constructor
