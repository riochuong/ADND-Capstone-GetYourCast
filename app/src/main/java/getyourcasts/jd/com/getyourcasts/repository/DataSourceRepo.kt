package getyourcasts.jd.com.getyourcasts.repository

import android.content.ContentValues
import android.content.Context
import getyourcasts.jd.com.getyourcasts.repository.local.LocalDataRepository
import getyourcasts.jd.com.getyourcasts.repository.remote.RemoteDataRepository
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.repository.remote.data.FeedItem
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast

/**
 * Created by chuondao on 7/22/17.
 */

class DataSourceRepo(ctx: Context) : DataRepository
{
    override fun insertPodcastToDb(pod: Podcast): Boolean {
        return localRepo.insertPodcastToDb(pod)
    }

    private lateinit var localRepo: LocalDataRepository



    init {
        localRepo = LocalDataRepository(ctx)

    }

    companion object {
        var INST: DataSourceRepo? = null

        fun getInstance(ctx: Context): DataSourceRepo {
             if (INST == null){
                 INST = DataSourceRepo(ctx)
             }
             return INST as DataSourceRepo
        }
    }


    override fun searchPodcast(title: String): List<Podcast> {
        return RemoteDataRepository.getDataInstance().searchPodcast(title)
    }

    override fun downloadFeed(feedUrl:String): List<FeedItem>{
        return RemoteDataRepository.getDataInstance().fetchEpisodesFromFeedUrl(feedUrl)
    }

    override fun getPodcast(podcastId: String): Podcast? {
        return localRepo.getPodcast(podcastId)

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