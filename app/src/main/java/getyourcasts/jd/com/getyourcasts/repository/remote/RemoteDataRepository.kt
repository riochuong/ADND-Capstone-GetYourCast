package getyourcasts.jd.com.getyourcasts.repository.remote

import android.util.Log
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast

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

    fun searchPodcast (title: String): List<Podcast>{
        val results = ArrayList<Podcast>()
        val searchUrl = NetworkHelper.getHelperInstance().searchPodcast(title)
        val http_req = searchUrl.request()
        Log.d(TAG,"Request : $http_req")
        val response = searchUrl.execute()
        /*verify if response is good*/
        if (response.isSuccessful){
            // parse data here
            return response.body().results
        }
        return results
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