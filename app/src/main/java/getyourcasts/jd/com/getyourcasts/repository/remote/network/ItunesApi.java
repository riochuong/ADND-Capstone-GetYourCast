package getyourcasts.jd.com.getyourcasts.repository.remote.network;

import getyourcasts.jd.com.getyourcasts.repository.remote.data.ItuneResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by chuondao on 8/9/17.
 */

public interface ItunesApi {

    @GET("/search")
    public Call<ItuneResponse> searchPodcast(
            @Query("media") String media,
            @Query("term") String podCastName);
}



