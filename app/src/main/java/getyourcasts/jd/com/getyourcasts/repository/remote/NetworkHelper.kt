package getyourcasts.jd.com.getyourcasts.repository.remote

import getyourcasts.jd.com.getyourcasts.repository.remote.data.ItuneResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Created by chuondao on 7/22/17.
 */

class NetworkHelper () {
    //private val API_URL = "https://itunes.apple.com/search?media=podcast&term=%s"
    private val BASE_ITUNES_URL = "https://itunes.apple.com"

    private val ituneApi : ItunesApi

    companion object {
        val PODCAST_TERM = "podcast"
        private var instance : NetworkHelper? = null

        /**
         * try to make this class signleton
         */
        fun getHelperInstance(): NetworkHelper{
            if (instance == null ){
                instance = NetworkHelper()
            }
            return instance!!
        }
    }

    init {
        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_ITUNES_URL)
                .addConverterFactory(MoshiConverterFactory.create()).build()

        ituneApi = retrofit.create(ItunesApi::class.java)
    }

    /**
     * generate call for Itunes API service
     */
    fun searchPodcast (term: String): Call<ItuneResponse> {
        return ituneApi.searchPodcast(PODCAST_TERM,term)
    }
}
