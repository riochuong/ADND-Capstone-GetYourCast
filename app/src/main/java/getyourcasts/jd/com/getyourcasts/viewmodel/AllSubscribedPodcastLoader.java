package getyourcasts.jd.com.getyourcasts.viewmodel;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.List;

import getyourcasts.jd.com.getyourcasts.repository.remote.DataRepository;
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast;

/**
 * Created by chuondao on 9/13/17.
 */

public class AllSubscribedPodcastLoader extends AsyncTaskLoader<List<Podcast>> {

    DataRepository dataRepo;

    public AllSubscribedPodcastLoader(Context context) {
        super(context);
        dataRepo = DataSourceRepo.getInstance(context);
    }

    @Override
    public List<Podcast> loadInBackground() {
        List<Podcast> podList = dataRepo.getAllPodcast();
        return podList;
    }
}