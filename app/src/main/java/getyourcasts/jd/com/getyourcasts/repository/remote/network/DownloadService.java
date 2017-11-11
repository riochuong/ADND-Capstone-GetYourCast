package getyourcasts.jd.com.getyourcasts.repository.remote.network;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.tonyodev.fetch.Fetch;
import com.tonyodev.fetch.listener.FetchListener;
import com.tonyodev.fetch.request.Request;
import com.tonyodev.fetch.request.RequestInfo;

import java.util.HashMap;
import java.util.Map;

import getyourcasts.jd.com.getyourcasts.R;
import getyourcasts.jd.com.getyourcasts.repository.local.Contract;
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode;
import getyourcasts.jd.com.getyourcasts.util.StorageUtil;
import getyourcasts.jd.com.getyourcasts.view.adapter.EpisodeDownloadListener;
import getyourcasts.jd.com.getyourcasts.viewmodel.EpisodeState;
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Created by chuondao on 9/11/17.
 */

public class DownloadService extends Service {

    private IBinder binder = new DownloadServiceBinder();
    private Fetch fetcher;
    private Map<Long,Long> listReqIds = new HashMap<>();
    private NotificationManager notifyManager;
    private int currentId = 0;

    private PodcastViewModel viewModel ;
    private static final String TAG = DownloadService.class.getSimpleName();
    private static final int CONCC_LIMIT = 2;



