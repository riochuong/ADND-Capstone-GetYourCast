package getyourcasts.jd.com.getyourcasts.repository.remote.network;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;

import java.net.URL;

import getyourcasts.jd.com.getyourcasts.R;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.FeedResponse;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.ItuneResponse;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

/**
 * Created by chuondao on 8/9/17.
 */

public class NetworkHelper {
    //private val API_URL = "https://itunes.apple.com/search?media=podcast&term=%s"
    private String BASE_ITUNES_URL = "https://itunes.apple.com";

    private ItunesApi ituneApi;

    private OkHttpClient okHttpClient;


    private static final String PODCAST_TERM = "podcast";
    private static NetworkHelper instance = null;

    /**
     * try to make this class signleton
     */
    public static NetworkHelper getHelperInstance()

    {
        if (instance == null) {
            instance = new NetworkHelper();
        }
        return instance;
    }

    public NetworkHelper() {
        Retrofit itunesRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_ITUNES_URL)
                .addConverterFactory(MoshiConverterFactory.create()).build();

        ituneApi = itunesRetrofit.create(ItunesApi.class);

        //val interceptor = HttpLoggingInterceptor()
        //interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        okHttpClient = new OkHttpClient.Builder().build();
    }


    /**
     * generate call for Itunes API service
     */
    public Call<ItuneResponse> searchPodcast(String term)

    {
        return ituneApi.searchPodcast(PODCAST_TERM, term);
    }


    public Call<FeedResponse> fetchRss(String feedUrl)

    {

        String baseUrl = getBaseUrl(feedUrl);

        if (baseUrl == null) {
            return null;
        }

        Retrofit rssRetrofit = new Retrofit.Builder()
                .baseUrl(getBaseUrl(feedUrl))
                .client(okHttpClient)
                .addConverterFactory(SimpleXmlConverterFactory.createNonStrict(new Persister( new AnnotationStrategy())))
                .build();


        RssApi rssAdapter = rssRetrofit.create(RssApi.class);
        return rssAdapter.fetchPodcastInfo(feedUrl);
    }

    private String getBaseUrl(String fullUrl)

    {
        try {
            URL url = new URL(fullUrl.trim());
            String baseUrl = url.getProtocol() + "://" + url.getHost();
            return baseUrl;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isConnectedToNetwork(Context ctx) {
        ConnectivityManager cm =
                (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (cm != null) {
            networkInfo = cm.getActiveNetworkInfo();
        }
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }


}
