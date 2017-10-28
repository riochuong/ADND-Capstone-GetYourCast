package getyourcasts.jd.com.getyourcasts.update;

import android.os.Bundle;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Channel;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by chuondao on 8/16/17.
 */

public class UpdateJobService extends JobService {

    DataSourceRepo dataRepo = DataSourceRepo.getInstance(this);
    Disposable disposable;
    private FirebaseAnalytics mFirebaseAnalytics;
    private static final String FIREBASE_UPDATE_AVAILABLE  = "update_avail";
    private static final String FIREBASE_UPDATE_SIZE_KEY  = "update_size";
    private static final String TAG = UpdateJobService.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    public boolean onStartJob(JobParameters job) {
        createUpdateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Observer<Boolean>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                disposable = d;
                            }

                            @Override
                            public void onNext(Boolean isThereUpdate) {
                                if (isThereUpdate) {
                                    UpdateNotificationHelper
                                            .notifyNewUpdateAvailable(UpdateJobService.this);
                                    Log.d(TAG, "Updates are available for download");
                                }
                                else{
                                    Log.d(TAG, "No new Episode to be updated ");
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onComplete() {

                            }
                        }
                );
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (disposable != null) {
            disposable.dispose();
            disposable = null;
        }
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }

    private Observable<Boolean> createUpdateObservable() {
       return Observable.defer(
               () -> Observable.just(getUpdateEpisodeFromSubscribedPod())
       ).subscribeOn(Schedulers.io());
    }

    private Boolean getUpdateEpisodeFromSubscribedPod() {

        // get all subscribe podcast and episodes
        List<Podcast> podcastList = dataRepo.getAllPodcast();
        // nothing to query and to be updated
        if (podcastList.size() == 0){
            return false;
        }
        Map<Podcast, List<Episode>> updateFromNet = queryUpdateEpisodeFromNetwork(podcastList);
        Map<Podcast, Map<String,Episode>> currentEps = queryCurrentEpisodeFromDb(podcastList);
        Map<Podcast, List<Episode>> toUpdateEpMap = filterUpdateEpisodeForEachPodcast(
                currentEps,
                updateFromNet,
                podcastList
        );
        // ADD Firebase log here  LOG here
        Bundle fBundle = new Bundle();
        fBundle.putString(FIREBASE_UPDATE_SIZE_KEY, toUpdateEpMap.size()+"");
        mFirebaseAnalytics.logEvent(FIREBASE_UPDATE_AVAILABLE,fBundle);

        return toUpdateEpMap.size() > 0;
    }

    /**
     * go through each subscribed podcast and get the updated
     * list of all episodes. this method need to be called on
     * a seperate thread as it has some works done on network.
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



    private void clearOldUpdate () {
        if (dataRepo != null) {
            dataRepo.clearOldUpdates();
        }
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
          // clear old updates
          clearOldUpdate();
          for (Podcast podcast: podcastList) {
                List<Episode> toBeUpdateEp = new ArrayList<>();
                List<Episode> newFetchList = updateEps.get(podcast);
                Map<String, Episode> currentMap = currentEps.get(podcast);
                Set<String> keys = currentMap.keySet();
                // check for updated episodes
                for (Episode ep : newFetchList){
                    if (! keys.contains(ep.getUniqueId())){
                         // set the is new update field
                         ep.setIsNewUpdate(1);
                         // insert ep to database
                         dataRepo.insertEpisode(ep);
                         toBeUpdateEp.add(ep);
                     }
                }
                // only add to map if we have something
              if (toBeUpdateEp.size() > 0) {
                    podcastUpdate.put(podcast,toBeUpdateEp);
              }
          }
        return podcastUpdate;
    }

}
