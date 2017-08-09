package getyourcasts.jd.com.getyourcasts.view.media

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.exoplayer.MediaPlayBackService

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [MediaPlayerViewFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [MediaPlayerViewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MediaPlayerViewFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_media_player_view, container, false)
    }


    override fun onResume() {
        bindMediaService()
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    private fun bindMediaService() {
        val intent = Intent(this.context, MediaPlayBackService::class.java)
        this.context.bindService(intent, mediaServiceConnection, Context.BIND_AUTO_CREATE)
    }

    /* ============================ CONNECT TO MEDIA SERVICE ========================================= */
    private var boundToMediaService = false
    private var mediaService: MediaPlayBackService? = null

    // connection to service
    private val mediaServiceConnection: ServiceConnection = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName?) {
            boundToMediaService = false
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            boundToMediaService = true
            mediaService = (service as MediaPlayBackService.MediaPlayBackServiceBinder).getService()
        }

    }

}
