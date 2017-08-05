package getyourcasts.jd.com.getyourcasts.viewmodel

import android.content.ContentValues
import getyourcasts.jd.com.getyourcasts.repository.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.repository.local.EpisodeTable
import getyourcasts.jd.com.getyourcasts.repository.local.PodcastsTable
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Channel
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

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
        private val podcastSyncSubject: BehaviorSubject<PodcastState> = BehaviorSubject.create()

        /*subscribe to this subject to get
       * notice about change of the state of
       * the podcast either subscribed or not
       * */
        fun subscribePodcastSubject(observer: Observer<PodcastState>){
            podcastSyncSubject.observeOn(AndroidSchedulers.mainThread()).subscribe(observer)
        }

        /*get the source for synchronize */
        fun updatePodcastSubject(podState: PodcastState) {
            podcastSyncSubject.onNext(podState)
        }

        /**
         * this subject will help to synchronize between episode list episode and
         * episode detail info for the downloading status of the episode
         * this will emit progress and episode id
         */
        private val episodeSyncSubject: BehaviorSubject<EpisodeState> = BehaviorSubject.create()

        fun subscribeEpisodeSubject(observer: Observer<EpisodeState>) {
            episodeSyncSubject.observeOn(AndroidSchedulers.mainThread()).subscribe(observer)
        }

        fun updateEpisodeSubject(epState: EpisodeState){
            episodeSyncSubject.onNext(epState)
        }






    }
    /* class to deliver sync state between different item related to epidsode */

    data class EpisodeState(var uniqueId: String,
                            var state: Int,
                            val transId: Long?) {

        companion object {
            const val FETCHED = 0
            const val DOWNLOADING = 2
            const val DOWNLOADED = 1
        }
    }

    /* class to deliver sync state between different item related to epidsode */
    data class PodcastState(var uniqueId: String,
                            var state: Int
                            ) {

        companion object {
            const val NOT_SUBSCRIBED = 0
            const val SUBSCRIBED = 1
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

    fun getUnsubscribeObservable() : Observable<Boolean> {
        return Observable.just(true)
    }

    private fun subscribePodcast(pod: Podcast, channelInfo: Channel): Boolean{
        var res = false
        if (dataRepo.insertPodcastToDb(pod) && channelInfo != null) {
            // now update with field that not available from itune
            val cv = ContentValues()
            cv.put(PodcastsTable.DESCRIPTION, channelInfo.channelDescription)
            // update data description
            dataRepo.updatePodcast(cv, pod.collectionId)
            // now insert episode into db
            res = dataRepo.insertEpisodes(channelInfo.toListEpisodes(pod.collectionId))
        }

        // update with behavior object .. otherwise nothing to update
        if (res) updatePodcastSubject(PodcastState(pod.collectionId, PodcastState.SUBSCRIBED))

        return res
    }

    /*delete podcast and its episode */
    private fun unSubscribePodcast(pod: Podcast){
        //1. remove podcast from table and its image

        //2. remove all episode and state files

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
             Observable.just(updateEpisode(episode,cv))
        }.subscribeOn(Schedulers.io())
    }

    private fun updateEpisode(episode: Episode, cv: ContentValues): Boolean{
        var res = dataRepo.updateEpisode(cv,episode)
        if (res){
            val state = cv[EpisodeTable.STATE] as Int
            val transId = cv[EpisodeTable.DOWNLOAD_TRANS_ID] as String?
            val uniqueId = episode.uniqueId

            // update sync subject
            if (transId == null){
                updateEpisodeSubject(
                        EpisodeState(uniqueId, state, null))
            } else{
                updateEpisodeSubject(
                        EpisodeState(uniqueId, state, transId.toLong()))
            }

        }

        return res
    }

    fun getEpisodeObsevable(episode: Episode): Observable<Episode> {
        return Observable.defer {
            Observable.just(dataRepo.getEpisode(episode.uniqueId, episode.podcastId))
        }.subscribeOn(Schedulers.io())
    }


}