package getyourcasts.jd.com.getyourcasts.exoplayer

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationCompat.Builder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.util.Pair
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import getyourcasts.jd.com.getyourcasts.util.StorageUtil
import getyourcasts.jd.com.getyourcasts.viewmodel.EpisodeState
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastState
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel
import getyourcasts.jd.com.getyourcasts.widget.GetYourCastWidgetProvider
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.util.*


/**
 * Created by chuondao on 8/11/17.
 */

class MediaPlayBackService : Service(), Player.EventListener {


    private var exoPlayer: SimpleExoPlayer? = null
    private var dataSourceFactory: DataSource.Factory? = null
    private var extractorFactory: ExtractorsFactory? = null
    private var mediaSessionConn: MediaSessionConnector? = null

    private val binder = MediaPlayBackServiceBinder()
    private var playListRemoveDisposable: Disposable? = null
    private var episodeStateDisposable: Disposable? = null
    private lateinit var mediaNotificationManager: MediaNotificationManager

    /*Simple fields for playlist management*/
    private var playList: MutableList<Episode> = ArrayList()
    private var currEpisodePos = -1
    private var initialized = false

    /**
     * to help synchronize between playlist activity and current playlist
     *
     * @return
     */
    // make a copy here to avoid mutable outside of this class
    val mediaPlaylist: List<Episode>
        @Synchronized get() {
            return this.playList!!.map { Episode(it) }
        }

    /**
     * return the next song index
     *
     * @return
     */
    private val nextMediaFile: Int
        get() {
            if (playList!!.size == 0) {
                return -1
            }
            return if (currEpisodePos == playList!!.size - 1) 0 else currEpisodePos + 1
        }

    /**
     * return the previous song index
     *
     * @return
     */
    private val prevMediaFile: Int
        get() {
            if (playList!!.size == 0) {
                return -1
            }
            return if (currEpisodePos == 0) playList!!.size - 1 else currEpisodePos - 1
        }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    private fun initExoPlayer() {
        if (exoPlayer !=
                null) {
            exoPlayer!!.release()
        }

        if (exoPlayer == null) {
            exoPlayer = ExoPlayerFactory.newSimpleInstance(this, DefaultTrackSelector())
            mediaSessionConn!!.setPlayer(exoPlayer, null)
            mediaSessionConn!!.mediaSession.isActive = true
        }
    }

