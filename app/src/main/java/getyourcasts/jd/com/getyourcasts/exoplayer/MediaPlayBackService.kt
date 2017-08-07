package getyourcasts.jd.com.getyourcasts.exoplayer

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.support.v4.app.NotificationCompat
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
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import java.util.*

class MediaPlayBackService : Service(), ExoPlayer.EventListener {

    private var  exoPlayer: SimpleExoPlayer? = null
    private var dataSourceFactory : DataSource.Factory? = null
    private var extractorFactory: ExtractorsFactory? = null
    private lateinit var mediaSession: MediaSessionCompat
    private var currEpisode: Episode? = null
    private var binder : MediaPlayBackServiceBinder = MediaPlayBackServiceBinder()


    companion object {
        val TAG = MediaPlayBackService::class.java.simpleName
        val NOTIFICATION_ID = 1990

        // state of the playback
        const val MEDIA_PLAYING = 0
        const val MEDIA_PAUSE = 1
        const val MEDIA_STOPPED = 2

        val  MediaPlaybackSubject : Subject<Pair<String, Int>> = BehaviorSubject.create()

        fun subscribeMediaPlaybackSubject(obsvr: Observer<Pair<String, Int>>) {
            MediaPlaybackSubject.observeOn(AndroidSchedulers.mainThread()).subscribe(obsvr)
        }

        fun publishMediaPlaybackSubject(episodeId: String, state: Int){
            MediaPlaybackSubject.onNext(Pair(episodeId, state))
        }
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

    private fun startServiceAsForeground() {
        startForeground(NOTIFICATION_ID, buildForegroundNotification())
    }

    /**
     * build notification to let user know apps is playing music
     */
    private fun buildForegroundNotification(): Notification {
        val builder = NotificationCompat.Builder(this)
        builder.setOngoing(true)
                .setContentTitle(currEpisode!!.title)
                .setSmallIcon(R.mipmap.ic_play_white)
        return builder.build()
    }


    private fun buildMediaSourceFromUrl(localUrl: String): MediaSource {
        val mediaSource =
                ExtractorMediaSource(Uri.parse(localUrl), dataSourceFactory, extractorFactory, null , null)
        return mediaSource
    }

    /**
     * play an audio file from a local url
     */
    fun playLocalUrlAudio(episode: Episode) {
        stopPlayback()
        initExoPlayer()
        exoPlayer!!.addListener(this)
        if (exoPlayer != null && episode.localUrl != null){
            exoPlayer!!.prepare(buildMediaSourceFromUrl(episode.localUrl))
            exoPlayer!!.playWhenReady = true

            // before stop this player ...send out a cast for other view
            if (currEpisode != null) publishMediaPlaybackSubject(currEpisode!!.uniqueId, MEDIA_STOPPED)
            currEpisode = episode
            startServiceAsForeground()
        }
    }

    override fun onDestroy() {
        if (exoPlayer != null){
            stopPlayback()
        }
        super.onDestroy()
    }

    fun addTrackToPlaylist() {

    }

    fun registerPlayerListener (listener: ExoPlayer.EventListener) {
        if (exoPlayer != null ){
            exoPlayer!!.addListener(listener)
        }
    }

    fun stopPlayback() {
        if (exoPlayer != null){
            exoPlayer!!.release()
            exoPlayer!!.removeListener(this)
            exoPlayer = null
            currEpisode = null
            stopForeground(true)
        }

    }

    fun pausePlayback() {
        if (exoPlayer != null) {
            exoPlayer!!.playWhenReady = false
        }
    }

    fun resumePlayback() {
        if (exoPlayer != null) {
            exoPlayer!!.playWhenReady = true
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
        if (error != null) error.printStackTrace()
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        if (currEpisode != null){
            when(playbackState) {

                ExoPlayer.STATE_BUFFERING -> {
                    publishMediaPlaybackSubject(currEpisode!!.uniqueId, MEDIA_PLAYING)
                }

                ExoPlayer.STATE_IDLE -> {
                    publishMediaPlaybackSubject(currEpisode!!.uniqueId, MEDIA_STOPPED)
                }
            }
        }

    }

    override fun onPositionDiscontinuity() {

    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?) {

    }

    override fun onLoadingChanged(isLoading: Boolean) {

    }
}
