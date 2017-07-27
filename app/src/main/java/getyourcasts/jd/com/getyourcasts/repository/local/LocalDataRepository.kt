package getyourcasts.jd.com.getyourcasts.repository.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import getyourcasts.jd.com.getyourcasts.repository.DataRepository
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.repository.remote.data.FeedItem
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import org.jetbrains.anko.db.select


/**
 * Created by chuondao on 7/22/17.
 */

class LocalDataRepository(val ctx: Context): DataRepository {

    override fun downloadFeed(feedUrl: String): List<FeedItem> {
        // NOOP
        return ArrayList<FeedItem>()
    }


    override fun searchPodcast(title: String): List<Podcast> {
        // NOOP
        return ArrayList<Podcast>()
    }

    override fun getPodcast(podcastId: String): Podcast {
        var cursor :Cursor = ctx.database.use {
            // this is a sqlite db instance
            select(PodcastsTable.NAME).whereArgs(PodcastsTable.UNIQUE_ID+" = $podcastId").exec {this}
        }

        if (cursor.count > 0){
            cursor.moveToFirst()

        }
        return Podcast("","","","","","",0)
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
