package getyourcasts.jd.com.getyourcasts.repository.remote

import getyourcasts.jd.com.getyourcasts.repository.remote.data.ItuneResponse
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by chuondao on 7/22/17.
 */

interface ItunesApi{

    @GET("/search")
    fun searchPodcast(
            @Query("media") media: String,
            @Query("term")podCastName: String) : Call<ItuneResponse>



}