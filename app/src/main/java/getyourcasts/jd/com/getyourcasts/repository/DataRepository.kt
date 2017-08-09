package getyourcasts.jd.com.getyourcasts.repository

import android.content.ContentValues
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Channel
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.repository.remote.data.FeedItem
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast

/**
 * Repository Interface to encapsulate functionalities of
 * both remote and local repository
 * Created by chuondao on 7/21/17.
 */

//interface DataRepository {
//
//    // query function
//    fun searchPodcast(title: String): List<Podcast>
//
//    fun getPodcast(podcastId: String) : Podcast?
//
//    fun getAllPodcast() : List<Podcast>
//
//    fun downloadFeed(feedUrl: String): Channel?
//
//    fun getAllEpisodesOfPodcast(podcastId: String) : List<Episode>
//
//    fun getEpisode (episodeId: String, podcastId: String): Episode
//
//    // updates function
//    /**
//     * return -1 if update is failed
//     */
//    fun updatePodcast(cv: ContentValues, podcastID: String) : Boolean
//
//    /**
//     * return -1 if update is failed
//     */
//    fun updateEpisode(cv: ContentValues, episode: Episode) : Boolean
//
//    fun insertPodcast(pod: Podcast): Boolean
//
//    fun insertEpisode(episode: Episode): Boolean
//
//    fun insertEpisodes(episodes: List<Episode>) : Boolean
//}
