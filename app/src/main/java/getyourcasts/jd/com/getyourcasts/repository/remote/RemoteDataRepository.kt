package getyourcasts.jd.com.getyourcasts.repository.remote

import android.util.Log
import getyourcasts.jd.com.getyourcasts.repository.remote.data.FeedItem
import getyourcasts.jd.com.getyourcasts.repository.remote.data.FeedResponse
import getyourcasts.jd.com.getyourcasts.repository.remote.data.MediaInfo
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import getyourcasts.jd.com.getyourcasts.repository.remote.network.NetworkHelper
import retrofit2.http.Url
import java.net.URL

/**
 * Created by chuondao on 7/22/17.
 */


class RemoteDataRepository {

    companion object {
        val TAG = "REMOTE_REPO"
        private var instance : RemoteDataRepository? = null

        /**
         * getter for data instance
         */
        fun getDataInstance():RemoteDataRepository{
            if (instance == null){
                instance = RemoteDataRepository()
            }
            return instance!!
        }
    }

    /**
     * search for requested podcast
     */
    fun searchPodcast (title: String): List<Podcast>{
        val results = ArrayList<Podcast>()
        val searchUrl = NetworkHelper.getHelperInstance().searchPodcast(title)
        val http_req = searchUrl.request()
        Log.d(TAG,"Request : $http_req")
        val response = searchUrl.execute()
        /*verify if response is good*/
        if (response.isSuccessful){
            // parse data here
            if (response.body() != null){

                /*QUICK TEST HERE */


                return response.body()!!.results
            }
            else{
                Log.e(TAG,"Failed tor retreive message body")
            }
        }
        return results
    }

    /**
     * download feeds for podcast
     */
    fun downloadFeed(feedUrl: String): List<FeedItem> {

        //val base_url = URL(feedUrl)
        val req = NetworkHelper.getHelperInstance().downLoadFeed(feedUrl).request()
        Log.d(TAG,"SEND REQUEST $req" )
        try {
            val feedResponse = NetworkHelper.getHelperInstance().downLoadFeed(feedUrl).execute()
            if (feedResponse.isSuccessful){
                if (feedResponse.body() != null){
                    return feedResponse.body()!!.channel!!.items
                }
                else{
                    Log.e(TAG,"Failed to fetch Media Item")
                }
            }
        }
        catch(e: Exception)
        {
            Log.e(TAG,"FAILED REQUEST $req. ${e.message} " )
            e.printStackTrace()
        }

        // return empty list
        return ArrayList<FeedItem>()

    }
//
//    override fun getPodcast(podcastId: String): List<Podcast> {
//
//    }
//
//    override fun getAllEpisodesOfPodcast(podcastId: String): List<Episode> {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun getEpisode(episodeId: String, podcastId: String): List<Episode> {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun updatePodcast(cv: ContentValues, podcastID: String): Long {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }
//
//    override fun updateEpisode(cv: ContentValues, episodeId: String): Long {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//    }

}