    private fun initMediaPlaybackObserver() {
        subscribeMediaPlaybackSubject(object : Observer<Pair<Episode?, Int>> {
            override fun onSubscribe(d: Disposable) {
                playListRemoveDisposable = d
            }

            override fun onNext(info: Pair<Episode?, Int>) {
                val state = info.second
                // avoid restart error when media playlist is invalid state
                if (currEpisodePos < 0) return
                when (state) {
                    MEDIA_PLAYING -> PodcastViewModel.getInstance(DataSourceRepo.getInstance(this@MediaPlayBackService))
                            .getPodcastObservable(playList!![currEpisodePos].podcastId)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    object : Observer<Podcast> {
                                        override fun onSubscribe(d: Disposable) {

                                        }

                                        override fun onNext(podcast: Podcast) {
                                            GetYourCastWidgetProvider.resolveButtonState(
                                                    this@MediaPlayBackService,
                                                    GetYourCastWidgetProvider.WIDGET_PLAY_ACTION_PROVIDER,
                                                    false,
                                                    podcast.imgLocalPath
                                            )
                                        }

                                        override fun onError(e: Throwable) {
                                            e.printStackTrace()
                                        }

                                        override fun onComplete() {

                                        }
                                    }
                            )
                    MEDIA_PAUSE -> GetYourCastWidgetProvider.resolveButtonState(
                            this@MediaPlayBackService,
                            GetYourCastWidgetProvider.WIDGET_PAUSE_ACTION_PROVIDER,
                            false, null)
                    MEDIA_REMOVED_FROM_PLAYLIST -> removeTrackAndSetMediaPlayerNewState(info.first!!)
                    MEDIA_PLAYLIST_EMPTY -> GetYourCastWidgetProvider.resolveButtonState(
                            this@MediaPlayBackService,
                            GetYourCastWidgetProvider.WIDGET_EMPTY_PLAYLIST_ACTION_PROVIDER,
                            false, null)
                }
            }

            override fun onError(e: Throwable) {

            }

            override fun onComplete() {

            }
        })
    }

    private fun removeTrackAndSetMediaPlayerNewState(ep: Episode) {
        // make sure playlist is not empty
        val currentEp = playList!![currEpisodePos]
        if (currEpisodePos >= 0) {
            val index = findIndexFromList(ep)
            if (index >= 0) {
                removeTrackFromPlayList(index)

            }
            // only remove item above will affect the index
            // fix the current pointer of the playlist position
            if (index < currEpisodePos) {
                // just need to update curr episode here
                currEpisodePos = findIndexFromList(currentEp)
            } else if (index == currEpisodePos) {
                if (!playList.isEmpty()) {
                    currEpisodePos = if (currEpisodePos == playList!!.size - 1) 0 else currEpisodePos
                    changeCurentPlayingSongTo(currEpisodePos,false)
                } else {
                    // playlist is empty here
                    stopPlayback()
                }
            }
        }
    }

    @Synchronized private fun findIndexFromList(ep: Episode): Int {
        for (i in playList!!.indices) {
            if (playList!![i].uniqueId == ep.uniqueId) {
                return i
            }
        }
        Log.e(TAG, "Cannot find episode from playlist")
        return -1
    }


    /**
     * build notification to let user know apps is playing music
     */
    private fun buildForegroundNotification(builder: NotificationCompat.Builder): Notification {
        builder .setContentTitle(playList[currEpisodePos].title)
                .setSmallIcon(R.mipmap.ic_media_play)
        return builder.build()
    }


    private fun buildMediaSession(): MediaSessionConnector {
        val mediaSession = MediaSessionCompat(this, TAG)
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat
                .FLAG_HANDLES_TRANSPORT_CONTROLS)
        // dont want media button to start our app when it is off
        mediaSession.setMediaButtonReceiver(null)
        mediaSession.setCallback(MediaSessionCallback())

        // set an intiail playback state with ACTION_PLAY, so media buttons can start the player
        val stateBuilder = PlaybackStateCompat.Builder().setActions(
                PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or PlaybackStateCompat.ACTION_STOP
        )
        mediaSession.setPlaybackState(stateBuilder.build())
        mediaSession.isActive = true
        return MediaSessionConnector(mediaSession)
    }


    private fun buildMediaSourceFromUrl(localUrl: String): MediaSource {
        return ExtractorMediaSource(Uri.parse(localUrl), dataSourceFactory,
                extractorFactory, null, null)
    }

    override fun onCreate() {
        super.onCreate()
        mediaSessionConn = buildMediaSession()
        dataSourceFactory = buildDataSource()
        extractorFactory = buildExtractorFactory()
        mediaNotificationManager = MediaNotificationManager(this)
        playList = ArrayList()
        initMediaPlaybackObserver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (!initialized) {
            // subscribe to EpisodeState
            PodcastViewModel.subscribeEpisodeSubject(object : Observer<EpisodeState> {
                override fun onSubscribe(d: Disposable) {
                    if (episodeStateDisposable != null) episodeStateDisposable!!.dispose()
                    episodeStateDisposable = d
                }

                override fun onNext(episodeState: EpisodeState) {
                    when (episodeState.state) {
                        EpisodeState.EPISODE_DELETED -> for (ep in playList!!) {
                            if (ep.uniqueId == episodeState.uniqueId) {
                                MediaPlayBackService.publishMediaPlaybackSubject(ep,
                                        MediaPlayBackService.MEDIA_REMOVED_FROM_PLAYLIST)
                                saveMediaPlaylist()
                                break
                            }
                        }
                    }
                }

                override fun onError(e: Throwable) {

                }

                override fun onComplete() {

                }
            })
            // subscribe to Podcast to remove episode of unsubscribed one
            PodcastViewModel.subscribePodcastSubject(
                    object : Observer<PodcastState> {
                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onNext(podcastState: PodcastState) {
                            when (podcastState.state) {
                                PodcastState.UNSUBSCRIBED -> {
                                    // check if current episode is belong to this unsubscribed one
                                    val removedItems = playList.filter { it.podcastId == podcastState.uniqueId }
                                    // remove each item from list
                                    for (ep in removedItems) {
                                        publishMediaPlaybackSubject(ep, MEDIA_REMOVED_FROM_PLAYLIST)
                                    }
                                }
                            }
                        }

                        override fun onError(e: Throwable) {

                        }

                        override fun onComplete() {

                        }
                    }
            )
            // load playlist from file
            Observable.just(StorageUtil.loadMediaPlayList(this))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            object : Observer<List<Episode>> {
                                override fun onSubscribe(d: Disposable) {}

                                override fun onNext(episodeList: List<Episode>) {
                                    initialized = true
                                    playList = episodeList.toMutableList()
                                    if (playList!!.size > 0) {
                                        // simply set the current index to 0
                                        currEpisodePos = 0
                                        prepareMediaFile(playList!![0])
                                    }
                                }

                                override fun onError(e: Throwable) {
                                    e.printStackTrace()
                                }

                                override fun onComplete() {}
                            }
                    )
        }
        // handle Widget intent
        if (intent != null && initialized) {
            val action = intent.getIntExtra(GetYourCastWidgetProvider.WIDGET_MEDIA_ACTION_KEY, 0xFF)
            when (action) {
                WIDGET_ACTION_PAUSE -> pausePlayback()
                WIDGET_ACTION_PLAY -> if (exoPlayer != null) {
                    resumePlayback()
                } else if (currEpisodePos < 0 && !playList!!.isEmpty()) {
                    changeCurentPlayingSongTo(0,true)
                } else {
                    MediaPlayBackService.publishMediaPlaybackSubject(null, MediaPlayBackService
                            .MEDIA_PLAYLIST_EMPTY)
                    GetYourCastWidgetProvider.resolveButtonState(this,
                            GetYourCastWidgetProvider.WIDGET_EMPTY_PLAYLIST_ACTION_PROVIDER,
                            false, null)
                }
                WIDGET_ACTION_NEXT -> playNextSongInPlaylist()
                WIDGET_ACTION_PREV -> playPreviousSongInPlaylist()
            }

        }
        return Service.START_STICKY
    }

    override fun onUnbind(intent: Intent): Boolean {
        saveMediaPlaylist()
        return super.onUnbind(intent)
    }

    private fun saveMediaPlaylist() {
        Observable.just(
                StorageUtil.saveMediaPlayList(this, playList!!)
        ).subscribeOn(Schedulers.io())
                .subscribe(
                        object : Observer<Boolean> {
                            override fun onSubscribe(d: Disposable) {

                            }

                            override fun onNext(result: Boolean) {
                                if (result!!) {
                                    Log.d(TAG, "Successfully save media playlist as GSON")
                                } else {
                                    Log.e(TAG, "Failed to save media playlist")
                                }
                            }

                            override fun onError(e: Throwable) {

                            }

                            override fun onComplete() {

                            }
                        }
                )
    }


    override fun onDestroy() {

        if (exoPlayer != null) {
            stopPlayback()
        }
        // clean up media session
        if (mediaSessionConn != null) {
            mediaSessionConn!!.mediaSession.isActive= false
            mediaSessionConn!!.mediaSession.release()
        }

        if (playListRemoveDisposable != null) {
            playListRemoveDisposable!!.dispose()
            playListRemoveDisposable = null
        }

        if (episodeStateDisposable != null) {
            episodeStateDisposable!!.dispose()
            episodeStateDisposable = null
        }
        super.onDestroy()
    }

    private fun buildDataSource(): DataSource.Factory {
        return DefaultDataSourceFactory(this, this.resources.getString(R.string.app_name))
    }

    private fun buildExtractorFactory(): ExtractorsFactory {
        return DefaultExtractorsFactory()
    }


    /*====================== SERVICE APP FUNTIONS EXPOSED TO CLIENTS ============ */

    @Synchronized private fun prepareMediaFile(episode: Episode?) {
        stopPlayback()
        initExoPlayer()
        exoPlayer!!.addListener(this)
        if (exoPlayer != null && episode!!.localUrl != null) {
            currEpisodePos = 0
            addTrackToPlaylist(episode, 0)
            if (currEpisodePos >= 0) publishMediaPlaybackSubject(playList!![currEpisodePos], MEDIA_STOPPED)
            exoPlayer!!.prepare(buildMediaSourceFromUrl(episode.localUrl))
        }
    }

    /**
     * play an audio file from a local url
     */
    @Synchronized
    fun playMediaFile(episode: Episode?) {
        prepareMediaFile(episode)
        if (episode != null) {
            exoPlayer!!.playWhenReady = true
            mediaNotificationManager.startNotification(episode, MEDIA_PLAYING)
        }
    }

    /**
     * add this function to support select song on the playlist side
     *
     * @param index
     */
    @Synchronized
    private fun changeCurentPlayingSongTo(index: Int, setPlayWhenrReady: Boolean) {
        stopPlayback()
        initExoPlayer()
        exoPlayer!!.addListener(this)
        if (index >= 0 && index < playList!!.size) {
            currEpisodePos = index
            exoPlayer!!.prepare(buildMediaSourceFromUrl(playList!![index].localUrl))
            exoPlayer!!.playWhenReady = setPlayWhenrReady
            publishMediaPlaybackSubject(playList!![currEpisodePos], MEDIA_TRACK_CHANGED)
        }
    }

    @Synchronized
    fun playEpisodeInPlayList(ep: Episode) {
        val index = findEpisodeIndexInPlaylist(ep)
        if (index >= 0) {
            changeCurentPlayingSongTo(index, true)
        }
    }

    private fun findEpisodeIndexInPlaylist(episode: Episode): Int {
        playList.forEachIndexed {
            index, it -> run {
                if (episode.uniqueId.equals(it.uniqueId)) {
                    return index
                }
            }
        }
        return -1
    }


    fun setPlayerView(view: SimpleExoPlayerView) {
        view.player = exoPlayer
    }


    private fun findDuplicateIndex(ep: Episode): Int {
        for (i in playList!!.indices) {
            if (playList!![i].uniqueId == ep.uniqueId) {
                return i
            }
        }
        return -1
    }

    @Synchronized private fun addTrackToPlaylist(episode: Episode, index: Int) {
        if (episode.localUrl != null) {
            val dupIndex = findDuplicateIndex(episode)
            if (dupIndex >= 0) {
                playList!!.removeAt(dupIndex)
            }
            playList!!.add(index, episode)
            saveMediaPlaylist()
            publishMediaPlaybackSubject(playList!![currEpisodePos], MEDIA_ADDED_TO_PLAYLIST)
        }
    }

    /**
     * add track to the current end of the playlist
     *
     * @param episode
     */
    @Synchronized
    fun addTrackToEndPlaylist(episode: Episode) {
        if (episode.localUrl != null) {
            playList!!.add(episode)
            saveMediaPlaylist()
            // initialize current episode if need to
            if (currEpisodePos < 0) {
                currEpisodePos = 0
                prepareMediaFile(playList!![0])
            }
            publishMediaPlaybackSubject(playList!![currEpisodePos], MEDIA_ADDED_TO_PLAYLIST)
        }
    }


    /**
     * remove item from playlist
     *
     * @param index
     */
    @Synchronized private fun removeTrackFromPlayList(index: Int) {
        playList!!.removeAt(index)
        saveMediaPlaylist()
        if (playList!!.isEmpty()) {
            MediaPlayBackService.publishMediaPlaybackSubject(null,
                    MediaPlayBackService.MEDIA_PLAYLIST_EMPTY)
        }
    }


    @Synchronized
    fun stopPlayback() {
        if (exoPlayer != null) {
            exoPlayer!!.release()
            exoPlayer!!.removeListener(this)
            exoPlayer = null
            currEpisodePos = -1
            stopForeground(true)
            mediaSessionConn!!.mediaSession.isActive = false
        }

    }

    fun getSessionToken() : MediaSessionCompat.Token? {
        return mediaSessionConn?.mediaSession?.sessionToken
    }

    @Synchronized
    fun playNextSongInPlaylist() {
        changeCurentPlayingSongTo(nextMediaFile,true)
    }

    @Synchronized
    fun playPreviousSongInPlaylist() {
        changeCurentPlayingSongTo(prevMediaFile, true)
    }

    @Synchronized
    fun pausePlayback() {
        if (exoPlayer != null) {
            exoPlayer!!.playWhenReady = false
            mediaNotificationManager.startNotification(playList[currEpisodePos], MEDIA_PAUSE)
        }
    }

    @Synchronized
    fun resumePlayback() {
        if (exoPlayer != null) {
            exoPlayer!!.playWhenReady = true
            mediaNotificationManager.startNotification(playList[currEpisodePos], MEDIA_PLAYING)
        }
    }

    fun isEpisodeInPlayList(episodeKey: String): Boolean {
        for (ep in playList!!) {
            if (ep.uniqueId == episodeKey) {
                return true
            }
        }
        return false
    }


    /* ======================================= LISTENER ================================================*/

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?) {
        Log.d(TAG,"Timeline Changed")
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {
        Log.d(TAG, "Track Changed")
    }

    override fun onLoadingChanged(isLoading: Boolean) {

    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        if (currEpisodePos >= 0) {
            when (playbackState) {

                Player.STATE_READY -> if (exoPlayer!!.playWhenReady) {
                    publishMediaPlaybackSubject(playList[currEpisodePos], MEDIA_PLAYING)
                } else {
                    publishMediaPlaybackSubject(playList[currEpisodePos], MEDIA_PAUSE)
                }

                Player.STATE_BUFFERING -> {
                }
                Player.STATE_ENDED ->
                    // now pick and play next song !!!
                    changeCurentPlayingSongTo(nextMediaFile, true)
                Player.STATE_IDLE -> publishMediaPlaybackSubject(playList[currEpisodePos], MEDIA_STOPPED)
            }
        }
    }

    override fun onRepeatModeChanged(repeatMode: Int) {

    }

    override fun onPlayerError(error: ExoPlaybackException) {

    }

    override fun onPositionDiscontinuity() {

    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {

    }


    inner class MediaPlayBackServiceBinder : Binder() {
        val service: MediaPlayBackService
            get() = this@MediaPlayBackService
    }

    /* ====== EXOPLAYER LISTENERS ===========*/


    /* MEDIA SESSION CALL BACK*/
    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() {
            exoPlayer!!.playWhenReady = true
        }

        override fun onPause() {
            exoPlayer!!.playWhenReady = false
        }

        override fun onSkipToPrevious() {
            exoPlayer!!.seekTo(0)
        }

        override fun onStop() {
            exoPlayer!!.stop()
        }

    }

    companion object {


        private val TAG = MediaPlayBackService::class.java.simpleName
        private val MEDIA_CHANNEL = "mChannel"
        private val NOTIFICATION_ID = 1990

        // state of the playback
        const val MEDIA_PLAYING = 0
        const val MEDIA_PAUSE = 1
        const val MEDIA_STOPPED = 2
        const val MEDIA_TRACK_CHANGED = 4
        const val MEDIA_UNKNOWN_STATE = 0xFF

        // widget ACTION
        const val WIDGET_ACTION_PLAY = 389
        const val WIDGET_ACTION_PAUSE = 390
        const val WIDGET_ACTION_NEXT = 391
        const val WIDGET_ACTION_PREV = 392

        // state of the playlist
        const val MEDIA_ADDED_TO_PLAYLIST = 3
        //    public static final int MEDIA_ADDED_TO_END_PLAYLIST = 4;
        const val MEDIA_REMOVED_FROM_PLAYLIST = 5
        const val MEDIA_PLAYLIST_EMPTY = 6

        /* MEDIA STATE OBJECT */
        private val MediaPlaybackSubject = BehaviorSubject.create<Pair<Episode?, Int>>()

        fun subscribeMediaPlaybackSubject(obsvr: Observer<Pair<Episode?, Int>>) {
            MediaPlaybackSubject.observeOn(AndroidSchedulers.mainThread()).subscribe(obsvr)
        }

        fun publishMediaPlaybackSubject(episode: Episode?, state: Int) {
            MediaPlaybackSubject.onNext(Pair(episode, state))
        }

    }
}