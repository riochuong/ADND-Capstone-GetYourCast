package getyourcasts.jd.com.getyourcasts.repository.remote.data;

import java.util.List;

/**
 * Created by chuondao on 8/9/17.
 */

public final class ItuneResponse {
    private List<Podcast> results;

    public List<Podcast> getResults() {
        return results;
    }

    public void setResults(List<Podcast> results) {
        this.results = results;
    }

    public ItuneResponse(List<Podcast> results) {
        this.results = results;
    }
}
