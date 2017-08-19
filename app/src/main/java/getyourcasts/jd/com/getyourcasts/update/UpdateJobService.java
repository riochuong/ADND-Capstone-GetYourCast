package getyourcasts.jd.com.getyourcasts.update;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Channel;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by chuondao on 8/16/17.
 */

public class UpdateJobService extends JobService {

    DataSourceRepo dataRepo = DataSourceRepo.getInstance(this);

    @Override
    public boolean onStartJob(JobParameters job) {
        createUpdateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Observer<Map<Podcast, List<Episode>>>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(Map<Podcast, List<Episode>> podcastListMap) {
                                UpdateNotificationHelper.
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {

                            }
                        }
                );
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }

    private Observable<Map<Podcast, List<Episode>> > createUpdateObservable() {
       return Observable.defer(
               () -> Observable.just(getUpdateEpisodeFromSubscribedPod())
       ).subscribeOn(Schedulers.io());
    }

    private Map<Podcast, List<Episode>> getUpdateEpisodeFromSubscribedPod() {

        // get all subscribe podcast and episodes
        List<Podcast> podcastList = dataRepo.getAllPodcast();
        Map<Podcast, List<Episode>> updateFromNet = queryUpdateEpisodeFromNetwork(podcastList);
        Map<Podcast, Map<String,Episode>> currentEps = queryCurrentEpisodeFromDb(podcastList);
        Map<Podcast, List<Episode>> toUpdateEpMap = filterUpdateEpisodeForEachPodcast(
                currentEps,
                updateFromNet,
                podcastList
        );
        return toUpdateEpMap;
    }

    /**
     * go through each subscribed podcast and get the updated
     * list of all episodes
     *
     * @param pList
     * @return
     */
    private Map<Podcast, List<Episode>> queryUpdateEpisodeFromNetwork(List<Podcast> pList) {
        Map<Podcast, List<Episode>> updateEpisodeMap = new HashMap<>();
        for (Podcast p : pList) {
            Channel channel = dataRepo.fetchEpisodesFromFeedUrl(p.getFeedUrl());
            if (channel != null) {
                List<Episode> updateList = channel.toListEpisodes(p.getCollectionId());
                updateEpisodeMap.put(p, updateList);
            }
        }
        return updateEpisodeMap;

    }

    /**
     * get current episode list from the map
     * @param pList
     * @return
     */
    private Map<Podcast, Map<String,Episode>> queryCurrentEpisodeFromDb(List<Podcast> pList) {
        Map<Podcast, Map<String,Episode>> currentEpMap = new HashMap<>();
        for (Podcast p : pList) {
            List<Episode> updateList = dataRepo.getAllEpisodesOfPodcast(p.getCollectionId());
            //map episode to its unique key for easier filter
            Map<String, Episode> epMap = new HashMap<>();
            for (Episode ep: updateList) {
                epMap.put(ep.getUniqueId(), ep);
            }
            currentEpMap.put(p, epMap);
        }
        return currentEpMap;

    }

    /**
     * filter out new episode from the new fetch and the current list
     * @param currentEps
     * @param updateEps
     * @param podcastList
     * @return
     */
    private Map<Podcast, List<Episode>> filterUpdateEpisodeForEachPodcast(
            Map<Podcast, Map<String,Episode>> currentEps,
            Map<Podcast, List<Episode>> updateEps,
            List<Podcast> podcastList
    )
    {
          Map<Podcast, List<Episode>> podcastUpdate = new HashMap<>();
          for (Podcast podcast: podcastList) {
                List<Episode> toBeUpdateEp = new ArrayList<>();
                List<Episode> newFetchList = updateEps.get(podcast.getCollectionId());
                Map<String, Episode> currentMap = currentEps.get(podcast.getCollectionId());
                // check for updated episodes
                for (Episode ep : newFetchList){
                     if (!currentMap.containsKey(ep)){
                         toBeUpdateEp.add(ep);
                     }
                }
                // only add to map if we have something
              if (toBeUpdateEp.size() >0) {
                    podcastUpdate.put(podcast,toBeUpdateEp);
              }
          }
        return podcastUpdate;
    }

}
