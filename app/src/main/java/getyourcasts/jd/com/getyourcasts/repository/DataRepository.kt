package getyourcasts.jd.com.getyourcasts.repository

/**
 * Repository Interface to encapsulate functionalities of
 * both remote and local repository
 * Created by chuondao on 7/21/17.
 */

interface DataRepository {

    fun getPodcast(podcastId: String)

    fun getAllEpisodesOfPodcast(podcastId: String)

    fun getEpisode (episodeId: String)
}