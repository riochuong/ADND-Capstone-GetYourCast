package getyourcasts.jd.com.getyourcasts.view

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.util.Pair
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView

import java.util.ArrayList

import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.exoplayer.MediaPlayBackService
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp
import getyourcasts.jd.com.getyourcasts.view.media.MediaServiceBoundListener
import getyourcasts.jd.com.getyourcasts.view.media.PlaybackControlsFragment
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

open class BaseActivity : AppCompatActivity() {

    internal var playbackControlsFragment: PlaybackControlsFragment? = null
    internal var isPlaybackShow = false
    lateinit var controlViewHolder: PlaybackControlViewHolder
    internal lateinit var viewModel: PodcastViewModel
    private var mediaBoundListeners: MutableList<MediaServiceBoundListener> = ArrayList()
    internal var currEpisode: Episode? = null


    /* ============================ CONNECT TO MEDIA SERVICE ========================================= */
    protected var boundToMediaService = false
    protected var mediaService: MediaPlayBackService? = null

    // connection to service
    private var mediaServiceConnection: ServiceConnection? = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            boundToMediaService = true
            mediaService = (service as MediaPlayBackService.MediaPlayBackServiceBinder).service
            controlViewHolder.setMediaService(mediaService)
            broadcastMediaServiceBound()

        }

        override fun onServiceDisconnected(name: ComponentName) {
            boundToMediaService = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(this))
    }

    override fun onStart() {
        super.onStart()
        playbackControlsFragment = supportFragmentManager.findFragmentById(R.id
                .fragment_playback_controls) as PlaybackControlsFragment
        controlViewHolder = PlaybackControlViewHolder(playbackControlsFragment!!.view)
        if (playbackControlsFragment == null) {
            throw IllegalStateException("This activity does not have playback control fragment")
        }
        hidePlaybackControls()
    }

    /* hide fragment playback */
    protected fun hidePlaybackControls() {
        if (playbackControlsFragment != null) {
            supportFragmentManager.beginTransaction().hide(playbackControlsFragment).commit()
            isPlaybackShow = false
        }
    }

    /**
     * show playbacks when ready
     */
    protected fun showPlaybackControls() {
        supportFragmentManager.beginTransaction()
                .show(playbackControlsFragment)
                .commit()
        isPlaybackShow = true
    }

    fun setControlPlaybackInfo(ep: Episode, state: Int) {
        // load img
        viewModel.getPodcastObservable(ep.podcastId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        object : Observer<Podcast> {
                            override fun onSubscribe(d: Disposable) {

                            }

                            override fun onNext(podcast: Podcast) {
                                GlideApp.with(this@BaseActivity)
                                        .load(podcast.imgLocalPath)
                                        .into(controlViewHolder.podcastImg)
                                // set content
                                controlViewHolder.artist.text = podcast.artistName
                                controlViewHolder.title.text = ep.title
                                // set color background for the playback fragment
                                if (playbackControlsFragment != null) {
                                    playbackControlsFragment!!.mainLayout
                                            .setBackgroundColor(Integer.parseInt(podcast.vibrantColor))
                                }
                                if (state == MediaPlayBackService.MEDIA_PLAYING) {
                                    controlViewHolder.actionBtn.setImageResource(R.mipmap.ic_pause_for_list)
                                } else {
                                    controlViewHolder.actionBtn.setImageResource(R.mipmap.ic_ep_play)
                                }
                            }

                            override fun onError(e: Throwable) {

                            }

                            override fun onComplete() {

                            }
                        }
                )


    }

    override fun onPause() {
        super.onPause()
        if (controlViewHolder.actionBtnDisposable != null) {
            controlViewHolder.actionBtnDisposable!!.dispose()
            controlViewHolder.actionBtnDisposable = null
        }

        // cleanup media service
        if (mediaServiceConnection != null && boundToMediaService) {
            this.unbindService(mediaServiceConnection)
            mediaService = null
        }
    }

    override fun onPostResume() {
        super.onPostResume()
        controlViewHolder.subscribeToMediaService()
    }

    /**
     * This viewholder store all the view information of the fragment playback
     */
    inner class PlaybackControlViewHolder(private var mainView: View?) {
        internal var podcastImg: ImageView
        internal var title: TextView
        internal var artist: TextView
        internal var actionBtn: ImageButton
        internal var actionBtnDisposable: Disposable? = null
        private var mediaService: MediaPlayBackService? = null
        internal var actionBtnState = MediaPlayBackService.MEDIA_UNKNOWN_STATE

        fun setMediaService(mediaService: MediaPlayBackService?) {
            this.mediaService = mediaService
        }

        init {
            if (mainView == null) {
                throw IllegalStateException("Fragment root view cannot be null")
            }
            podcastImg = mainView!!.findViewById<ImageView>(R.id.podcastImg)
            title = mainView!!.findViewById<TextView>(R.id.epTitle)
            artist = mainView!!.findViewById<TextView>(R.id.artist)
            actionBtn = mainView!!.findViewById<ImageButton>(R.id.play_pause)
            // set onclick listener for the play/pause button on the playback fragment
            actionBtn.setOnClickListener { _ ->
                when (actionBtnState) {
                    MediaPlayBackService.MEDIA_PLAYING -> {
                        actionBtnState = MediaPlayBackService.MEDIA_PAUSE
                        actionBtn.setImageResource(R.mipmap.ic_ep_play)
                        mediaService!!.pausePlayback()
                    }
                    MediaPlayBackService.MEDIA_PAUSE -> {
                        actionBtnState = MediaPlayBackService.MEDIA_PLAYING
                        actionBtn.setImageResource(R.mipmap.ic_pause_for_list)
                        mediaService!!.resumePlayback()
                    }
                }
            }


        }

        fun subscribeToMediaService() {
            MediaPlayBackService.subscribeMediaPlaybackSubject(object : Observer<Pair<Episode?, Int>> {
                override fun onSubscribe(d: Disposable) {
                    actionBtnDisposable = d
                }

                override fun onNext(info: Pair<Episode?, Int>) {
                    // make sure we have valid epsiode
                    if (info.first == null){
                        return
                    }
                    when (info.second) {
                        MediaPlayBackService.MEDIA_TRACK_CHANGED, MediaPlayBackService.MEDIA_PLAYING -> if (actionBtnState != MediaPlayBackService.MEDIA_PLAYING) {
                            actionBtnState = MediaPlayBackService.MEDIA_PLAYING
                            actionBtn.setImageResource(R.mipmap.ic_pause_for_list)
                            setControlPlaybackInfo(info.first!!, actionBtnState)
                            if (!this@BaseActivity.isPlaybackShow) {
                                this@BaseActivity.showPlaybackControls()
                            }

                        } else if (currEpisode == null || currEpisode!!.uniqueId != info.first!!.uniqueId) {
                            setControlPlaybackInfo(info.first!!, actionBtnState)
                        }
                        MediaPlayBackService.MEDIA_PAUSE, MediaPlayBackService.MEDIA_ADDED_TO_PLAYLIST, MediaPlayBackService.MEDIA_STOPPED -> {
                            if (actionBtnState != MediaPlayBackService.MEDIA_PAUSE) {
                                actionBtnState = MediaPlayBackService.MEDIA_PAUSE
                                actionBtn.setImageResource(R.mipmap.ic_ep_play)
                                setControlPlaybackInfo(info.first!!, actionBtnState)
                            }
                            if (!this@BaseActivity.isPlaybackShow) {
                                this@BaseActivity.showPlaybackControls()
                            }
                        }
                        MediaPlayBackService.MEDIA_PLAYLIST_EMPTY -> this@BaseActivity.hidePlaybackControls()
                    }
                }

                override fun onError(e: Throwable) {
                    e.printStackTrace()
                }

                override fun onComplete() {

                }
            })
        }


    }


    private fun bindMediaService() {
        val intent = Intent(this, MediaPlayBackService::class.java)
        this.bindService(intent, mediaServiceConnection, Context.BIND_AUTO_CREATE)
    }


    override fun onResume() {
        super.onResume()
        bindMediaService()
    }

    fun registerMediaServiceBoundListenter(listener: MediaServiceBoundListener) {
        mediaBoundListeners.add(listener)
        broadcastMediaServiceBound()
    }

    protected fun broadcastMediaServiceBound() {
        if (mediaService != null && boundToMediaService) {
            for (l in mediaBoundListeners) {
                l.onMediaServiceBound(mediaService)
            }
        }
    }

}
