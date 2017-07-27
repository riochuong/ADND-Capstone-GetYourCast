package getyourcasts.jd.com.getyourcasts.repository

import android.content.ContentValues
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.repository.remote.data.FeedItem
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast

/**
 * Repository Interface to encapsulate functionalities of
 * both remote and local repository
 * Created by chuondao on 7/21/17.
 */

interface DataRepository {

    // query function
    fun searchPodcast(title: String): List<Podcast>

    fun getPodcast(podcastId: String) : Podcast?

    fun downloadFeed(feedUrl: String): List<FeedItem>

    fun getAllEpisodesOfPodcast(podcastId: String) : List<Episode>

    fun getEpisode (episodeId: String, podcastId: String): List<Episode>

    // updates function
    /**
     * return -1 if update is failed
     */
    fun updatePodcast(cv: ContentValues, podcastID: String) : Long

    /**
     * return -1 if update is failed
     */
    fun updateEpisode(cv: ContentValues, episodeId: String) : Long
}
