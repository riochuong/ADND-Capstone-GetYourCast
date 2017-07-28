package getyourcasts.jd.com.getyourcasts.view

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
import getyourcasts.jd.com.getyourcasts.viewmodel.SearchPodcastViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
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
    lateinit var searchViewModel: SearchPodcastViewModel
    private lateinit var  searchAdapter : SearchPodcastRecyclerViewAdapter

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
        searchViewModel = SearchPodcastViewModel(DataSourceRepo.getInstance(this.context))
        recyclerView = podcast_list_recycler_view
        setupRecyclerView(recyclerView)
        searchAdapter = SearchPodcastRecyclerViewAdapter(ArrayList<Podcast>(), this)
        recyclerView.adapter = searchAdapter
        // EDIT_TEXT LISTENER
        search_term_text.setOnEditorActionListener(
                object : TextView.OnEditorActionListener {
                    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {

                        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                            // search for podcast here
                            val searchTerm = search_term_text.text.toString()
                            val searchObsrv = searchViewModel.getPodcastSearchObservable(searchTerm)
                            searchObsrv.observeOn(AndroidSchedulers.mainThread()).subscribe(
                                    // OnNext
                                    {
                                        updatePodcastList(it)
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
    fun updatePodcastList (newData: List<Podcast>){
        if (searchAdapter != null){
            searchAdapter.podcastList = newData
            searchAdapter.notifyDataSetChanged()
            // make recyclerview visible
            if (newData.size > 0){
                recyclerView.visibility = View.VISIBLE
            }
            else{
                recyclerView.visibility = View.GONE
            }
        }
    }

    fun setupRecyclerView(recyclerView: RecyclerView){
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

        val TAG ="SEARCH_PODCAST"
    }

}// Required empty public constructor
