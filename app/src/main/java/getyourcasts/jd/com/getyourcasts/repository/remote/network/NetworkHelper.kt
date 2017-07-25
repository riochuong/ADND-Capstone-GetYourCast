package getyourcasts.jd.com.getyourcasts.repository.remote.network


import getyourcasts.jd.com.getyourcasts.repository.remote.data.FeedResponse
import getyourcasts.jd.com.getyourcasts.repository.remote.data.ItuneResponse
import okhttp3.OkHttpClient
import org.simpleframework.xml.convert.AnnotationStrategy
import org.simpleframework.xml.core.Persister
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import java.net.URL


/**
 * Created by chuondao on 7/22/17.
 */

class NetworkHelper () {
    //private val API_URL = "https://itunes.apple.com/search?media=podcast&term=%s"
    private val BASE_ITUNES_URL = "https://itunes.apple.com"

    private val ituneApi : ItunesApi

    private val okHttpClient: OkHttpClient

    companion object {
        val PODCAST_TERM = "podcast"
        private var instance : NetworkHelper? = null

        /**
         * try to make this class signleton
         */
        fun getHelperInstance(): NetworkHelper {
            if (instance == null ){
                instance = NetworkHelper()
            }
            return instance!!
        }
    }

    init {
        val itunesRetrofit = Retrofit.Builder()
                .baseUrl(BASE_ITUNES_URL)
                .addConverterFactory(MoshiConverterFactory.create()).build()

        ituneApi = itunesRetrofit.create(ItunesApi::class.java)

        //val interceptor = HttpLoggingInterceptor()
        //interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        okHttpClient = OkHttpClient.Builder().build()
    }

    /**
     * generate call for Itunes API service
     */
    fun searchPodcast (term: String): Call<ItuneResponse> {
        return ituneApi.searchPodcast(PODCAST_TERM,term)
    }


    fun fetchRss(feedUrl : String): Call<FeedResponse>?{

        val baseUrl = getBaseUrl(feedUrl)

        if (baseUrl == null) {
            return null
        }

        val rssRetrofit = Retrofit.Builder()
                .baseUrl(getBaseUrl(feedUrl))
                .client(okHttpClient)
                .addConverterFactory(SimpleXmlConverterFactory.createNonStrict(Persister(AnnotationStrategy())))
                .build()


        val rssAdapter = rssRetrofit.create(RssApi::class.java)
        val fetch_info = rssAdapter.fetchPodcastInfo(feedUrl)
        return fetch_info
    }

    fun getBaseUrl(fullUrl: String): String?{
        try {
            val url = URL(fullUrl.trim())
            val baseUrl = url.getProtocol() + "://" + url.getHost()
            return baseUrl
        }
        catch(e: Exception) {
            e.printStackTrace()
        }
        return null;
    }

}
