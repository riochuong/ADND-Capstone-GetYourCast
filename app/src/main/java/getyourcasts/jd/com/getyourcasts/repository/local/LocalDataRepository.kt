package getyourcasts.jd.com.getyourcasts.repository.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import getyourcasts.jd.com.getyourcasts.repository.DataRepository
import org.jetbrains.anko.db.select
import org.jetbrains.anko.db.update


/**
 * Created by chuondao on 7/22/17.
 */

class LocalDataRepository(val ctx: Context): DataRepository {

    override fun getPodcast(podcastId: String): Cursor {
        var cursor :Cursor = ctx.database.use {
            // this is a sqlite db instance
            select(PodcastsTable.NAME).whereArgs(PodcastsTable.UNIQUE_ID+" = $podcastId").exec {this}
        }
        return cursor
    }

    override fun getAllEpisodesOfPodcast(podcastId: String): Cursor {
        return ctx.database.use {
            select(EpisodeTable.NAME).whereArgs(EpisodeTable.POD_UNIQUE_ID+" = $podcastId").exec {this}
        }
    }

    override fun getEpisode(episodeName: String, podcastID: String): Cursor {
        return ctx.database.use {
            select(EpisodeTable.NAME).whereArgs("("+EpisodeTable.POD_UNIQUE_ID+" = $podcastID ) and ("
                +EpisodeTable.EPISODE_NAME+" = $episodeName").exec {this}
        }
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


}
