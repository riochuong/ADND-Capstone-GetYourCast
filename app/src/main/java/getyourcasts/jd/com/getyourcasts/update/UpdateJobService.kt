package getyourcasts.jd.com.getyourcasts.update

import android.os.Bundle
import android.util.Log

import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import com.google.firebase.analytics.FirebaseAnalytics

import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.Callable

import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Channel
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by chuondao on 8/16/17.
 */

class UpdateJobService : JobService() {

    internal var dataRepo: DataSourceRepo? = DataSourceRepo.getInstance(this)
    internal var disposable: Disposable? = null
    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    private// get all subscribe podcast and episodes
            // nothing to query and to be updated
            // ADD Firebase log here  LOG here
    val updateEpisodeFromSubscribedPod: Boolean
        get() {
            val podcastList = dataRepo!!.allPodcast
            if (podcastList.size == 0) {
                return false
            }
            val updateFromNet = queryUpdateEpisodeFromNetwork(podcastList)
            val currentEps = queryCurrentEpisodeFromDb(podcastList)
            val toUpdateEpMap = filterUpdateEpisodeForEachPodcast(
                    currentEps,
                    updateFromNet,
                    podcastList
            )
            val fBundle = Bundle()
            fBundle.putString(FIREBASE_UPDATE_SIZE_KEY, toUpdateEpMap.size.toString() + "")
            mFirebaseAnalytics!!.logEvent(FIREBASE_UPDATE_AVAILABLE, fBundle)

            return toUpdateEpMap.isNotEmpty()
        }

    override fun onCreate() {
        super.onCreate()
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
    }

    override fun onStartJob(job: JobParameters): Boolean {
        createUpdateObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        object : Observer<Boolean> {
                            override fun onSubscribe(d: Disposable) {
                                disposable = d
                            }

                            override fun onNext(isThereUpdate: Boolean) {
                                if (isThereUpdate) {
                                    UpdateNotificationHelper
                                            .notifyNewUpdateAvailable(this@UpdateJobService)
                                    Log.d(TAG, "Updates are available for download")
                                } else {
                                    Log.d(TAG, "No new Episode to be updated ")
                                }
                            }

                            override fun onError(e: Throwable) {
                                e.printStackTrace()
                            }

                            override fun onComplete() {

                            }
                        }
                )
        return true
    }

    override fun onDestroy() {
        if (disposable != null) {
            disposable!!.dispose()
            disposable = null
        }
        super.onDestroy()
    }

    override fun onStopJob(job: JobParameters): Boolean {
        return false
    }

    private fun createUpdateObservable(): Observable<Boolean> {
        return Observable.defer { Observable.just(updateEpisodeFromSubscribedPod) }.subscribeOn(Schedulers.io())
    }

    /**
     * go through each subscribed podcast and get the updated
     * list of all episodes. this method need to be called on
     * a seperate thread as it has some works done on network.
     *
     * @param pList
     * @return
     */
    private fun queryUpdateEpisodeFromNetwork(pList: List<Podcast>): Map<Podcast, List<Episode>> {
        val updateEpisodeMap = HashMap<Podcast, List<Episode>>()
        for (p in pList) {
            val channel = dataRepo!!.fetchEpisodesFromFeedUrl(p.feedUrl)
            if (channel != null) {
                val updateList = channel.toListEpisodes(p.collectionId)
                updateEpisodeMap.put(p, updateList)
            }
        }
        return updateEpisodeMap
    }


    private fun clearOldUpdate() {
        if (dataRepo != null) {
            dataRepo!!.clearOldUpdates()
        }
    }

    /**
     * get current episode list from the map
     * @param pList
     * @return
     */
    private fun queryCurrentEpisodeFromDb(pList: List<Podcast>): Map<Podcast, Map<String, Episode>> {
        val currentEpMap = HashMap<Podcast, Map<String, Episode>>()
        for (p in pList) {
            val updateList = dataRepo!!.getAllEpisodesOfPodcast(p.collectionId)
            //map episode to its unique key for easier filter
            val epMap = HashMap<String, Episode>()
            for (ep in updateList) {
                epMap.put(ep.uniqueId, ep)
            }
            currentEpMap.put(p, epMap)
        }
        return currentEpMap

    }

    /**
     * filter out new episode from the new fetch and the current list
     * @param currentEps
     * @param updateEps
     * @param podcastList
     * @return
     */
    private fun filterUpdateEpisodeForEachPodcast(
            currentEps: Map<Podcast, Map<String, Episode>>,
            updateEps: Map<Podcast, List<Episode>>,
            podcastList: List<Podcast>
    ): Map<Podcast, List<Episode>> {
        val podcastUpdate = HashMap<Podcast, List<Episode>>()
        // clear old updates
        clearOldUpdate()
        for (podcast in podcastList) {
            val newFetchList = updateEps[podcast]
            val currentMap = currentEps[podcast]
            val keysInCurrentMap = currentMap?.keys
            // check for updated episodes
            if (newFetchList != null && newFetchList.isNotEmpty() && keysInCurrentMap != null) {
                val updateList = newFetchList.filter { it.uniqueId !in keysInCurrentMap }
                // update database
                updateList.forEach {
                    it.isNewUpdate = 1
                    dataRepo!!.insertEpisode(it)
                }
                // add the update list
                if (updateList.isNotEmpty()) {
                    podcastUpdate.put(podcast, updateList)
                }
            }
        }
        return podcastUpdate
    }

    companion object {
        private val FIREBASE_UPDATE_AVAILABLE = "update_avail"
        private val FIREBASE_UPDATE_SIZE_KEY = "update_size"
        private val TAG = UpdateJobService::class.java.simpleName
    }

}
