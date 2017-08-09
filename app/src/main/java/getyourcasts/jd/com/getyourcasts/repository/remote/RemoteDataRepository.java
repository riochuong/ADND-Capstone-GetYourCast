package getyourcasts.jd.com.getyourcasts.repository.remote;

import android.content.ContentValues;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import getyourcasts.jd.com.getyourcasts.repository.remote.data.Channel;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.FeedResponse;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.ItuneResponse;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast;
import getyourcasts.jd.com.getyourcasts.repository.remote.network.NetworkHelper;
import okhttp3.Request;
import retrofit2.Call;

/**
 * Created by chuondao on 8/9/17.
 */

public class RemoteDataRepository implements DataRepository {


    private static final String TAG = "REMOTE_REPO";
    private static RemoteDataRepository instance = null;

    /**
     * getter for data instance
     */
    public static RemoteDataRepository getDataInstance() {
        if (instance == null) {
            instance =  new RemoteDataRepository();
        }
        return instance;
    }

    private RemoteDataRepository() {
    }

    @NotNull
    @Override
    public List<Podcast> searchPodcast(@NotNull String title) {
        List<Podcast> results = new ArrayList<>();
        Call<ItuneResponse> searchUrl = NetworkHelper.getHelperInstance().searchPodcast(title);
        Request http_req = searchUrl.request();
        Log.d(TAG, "Request "+http_req.toString());
        try {
            retrofit2.Response<ItuneResponse> response = searchUrl.execute();
        /*verify if response is good*/
            if (response.isSuccessful()) {
                // parse data here
                if (response.body() != null) return response.body().getResults();
                else {
                    Log.e(TAG, "Failed tor retreive message body");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }

    @Override
    public Channel fetchEpisodesFromFeedUrl(String feedUrl) {
        // get fetch RSS opt
        Call<FeedResponse> fetchRssOpt = NetworkHelper.getHelperInstance().fetchRss(feedUrl);

        if (fetchRssOpt != null) {
            Request req = fetchRssOpt.request();
            Log.d(TAG, "SEND REQUEST "+req.toString());
            try {
                retrofit2.Response<FeedResponse> feedResponse = fetchRssOpt.execute();
                if (feedResponse.isSuccessful()) {
                    if (feedResponse.body() != null) {
                        return feedResponse.body().getChannel();
                    } else {
                        Log.e(TAG, "Failed to fetch Media Item");
                    }
                }
            } catch(Exception e) {
                Log.e(TAG, "FAILED REQUEST $req. "+e.getMessage());
                e.printStackTrace();
            }

        }

        // return empty list
        return null;

    }


    @Override
    public Podcast getPodcast(@NotNull String var1) {
        return null;
    }


    @Override
    public List getAllPodcast() {
        return null;
    }


    @Override
    public Channel downloadFeed(@NotNull String var1) {
        return null;
    }


    @Override
    public List getAllEpisodesOfPodcast(@NotNull String var1) {
        return null;
    }


    @Override
    public Episode getEpisode(@NotNull String var1, @NotNull String var2) {
        return null;
    }

    @Override
    public boolean updatePodcast(@NotNull ContentValues var1, @NotNull String var2) {
        return false;
    }

    @Override
    public boolean updateEpisode(@NotNull ContentValues var1, @NotNull Episode var2) {
        return false;
    }

    @Override
    public boolean insertPodcast(@NotNull Podcast var1) {
        return false;
    }

    @Override
    public boolean insertEpisode(@NotNull Episode var1) {
        return false;
    }

    @Override
    public boolean insertEpisodes(@NotNull List<Episode> var1) {
        return false;
    }


}
