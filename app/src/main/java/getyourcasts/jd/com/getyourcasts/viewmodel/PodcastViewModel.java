package getyourcasts.jd.com.getyourcasts.viewmodel;

import android.content.ContentValues;

import java.util.List;
import java.util.Map;

import getyourcasts.jd.com.getyourcasts.repository.local.Contract;
import getyourcasts.jd.com.getyourcasts.repository.remote.DataRepository;
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Channel;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by chuondao on 8/10/17.
 */

public class PodcastViewModel {

    private static final int NUM_TOP_RESULTS = 20;
    private static PodcastViewModel INSTANCE = null;
    private DataRepository dataRepo;

    public PodcastViewModel(DataRepository dataRepo) {
        this.dataRepo = dataRepo;
    }

    public static PodcastViewModel getInstance(DataSourceRepo repo) {
        if (INSTANCE == null ) {
            INSTANCE = new PodcastViewModel(repo);
        }
        return INSTANCE;
    }

    /**
     * this subject will help synchronize between the search list adapter
     * and the detail podcast activity
     */
    private static PublishSubject<PodcastState> podcastSyncSubject = PublishSubject.create();

    /*subscribe to this subject to get
   * notice about change of the state of
   * the podcast either subscribed or not
   * */
    public static void subscribePodcastSubject(Observer<PodcastState> observer) {
        podcastSyncSubject.observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
    }

    /*get the source for synchronize */
    public static void updatePodcastSubject(PodcastState podState) {
        podcastSyncSubject.onNext(podState);
    }

    /**
     * this subject will help to synchronize between episode list episode and
     * episode detail info for the downloading status of the episode
     * this will emit progress and episode id
     */
    private static PublishSubject<EpisodeState> episodeSyncSubject = PublishSubject.create();

    public static void subscribeEpisodeSubject(Observer<EpisodeState> observer) {
        episodeSyncSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public static void updateEpisodeSubject(EpisodeState epState) {
        episodeSyncSubject.onNext(epState);
    }




    /* class to deliver sync state between different item related to epidsode */


    public Observable<List<Podcast>> getPodcastSearchObservable(String term) {
        return Observable.defer(() -> Observable.just(dataRepo.searchPodcast(term)))
                .flatMapIterable(podcasts -> podcasts)
                .filter(podcast -> podcast.getTrackCount() > 0)
                .toList().toObservable().subscribeOn(Schedulers.io());


    }

    public Observable<Boolean> getSubscribeObservable(Podcast pod) {
        return Observable.defer(() -> Observable.just(subscribePodcast(pod)))
                .subscribeOn(Schedulers.io());
    }

    //
    public Observable<Boolean> getSubscribeObservable(Podcast pod, Channel channel) {
        return Observable.defer(() -> Observable.just(subscribePodcast(pod, channel)))
                .subscribeOn(Schedulers.io());
    }

    //
//    fun getUnsubscribeObservable() : Observable<Boolean> {
//        return Observable.just(true)
//    }
//
    private Boolean subscribePodcast(Podcast pod, Channel channelInfo) {
        boolean res = false;
        if (dataRepo.insertPodcast(pod) && channelInfo != null) {
            // now update with field that not available from itune
            ContentValues cv = new ContentValues();
            cv.put(Contract.PodcastTable.DESCRIPTION, channelInfo.getChannelDescription());
            // update data description
            res = dataRepo.updatePodcast(cv, pod.getCollectionId());
            // now insert episode into db
            res = res && dataRepo.insertEpisodes(channelInfo.toListEpisodes(pod.getCollectionId()));
        }

        // update with behavior object .. otherwise nothing to update
        if (res) updatePodcastSubject(new PodcastState(pod.getCollectionId(), PodcastState.SUBSCRIBED));

        return res;
    }

    //
//    /*delete podcast and its episode */
//    private fun unSubscribePodcast(pod: Podcast){
//        //1. remove podcast from table and its image
//
//        //2. remove all episode and state files
//
//    }
//
//
    private Boolean subscribePodcast(Podcast pod) {
        // fetch rss feed
        Channel channelInfo = dataRepo.downloadFeed(pod.getFeedUrl());
        if (channelInfo != null) {
            return subscribePodcast(pod, channelInfo);
        }
        return false;
    }

    //
    public Observable<List<Episode>> getAllEpisodesOfPodcastObservable(String podcastId) {
        return Observable.defer(
                () -> Observable.just(dataRepo.getAllEpisodesOfPodcast(podcastId)))
                .subscribeOn(Schedulers.io());
    }

    //
//
//
    public Observable<Channel> getChannelFeedObservable(String feedUrl) {
        return Observable.defer(() -> Observable.just(dataRepo.downloadFeed(feedUrl)))
                .subscribeOn(Schedulers.computation());
    }
//

    public Observable<Podcast> getPodcastObservable(String podcastId) {
        return Observable.defer(
                () -> {
                    try {
                        Podcast podcast = dataRepo.getPodcast(podcastId);
                        if (podcast != null) {
                            return Observable.just(podcast);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return Observable.just(Podcast.getEmptyPodcast());
                }
        ).subscribeOn(Schedulers.io());
    }
//
//
    public Observable<List<Podcast>>  getAllSubscribedPodcastObservable(){
        return Observable.defer(() ->
            Observable.just (getAllPodcasts()))
        .subscribeOn(Schedulers.io());
    }

    private List<Podcast> getAllPodcasts() {
        return dataRepo.getAllPodcast();
    }
//
//
//    /**
//     * get update Pod
//     */
    public  Observable<Boolean> getUpdateEpisodeObservable (Episode episode, ContentValues cv)  {
        return Observable.defer (() -> Observable.just(updateEpisode(episode,cv)))
        .subscribeOn(Schedulers.io());
    }

    public  Observable<Boolean> getUpdatePodcastObservable (Podcast podcast, ContentValues cv)  {
        return Observable.defer (
                () -> Observable.just(dataRepo.updatePodcast(cv, podcast.getCollectionId()))
                .subscribeOn(Schedulers.io()));
    }
//
    private  Boolean updateEpisode(Episode episode, ContentValues cv) {
       return  dataRepo.updateEpisode(cv,episode);
    }
//
    public Observable<Episode> getEpisodeObsevable(Episode episode) {
        return Observable.defer (
                () ->  Observable.just(
                                dataRepo.getEpisode(episode.getUniqueId(), episode.getPodcastId())))
                    .subscribeOn(Schedulers.io());
    }

    public Observable<Map<Podcast,List<Episode>>> getUpdateListObservable (){

        return Observable.defer (
                () -> Observable.just(
                        dataRepo.getNewUpdate()
                )
        ).subscribeOn(Schedulers.io());
    }

}
