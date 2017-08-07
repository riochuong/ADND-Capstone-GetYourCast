package getyourcasts.jd.com.getyourcasts.exoplayer

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import getyourcasts.jd.com.getyourcasts.R
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory

class MediaPlayBackService : Service(), ExoPlayer.EventListener {

    private var  exoPlayer: SimpleExoPlayer? = null
    private var dataSourceFactory : DataSource.Factory? = null
    private var extractorFactory: ExtractorsFactory? = null
    private lateinit var mediaSession: MediaSessionCompat
    private var binder : MediaPlayBackServiceBinder = MediaPlayBackServiceBinder()


    companion object {
        val TAG = MediaPlayBackService::class.java.simpleName
    }

    override fun onBind(intent: Intent): IBinder? {
        // init media player here
        return binder
    }


    private fun initExoPlayer() {
        if (exoPlayer == null){
            exoPlayer = ExoPlayerFactory.newSimpleInstance(this, DefaultTrackSelector())
            dataSourceFactory = buildDataSource()
            extractorFactory = buildExtractorFactory()
        }
    }

    private fun buildMediaSourceFromUrl(localUrl: String): MediaSource {
        val mediaSource =
                ExtractorMediaSource(Uri.parse(localUrl), dataSourceFactory, extractorFactory, null , null)
        return mediaSource
    }

    /**
     * play an audio file from a local url
     */
    fun playLocalUrlAudio(localUrl: String) {
        initExoPlayer()
        exoPlayer!!.addListener(this)
        if (exoPlayer != null){
            exoPlayer!!.prepare(buildMediaSourceFromUrl(localUrl))
            exoPlayer!!.playWhenReady = true
        }
    }


    fun addTrackToPlaylist() {

    }

    fun stopPlayback() {
        if (exoPlayer != null){
            exoPlayer!!.release()
            exoPlayer!!.removeListener(this)
            exoPlayer = null
        }

    }

    fun pausePlayback() {
        if (exoPlayer != null) {
            exoPlayer!!.playWhenReady = false
        }
    }

    inner class MediaPlayBackServiceBinder: Binder(){
        fun getService() : MediaPlayBackService {
            return this@MediaPlayBackService
        }
    }

    private fun buildDataSource(): DataSource.Factory {
        return  DefaultDataSourceFactory(this, resources.getString(R.string.app_name))
    }

    private fun buildExtractorFactory(): ExtractorsFactory {
        return DefaultExtractorsFactory()
    }



    /* ====== EXOPLAYER LISTENERS ===========*/
    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {

    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {

    }

    override fun onPlayerError(error: ExoPlaybackException?) {

    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {

    }

    override fun onPositionDiscontinuity() {

    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?) {

    }

    override fun onLoadingChanged(isLoading: Boolean) {

    }
}
