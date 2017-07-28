package getyourcasts.jd.com.getyourcasts.viewmodel

import getyourcasts.jd.com.getyourcasts.repository.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.repository.remote.data.FeedItem
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

/**
 * Created by chuondao on 7/22/17.
 */
class PodcastViewModel(val dataRepo :DataSourceRepo ) {

    companion object {
        val NUM_TOP_RESULTS : Long = 20
    }

    fun getPodcastSearchObservable (term : String): Observable<List<Podcast>>{
        return Observable.defer {
            Observable.just(dataRepo.searchPodcast(term))
        }.flatMapIterable {
            it
        }.filter {
            // filter only podcast with trackcount > 0
            it.trackCount > 0
        }.toList().toObservable().subscribeOn(Schedulers.io()).take(NUM_TOP_RESULTS)
    }

    /**
     * insert Podcast to DB
     */
    fun getInsertPodcastToDbObservable(pod: Podcast): Observable<Boolean>{

        return Observable.defer {
            Observable.just(dataRepo.insertPodcastToDb(pod))
        }.subscribeOn(Schedulers.io())
    }


    fun fetchPodcastEpisodeObservable(feedUrl:String): Observable<FeedItem>{
        return Observable.defer(
                fun(): Observable<List<FeedItem>> {
                    return Observable.just(dataRepo.downloadFeed(feedUrl))
                }
        ).subscribeOn(Schedulers.computation())
                .flatMap (
                        // remap each list item so we will get individual tracks
                        fun (feedItems:List<FeedItem>): Observable<FeedItem>{
                            return Observable.fromIterable(feedItems)
                        }
                )

    }

    fun getIsPodcastInDbObservable(podcastId: String): Observable<Boolean> {
        return Observable.defer(
            fun(): Observable<Boolean> {
                val podcast = dataRepo.getPodcast(podcastId)
                if (podcast != null){
                    return Observable.just(true)
                }
                return Observable.just(false)
            }
        ) .subscribeOn(Schedulers.computation())
    }


}