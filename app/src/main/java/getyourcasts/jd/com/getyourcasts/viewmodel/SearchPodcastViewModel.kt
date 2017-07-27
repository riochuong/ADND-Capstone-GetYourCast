package getyourcasts.jd.com.getyourcasts.viewmodel

import getyourcasts.jd.com.getyourcasts.repository.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.repository.remote.data.FeedItem
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

/**
 * Created by chuondao on 7/22/17.
 */
class SearchPodcastViewModel(val dataRepo :DataSourceRepo ) {


    fun getPodcastSearchObservable (term : String): Observable<List<Podcast>>{

        return Observable.defer(
                fun(): Observable<List<Podcast>> {
                    return Observable.just(dataRepo.searchPodcast(term))
                }
        ).subscribeOn(Schedulers.io())
//                .flatMap (
//                        fun (podcasts:List<Podcast>): Observable<Podcast>{
//                            return Observable.fromIterable(podcasts)
//                        }
//                )

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


}