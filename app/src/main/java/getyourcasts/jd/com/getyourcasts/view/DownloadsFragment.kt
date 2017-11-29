package getyourcasts.jd.com.getyourcasts.view

import android.support.v4.app.Fragment
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import java.util.ArrayList

import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.view.adapter.DownloadedEpisodesAdapter
import getyourcasts.jd.com.getyourcasts.viewmodel.DownloadedEpisodesLoader

/**
 * A placeholder fragment containing a simple view.
 */
class DownloadsFragment : Fragment(), LoaderManager.LoaderCallbacks<List<Episode>> {

    private lateinit var downloaded_eps_recycler_view: RecyclerView
    private lateinit var adapter: DownloadedEpisodesAdapter
    private lateinit var downloadedEmptyView: TextView
    private lateinit var loader : LoaderManager

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater!!.inflate(R.layout.fragment_downloads, container, false)
        downloaded_eps_recycler_view = root.findViewById(R.id.downloads_recycler_view)
        downloadedEmptyView = root.findViewById(R.id.downloaded_empty_view)
        adapter = DownloadedEpisodesAdapter(ArrayList(), this)
        loader = activity.supportLoaderManager
        return root
    }

    private fun setUpRecyclerView() {
        val lm = LinearLayoutManager(this.context)
        lm.orientation = LinearLayoutManager.VERTICAL
        downloaded_eps_recycler_view.layoutManager = lm
        downloaded_eps_recycler_view.adapter = adapter
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        setUpRecyclerView()
        loader.initLoader(DOWNLOAD_LOADER_ID, Bundle(), this).forceLoad()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<List<Episode>> {
        if (id != DOWNLOAD_LOADER_ID) {
            throw IllegalArgumentException("Wrong loader id received...must be weird")
        }

        return DownloadedEpisodesLoader(activity)
    }

    override fun onLoadFinished(loader: Loader<List<Episode>>, episodeList: List<Episode>) {
        adapter.updateEpisodeList(episodeList.toMutableList())
        // show empty views if list is empty
        downloadedEmptyView.visibility = if (episodeList.isEmpty()) {
            View.VISIBLE
        }
        else {
            View.GONE
        }
    }

    override fun onLoaderReset(loader: Loader<List<Episode>>) {

    }

    companion object {

        private val DOWNLOAD_LOADER_ID = 929
    }
}
