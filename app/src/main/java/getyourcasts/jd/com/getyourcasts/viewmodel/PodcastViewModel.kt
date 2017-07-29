package getyourcasts.jd.com.getyourcasts.viewmodel

import android.content.ContentValues
import getyourcasts.jd.com.getyourcasts.repository.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.repository.local.PodcastsTable
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Channel
import getyourcasts.jd.com.getyourcasts.repository.remote.data.FeedItem
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

/**
 * Created by chuondao on 7/22/17.
 */
class PodcastViewModel(val dataRepo :DataSourceRepo ) {

    companion object {
        val NUM_TOP_RESULTS : Long = 20
        private lateinit var INSTANCE : PodcastViewModel

        fun getInstance(dataRepo: DataSourceRepo): PodcastViewModel {
            if (INSTANCE == null){
                INSTANCE = PodcastViewModel(dataRepo)
            }
            return INSTANCE
        }

    }

    fun getPodcastSearchObservable (term : String): Observable<List<Podcast>>{
        return Observable.defer {
            Observable.just(dataRepo.searchPodcast(term))
        }.flatMapIterable {
            it
        }.filter {
            // filter only podcast with trackcount > 0
            it.trackCount > 0
        }.toList().toObservable().subscribeOn(Schedulers.io())
    }

    fun getSubscribeObservable(pod: Podcast)  {
        //Observable.defer {
            // fetch rss feed
            val channelInfo = dataRepo.downloadFeed(pod.feedUrl)
            if (dataRepo.insertPodcastToDb(pod) && channelInfo != null){
                // now update with field that not available from itune
                val cv = ContentValues()
                cv.put(PodcastsTable.DESCRIPTION, channelInfo.channelDescription)
                // update data description
                dataRepo.updatePodcast(cv,pod.collectionId)
                // now insert episode into db

            }
        //}
    }



    fun insertEpisodes (items : List<FeedItem>){

    }

    /**
     * insert Podcast to DB
     */
    fun getInsertPodcastToDbObservable(pod: Podcast): Observable<Boolean>{

        return Observable.defer {
            Observable.just(dataRepo.insertPodcastToDb(pod))
        }.subscribeOn(Schedulers.io())
    }


    fun getChannelFeedObservable(feedUrl:String): Observable<Channel?>{
        return Observable.defer{
            Observable.just(dataRepo.downloadFeed(feedUrl))
        }.subscribeOn(Schedulers.computation())
    }

    fun getIsPodcastInDbObservable(podcastId: String): Observable<Podcast> {
        return Observable.defer(
            fun(): Observable<Podcast> {
                try {
                    val podcast = dataRepo.getPodcast(podcastId)
                    if (podcast != null){
                        return Observable.just(podcast)
                    }
                } catch(e: Exception) {
                    e.printStackTrace()
                }
                return Observable.just(Podcast.getEmptyPodcast())
            }
        ) .subscribeOn(Schedulers.io())
    }


}