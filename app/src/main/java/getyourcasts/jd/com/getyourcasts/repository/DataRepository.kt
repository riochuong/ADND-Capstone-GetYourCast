package getyourcasts.jd.com.getyourcasts.repository

import android.content.ContentValues
import android.database.Cursor

/**
 * Repository Interface to encapsulate functionalities of
 * both remote and local repository
 * Created by chuondao on 7/21/17.
 */

interface DataRepository {

    // query function
    fun getPodcast(podcastId: String) : Cursor

    fun getAllEpisodesOfPodcast(podcastId: String) : Cursor

    fun getEpisode (episodeId: String, podcastId: String): Cursor

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