    private void initFetch(){
        fetcher = Fetch.newInstance(this);
        fetcher.setConcurrentDownloadsLimit(CONCC_LIMIT);
        listReqIds  = new HashMap<>();
        // remove all request from fetch when first start !!
        fetcher.removeRequests();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,"Download Service bound ! ");
        initFetch();
        viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(this));
        return binder;
    }


    @Override
    public void onRebind(Intent intent) {
        if (! fetcher.isValid()){
            initFetch();
        }
        super.onRebind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG,"Download Service Unbound");
        return super.onUnbind(intent);
    }



    public class DownloadServiceBinder extends Binder {
        public  DownloadService getService()   {
            return DownloadService.this;
        }
    }

    private NotificationCompat.Builder buildProgressNotification (String fileName ) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(fileName)
                .setContentText(getString(R.string.download_in_prog))
                .setSmallIcon(R.mipmap.ic_todownload);
        return mBuilder;
    }

    private void registerLisenerForNotiProg( Episode ep,
                                            long transId,
                                             NotificationCompat.Builder notiBuilder,
                                            int notiId,
                                             String fullUrl){
        registerListener(new EpisodeDownloadListener(transId) {
            @Override
            public void onProgressUpdate(int progress) {
                notiBuilder.setProgress(100, progress, false);
                notifyManager.notify(notiId, notiBuilder.build());
                PodcastViewModel.updateEpisodeSubject(
                        new EpisodeState(ep.getUniqueId(),
                                EpisodeState.DOWNLOADING,
                                transId));
            }

            @Override
            public void onComplete() {
                // remove progress bar
                notiBuilder.setProgress(0,0,false);
                // notify manager
                notiBuilder.setContentText(getString(R.string.download_complete));
                notifyManager.notify(notiId, notiBuilder.build());
                // remove request to avoid not able to download it again
                fetcher.removeRequest(transId);
                // update the DB
                ContentValues cvUpdate = new ContentValues();
                cvUpdate.put(Contract.EpisodeTable.LOCAL_URL, fullUrl);
                cvUpdate.put(Contract.EpisodeTable.DOWNLOAD_STATUS, EpisodeState.DOWNLOADED);
                viewModel.getUpdateEpisodeObservable(ep, cvUpdate)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                new Observer<Boolean>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {

                                    }

                                    @Override
                                    public void onNext(Boolean res) {
                                        if (res) {
                                            Log.d(TAG,"Successfully update Downloaded Episode "+ep);
                                            if (res) PodcastViewModel.updateEpisodeSubject(
                                                    new EpisodeState(ep.getUniqueId(),
                                                            EpisodeState.DOWNLOADED,
                                                            transId));
                                        } else {
                                            Log.e(TAG, "Failed update downloaded episode "+ep);
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

            }

            @Override
            public void onStop() {
                PodcastViewModel.updateEpisodeSubject(
                        new EpisodeState(ep.getUniqueId(),
                                EpisodeState.FETCHED,
                                transId));
                notiBuilder.setProgress(0,0,false);
                notiBuilder.setContentText(getString(R.string.download_cancelled));
                notifyManager.notify(notiId, notiBuilder.build());
            }

            @Override
            public void onError() {
                // failed to download remove everything even partial file
//                fetcher.removeRequest(transId);
                //fetcher.remove(transId);
                Log.e(TAG, "Failed to request download "+ep.getDownloadUrl());
                ContentValues cvUpdate = new ContentValues();
                cvUpdate.put(Contract.EpisodeTable.LOCAL_URL, "");
                cvUpdate.put(Contract.EpisodeTable.DOWNLOAD_STATUS, EpisodeState.FETCHED);
                viewModel.getUpdateEpisodeObservable(ep,cvUpdate)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                new Observer<Boolean>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {

                                    }

                                    @Override
                                    public void onNext(Boolean res) {
                                        if (res) {
                                            Log.d(TAG,"Failed to download Epsiode. Update Episode as Fetched only  " +
                                                    ""+ep);
                                            PodcastViewModel.updateEpisodeSubject(
                                                    new EpisodeState(ep.getUniqueId(),
                                                            EpisodeState.FETCHED,
                                                            transId));
                                        }
                                        else{
                                            Log.e(TAG, "Failed to update DB as Fetched after failed to request " +
                                                    "download");
                                        }

                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                    }

                                    @Override
                                    public void onComplete() {

                                    }
                                }
                        );

            }
        });


    }


    /**
     * the request download link
     * will be enqued and the id for the request will be returned
     * @return: -1 if failed to enqueue otherwise valid Id will be returned
     */
    public Long requestDownLoad (Episode episode ,
                          String url,
                          String dirPath ,
                          String filename ){
        if (! NetworkHelper.isConnectedToNetwork(this)) {
            PodcastViewModel.updateEpisodeSubject(
                    new EpisodeState(episode.getUniqueId(), EpisodeState.FETCHED, -1));
            NetworkHelper.showNetworkErrorDialog(this);
            return -1L;
        }

        if (fetcher.isValid()) {
            Request req = new Request(url, dirPath, filename);
            // remove duplicate filename in the db
            StorageUtil.INSTANCE.cleanUpOldFile(episode, this);
            // temporary fix to remove request at beginning
            if (fetcher.contains(req)) {
                RequestInfo info = fetcher.get(req);
                if (info != null){
                    fetcher.remove(info.getId());
                }
            }
            long id = fetcher.enqueue(req);
            // failed to enqueue
            if (id < 0) {
                Log.d(TAG, "Failed to enqueue !");
                return -1L;
            }
            listReqIds.put(id,id);
            String fullUrl = dirPath+"/"+filename;
            registerLisenerForNotiProg(episode,
                    id,
                    buildProgressNotification(episode.getTitle()),
                    currentId++,
                    fullUrl);

            // notify other views to change status
            return id;
        }

        return -1L;
    }



    public void registerListener (FetchListener listener){
        if (fetcher.isValid()){
            fetcher.addFetchListener(listener);
            Log.d(TAG,"Successfully register Listener for Fetch download");
        }else{
            Log.d(TAG, "Failed to register Listener!!");
        }
    }

    public void unregisterListener(FetchListener listener){
        if (fetcher.isValid()){
            fetcher.removeFetchListener(listener);
        }
    }

    public void requestStopDownload(long transId) {
        if (fetcher.isValid()) {
            fetcher.pause(transId);
            fetcher.removeRequest(transId);
        }
    }
}
