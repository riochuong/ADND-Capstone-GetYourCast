package getyourcasts.jd.com.getyourcasts.repository.remote.network

import getyourcasts.jd.com.getyourcasts.repository.remote.data.FeedResponse
import getyourcasts.jd.com.getyourcasts.repository.remote.data.ItuneResponse
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Created by chuondao on 7/22/17.
 */

interface ItunesApi{

    @GET("/search")
    fun searchPodcast(
            @Query("media") media: String,
            @Query("term")podCastName: String) : Call<ItuneResponse>



}

interface RssApi {
    @GET
    fun fetchPodcastInfo(
        @Url feedUrl: String
    ): Call<FeedResponse>
}