package getyourcasts.jd.com.getyourcasts.ui.view.search_podcast

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import getyourcasts.jd.com.getyourcasts.R

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
        recyclerView = podcast_list_recycler_view
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
    }

}// Required empty public constructor
