package getyourcasts.jd.com.getyourcasts.viewmodel;


import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.List;


import getyourcasts.jd.com.getyourcasts.repository.remote.DataRepository;
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode;

/**
 * Created by chuondao on 9/13/17.
 */

public class EpisodeOfPodcastLoader extends AsyncTaskLoader<List<Episode>> {

    String podcastId;
    DataRepository dataRepo;

    public EpisodeOfPodcastLoader(Context context, String podcastId) {
        super(context);
        this.podcastId = podcastId;
        dataRepo = DataSourceRepo.getInstance(context);
    }

    @Override
    public List<Episode> loadInBackground() {
        List<Episode> epList = dataRepo.getAllEpisodesOfPodcast(this.podcastId);
        return epList;
    }
}
