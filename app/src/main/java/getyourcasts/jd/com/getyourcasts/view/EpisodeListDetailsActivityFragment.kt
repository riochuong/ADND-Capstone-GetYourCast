package getyourcasts.jd.com.getyourcasts.view

import android.support.v4.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import getyourcasts.jd.com.getyourcasts.R

/**
 * A placeholder fragment containing a simple view.
 */
class EpisodeListDetailsActivityFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_episode_list_details, container, false)
    }
}
