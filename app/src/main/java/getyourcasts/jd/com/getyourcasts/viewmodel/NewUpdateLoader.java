package getyourcasts.jd.com.getyourcasts.viewmodel;


import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.List;
import java.util.Map;

import getyourcasts.jd.com.getyourcasts.repository.remote.DataRepository;
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast;

/**
 * Created by chuondao on 9/13/17.
 */

public class NewUpdateLoader  extends AsyncTaskLoader<Map<Podcast,List<Episode>>> {
    DataRepository dataRepo;
    public NewUpdateLoader(Context context) {
        super(context);
        dataRepo = DataSourceRepo.getInstance(context);
    }

    @Override
    public Map<Podcast, List<Episode>> loadInBackground() {
        return dataRepo.getNewUpdate();
    }
}
