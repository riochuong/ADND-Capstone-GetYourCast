package getyourcasts.jd.com.getyourcasts.exoplayer

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.app.NotificationCompat
import android.support.v4.media.app.NotificationCompat.MediaStyle
import android.support.v4.media.session.MediaSessionCompat
import android.util.Pair
import getyourcasts.jd.com.getyourcasts.R
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.view.media.MediaPlayerActivity
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel
import getyourcasts.jd.com.getyourcasts.widget.GetYourCastWidgetProvider
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import java.util.*

/**
 * Created by chuondao on 12/5/17.
 */
class MediaNotificationManager (private val mediaPlaybackService: MediaPlayBackService) : BroadcastReceiver() {
    /* properties */
    private var sessionToken: MediaSessionCompat.Token? = null

    private val playPendingIntent: PendingIntent
    private val pausePendingIntent: PendingIntent
    private val nextPendingIntent: PendingIntent
    private val previousPendingIntent: PendingIntent
    private val notificationManager : NotificationManager
    private var disposable: Disposable? = null
    private var started: Boolean = false
    init {

        playPendingIntent = PendingIntent.getBroadcast(mediaPlaybackService, REQUEST_CODE,
                INTEN_ACTION_PLAY.setPackage(mediaPlaybackService.packageName),
                PendingIntent.FLAG_CANCEL_CURRENT
                )
        pausePendingIntent = PendingIntent.getBroadcast(mediaPlaybackService, REQUEST_CODE,
                INTEN_ACTION_PAUSE.setPackage(mediaPlaybackService.packageName),
                PendingIntent.FLAG_CANCEL_CURRENT
        )
        previousPendingIntent = PendingIntent.getBroadcast(mediaPlaybackService, REQUEST_CODE,
                INTEN_ACTION_PREVIOUS.setPackage(mediaPlaybackService.packageName),
                PendingIntent.FLAG_CANCEL_CURRENT
        )
        nextPendingIntent = PendingIntent.getBroadcast(mediaPlaybackService, REQUEST_CODE,
                INTEN_ACTION_NEXT.setPackage(mediaPlaybackService.packageName),
                PendingIntent.FLAG_CANCEL_CURRENT
        )
        notificationManager = mediaPlaybackService.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
          val action = intent?.action
          when(action) {
              ACTION_PAUSE -> {
                  mediaPlaybackService.pausePlayback()
              }
              ACTION_NEXT ->{
                  mediaPlaybackService.playNextSongInPlaylist()
              }
              ACTION_PLAY ->{
                  mediaPlaybackService.resumePlayback()
              }
              ACTION_PREV ->{
                  mediaPlaybackService.playPreviousSongInPlaylist()
              }
          }
    }

    private fun updateSessionToken()  {
        val freshToken: MediaSessionCompat.Token? = mediaPlaybackService.getSessionToken()
        if (freshToken != null) {
            sessionToken = freshToken
        }
    }
    private fun addMediaActions(builder: NotificationCompat.Builder, state: Int) {
        if (state == MediaPlayBackService.MEDIA_PLAYING){
            builder.addAction(R.mipmap.ic_media_pause,
                    mediaPlaybackService.getString(R.string.notification_pause), pausePendingIntent)
            builder.setOngoing(true)
        } else {
            builder.addAction(R.mipmap.ic_media_play,
                    mediaPlaybackService.getString(R.string.notification_play), playPendingIntent)
            builder.setOngoing(false)
        }

        builder.addAction(R.mipmap.ic_media_skip_prev,
                mediaPlaybackService.getString(R.string.notification_prev), previousPendingIntent)
        builder.addAction(R.mipmap.ic_media_next,
                mediaPlaybackService.getString(R.string.notification_next), nextPendingIntent)
    }

    private fun createNotification (ep: Episode, state: Int) : Notification {
        val notificationBuilder = NotificationCompat.Builder(mediaPlaybackService)
        addMediaActions(notificationBuilder, state)
        notificationBuilder
                .setStyle(MediaStyle()
                        .setShowActionsInCompactView(0)  // show only play/pause in compact view
                        .setMediaSession(sessionToken))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.mipmap.ic_media_play)
                .setContentTitle(ep.title)
                .setContentIntent(createContentIntent(mediaPlaybackService))
                //.setContentTitle(description.getTitle())
                //.setContentText(description.getSubtitle())
                //.setLargeIcon(art)
        return notificationBuilder.build()
    }

    private fun createContentIntent(ctx: Context): PendingIntent {
        val launchMediaPlayer = Intent(ctx, MediaPlayerActivity::class.java)
        launchMediaPlayer.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        return  PendingIntent.getActivity(ctx, REQUEST_CODE,
                launchMediaPlayer, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    private fun getIntentFilterForActions(): IntentFilter {
        val filter = IntentFilter()
        filter.addAction(ACTION_NEXT)
        filter.addAction(ACTION_PREV)
        filter.addAction(ACTION_PLAY)
        filter.addAction(ACTION_PAUSE)
        return filter
    }

    fun startNotification(ep: Episode, state: Int) {
        // already started then return
        if (started) return
        /// start notification now
        started = true
        updateSessionToken()
        val notification = createNotification(ep, state)
        mediaPlaybackService.registerReceiver(this, getIntentFilterForActions())
        mediaPlaybackService.startForeground(NOTIFICATION_ID,notification)
        // subscribe to media change state
        if (disposable == null) {
            MediaPlayBackService.subscribeMediaPlaybackSubject(
                    object: Observer<Pair<Episode?, Int>> {
                        override fun onSubscribe(d: Disposable) {
                            disposable = d
                        }

                        override fun onNext(t: Pair<Episode?, Int>) {
                            when (t.second) {
                                MediaPlayBackService.MEDIA_PLAYING -> {
                                    val newNoti = createNotification(t.first!!, MediaPlayBackService.MEDIA_PLAYING)
                                    notificationManager.notify(NOTIFICATION_ID,newNoti)
                                }
                                MediaPlayBackService.MEDIA_PAUSE -> {
                                    val newNoti = createNotification(t.first!!, MediaPlayBackService.MEDIA_PAUSE)
                                    notificationManager.notify(NOTIFICATION_ID,newNoti)
                                }
                            }

                        }

                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                        }

                        override fun onComplete() {
                            // no op for now
                        }

                    }
            )
        }
    }

    fun stopNotification () {

    }

    companion object {
        const val ACTION_PAUSE = "notification_action_pause"
        const val ACTION_PLAY = "notification_action_play"
        const val ACTION_PREV = "notification_action_prev"
        const val ACTION_NEXT = "notification_action_next"
        const val NOTIFICATION_ID = 10
        const val REQUEST_CODE = 101
        val INTEN_ACTION_PLAY = Intent(ACTION_PLAY)
        val INTEN_ACTION_PAUSE = Intent(ACTION_PAUSE)
        val INTEN_ACTION_PREVIOUS = Intent(ACTION_PREV)
        val INTEN_ACTION_NEXT = Intent(ACTION_NEXT)
    }
}