package getyourcasts.jd.com.getyourcasts.viewmodel

import android.content.ContentValues
import getyourcasts.jd.com.getyourcasts.repository.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.repository.local.PodcastsTable
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Channel
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.repository.remote.data.FeedItem
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Created by chuondao on 7/22/17.
 */
class PodcastViewModel(val dataRepo :DataSourceRepo ) {

    companion object {
        val NUM_TOP_RESULTS : Long = 20
        private var INSTANCE : PodcastViewModel? = null


        fun getInstance(repo: DataSourceRepo): PodcastViewModel {
            if (INSTANCE == null){
                INSTANCE = PodcastViewModel(repo)
            }
            return INSTANCE as PodcastViewModel
        }

        /**
         * this subject will help synchronize between the search list adapter
         * and the detail podcast activity
         */
        private val itemSync : PublishSubject<Pair<Int,String>> = PublishSubject.create()

        /*subscribe to this subject to get
       * notice about change of the state of
       * the podcast either subscribed or not
       * */
        fun subsribeItemSync(observer: Observer<Pair<Int,String>>){
            itemSync.subscribe(observer)
        }

        /*get the source for synchronize */
        fun getItemSyncSubject(): PublishSubject<Pair<Int,String>> {
            return itemSync
        }

        /**
         * this subject will help to synchronize between episode list episode and
         * episode detail info for the downloading status of the episode
         * this will emit progress and episode id
         */
        private val episodeDownloadItemSync : PublishSubject<Pair<String,Long>> = PublishSubject.create()

        fun subscribeEpisodeDownload( observer: Observer<Pair<String, Long>>) {
            episodeDownloadItemSync.subscribe(observer)
        }

        fun getEpisodeDownloadSubject() : PublishSubject<Pair<String,Long>> {
            return episodeDownloadItemSync
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

    fun getSubscribeObservable(pod: Podcast) : Observable<Boolean>  {
       return Observable.defer {
            Observable.just(subscribePodcast(pod))
        }.subscribeOn(Schedulers.io())
    }

    fun getSubscribeObservable(pod: Podcast, channel: Channel) : Observable<Boolean>  {
        return Observable.defer {
            Observable.just(subscribePodcast(pod,channel))
        }.subscribeOn(Schedulers.io())
    }

    private fun subscribePodcast(pod: Podcast, channelInfo: Channel): Boolean{
        if (dataRepo.insertPodcastToDb(pod) && channelInfo != null) {
            // now update with field that not available from itune
            val cv = ContentValues()
            cv.put(PodcastsTable.DESCRIPTION, channelInfo.channelDescription)
            // update data description
            dataRepo.updatePodcast(cv, pod.collectionId)
            // now insert episode into db
            return dataRepo.insertEpisodes(channelInfo.toListEpisodes(pod.collectionId))
        }
        return false
    }

    private fun subscribePodcast(pod: Podcast): Boolean {
        // fetch rss feed
        val channelInfo = dataRepo.downloadFeed(pod.feedUrl)
        if (channelInfo != null){
            return subscribePodcast(pod, channelInfo)
        }
        return false
    }

    fun getAllEpisodesOfPodcastObservable(podcastId: String): Observable<List<Episode>>{
        return Observable.defer {
            Observable.just(dataRepo.getAllEpisodesOfPodcast(podcastId))
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

    /**
     * get update Pod
     */
    fun getUpdateEpisodeObservable (episode: Episode, cv: ContentValues) : Observable<Boolean> {
        return Observable.defer {
             Observable.just(dataRepo.updateEpisode(cv,episode))
        }.subscribeOn(Schedulers.io())
    }

    fun getEpisodeObsevatble(episode: Episode): Observable<Episode> {
        return Observable.defer {
            Observable.just(dataRepo.getEpisode(episode.uniqueId, episode.podcastId))
        }.subscribeOn(Schedulers.io())
    }


}