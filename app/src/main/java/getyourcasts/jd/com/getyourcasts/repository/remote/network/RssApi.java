package getyourcasts.jd.com.getyourcasts.repository.remote.network;

import getyourcasts.jd.com.getyourcasts.repository.remote.data.FeedResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by chuondao on 8/9/17.
 */

public interface RssApi {
    @GET
    public Call<FeedResponse> fetchPodcastInfo(
            @Url String feedUrl
    );
}
