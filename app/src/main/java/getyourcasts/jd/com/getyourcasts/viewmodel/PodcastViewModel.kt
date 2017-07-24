package getyourcasts.jd.com.getyourcasts.viewmodel

import getyourcasts.jd.com.getyourcasts.repository.DataRepository
import getyourcasts.jd.com.getyourcasts.repository.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.repository.remote.data.FeedItem
import getyourcasts.jd.com.getyourcasts.repository.remote.data.FeedResponse
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

/**
 * Created by chuondao on 7/22/17.
 */
class PodcastViewModel(val dataRepo :DataSourceRepo ) {


    fun getPodcastSearchObservable (term : String): Observable<Podcast>{

        return Observable.defer(
                fun(): Observable<List<Podcast>> {
                    return Observable.just(dataRepo.searchPodcast(term))
                }
        ).subscribeOn(Schedulers.io())
                .flatMap (
                        fun (podcasts:List<Podcast>): Observable<Podcast>{
                            return Observable.fromIterable(podcasts)
                        }
                )

    }

    fun fetchPodcastEpisode(feedUrl : String): Observable<FeedItem>{
        return Observable.defer(
                fun(): Observable<List<FeedItem>> {
                    return Observable.just(dataRepo.downloadFeed(feedUrl))
                }
        ).subscribeOn(Schedulers.io())
                .flatMap (
                        fun (feedItems:List<FeedItem>): Observable<FeedItem>{
                            return Observable.fromIterable(feedItems)
                        }
                )

    }
}