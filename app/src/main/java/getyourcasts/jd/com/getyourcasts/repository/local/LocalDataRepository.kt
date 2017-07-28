package getyourcasts.jd.com.getyourcasts.repository.local


import android.content.ContentValues
import android.content.Context
import android.database.Cursor

import android.graphics.Bitmap
import android.util.Log
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import getyourcasts.jd.com.getyourcasts.repository.DataRepository
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.repository.remote.data.FeedItem
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import getyourcasts.jd.com.getyourcasts.util.StorageUtil
import getyourcasts.jd.com.getyourcasts.util.TimeUtil
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.Android
import org.jetbrains.anko.db.insert
import org.jetbrains.anko.db.select
import java.io.File
import java.io.FileOutputStream


/**
 * Created by chuondao on 7/22/17.
 */

class LocalDataRepository(val ctx: Context): DataRepository {


    companion object {
        val TAG = "LocalDataRepo"
    }

    override fun insertPodcastToDb(pod: Podcast): Boolean {
            // insert the db
            val res = ctx.database.use {
                insert(
                        PodcastsTable.NAME,
                        PodcastsTable.UNIQUE_ID to pod.collectionId,
                        PodcastsTable.PODCAST_NAME to pod.collectionName,
                        PodcastsTable.FEED_URL to pod.feedUrl,
                        PodcastsTable.RELEASE_DATE to pod.releaseDate,
                        PodcastsTable.IMG_ONLINE_PATH to pod.artworkUrl100,
                        PodcastsTable.ARTIST_NAME to pod.artistName,
                        PodcastsTable.TRACK_COUNT to pod.trackCount,
                        PodcastsTable.LAST_UPDATE to TimeUtil.getCurrentTimeInMs(),
                        PodcastsTable.IMG_LOCAL_PATH to pod.imgLocalPath
                )
            }






        return (res > 0)
    }



    override fun downloadFeed(feedUrl: String): List<FeedItem> {
        // NOOP
        return ArrayList<FeedItem>()
    }


    override fun searchPodcast(title: String): List<Podcast> {
        // NOOP
        return ArrayList<Podcast>()
    }

    override fun getPodcast(podcastId: String): Podcast? {
        var podcast :Podcast? = ctx.database.use {
            // this is a sqlite db instance
            select(PodcastsTable.NAME).whereArgs(PodcastsTable.UNIQUE_ID+" = $podcastId").exec {
                if (this.count > 0){
                    this.moveToFirst()
                    Podcast.fromCursor(this)
                }
                null
            }
        }

        return podcast
    }

    override fun getAllEpisodesOfPodcast(podcastId: String): List<Episode> {
        val cursor =  ctx.database.use {
            select(EpisodeTable.NAME).whereArgs(EpisodeTable.PODCAST_ID+" = $podcastId").exec {this}
        }
        return convertToEpisodeList(cursor)
    }

    override fun getEpisode(episodeName: String, podcastID: String): List<Episode> {
        val cursor = ctx.database.use {
            select(EpisodeTable.NAME).whereArgs("("+EpisodeTable.PODCAST_ID+" = $podcastID ) and ("
                +EpisodeTable.EPISODE_NAME+" = $episodeName").exec {this}
        }
        return convertToEpisodeList(cursor)
    }

    override fun updatePodcast(cv: ContentValues, podcastId: String): Long {
        return ctx.database.use {
            insert(PodcastsTable.NAME, null, cv)
        }
    }

    override fun updateEpisode(cv: ContentValues, episodeId: String): Long {
       return ctx.database.use {
           insert(EpisodeTable.NAME,null,cv)
       }
    }


    fun convertToEpisodeList (cursor:Cursor): List<Episode>{
        return ArrayList<Episode>()
    }

    fun convertToPodcastList (cursor:Cursor): List<Podcast>{
        return ArrayList<Podcast>()
    }


}
