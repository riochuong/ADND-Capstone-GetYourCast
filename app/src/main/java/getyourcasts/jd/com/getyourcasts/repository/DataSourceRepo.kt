package getyourcasts.jd.com.getyourcasts.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import getyourcasts.jd.com.getyourcasts.repository.local.LocalDataRepository
import getyourcasts.jd.com.getyourcasts.repository.remote.RemoteDataRepository
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.repository.remote.data.FeedItem
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast

/**
 * Created by chuondao on 7/22/17.
 */

class DataSourceRepo (ctx: Context): DataRepository
{
    private val remoteRepo: RemoteDataRepository
    private val localRepo: LocalDataRepository

    init {
        remoteRepo = RemoteDataRepository.getDataInstance()
        localRepo = LocalDataRepository(ctx)
    }


    override fun searchPodcast(title: String): List<Podcast> {
        return remoteRepo.searchPodcast(title)
    }

    override fun downloadFeed(feedUrl: String): List<FeedItem>{
        return remoteRepo.downloadFeed(feedUrl)
    }

    override fun getPodcast(podcastId: String): Podcast {
        return Podcast("","","","","","",0)
    }

    override fun getAllEpisodesOfPodcast(podcastId: String): List<Episode> {
        return ArrayList<Episode>();
    }

    override fun getEpisode(episodeId: String, podcastId: String): List<Episode> {
        return ArrayList<Episode>();
    }

    override fun updatePodcast(cv: ContentValues, podcastID: String): Long {
        return 0
    }

    override fun updateEpisode(cv: ContentValues, episodeId: String): Long {
        return 0
    }


}
//    override fun getPodcast(podcastId: String): Cursor {
//
//    }
//
//    override fun getAllEpisodesOfPodcast(podcastId: String): Cursor {
//
//    }
//
//    override fun getEpisode(episodeId: String, podcastId: String): Cursor {
//
//    }
//
//    override fun updatePodcast(cv: ContentValues, podcastID: String): Long {
//
//    }
//
//    override fun updateEpisode(cv: ContentValues, episodeId: String): Long {
//
//    }
//
//}