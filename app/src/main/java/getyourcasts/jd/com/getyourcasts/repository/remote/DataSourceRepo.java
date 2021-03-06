package getyourcasts.jd.com.getyourcasts.repository.remote;

import android.content.ContentValues;
import android.content.Context;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

import getyourcasts.jd.com.getyourcasts.repository.local.LocalDataRepository;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Channel;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast;

/**
 * Created by chuondao on 8/9/17.
 */

public class DataSourceRepo implements   DataRepository {

    private Context ctx;
    private LocalDataRepository localRepo;
    private RemoteDataRepository remoteRepo;

    public DataSourceRepo(Context ctx) {
        this.ctx = ctx;
        localRepo = new LocalDataRepository(ctx);
        remoteRepo = RemoteDataRepository.getDataInstance();
    }

    private static DataSourceRepo INST = null;

    public static DataSourceRepo getInstance(Context ctx) {
        if (INST == null){
            INST = new DataSourceRepo(ctx);
        }
        return INST;
    }


    @NotNull
    @Override
    public List<Podcast> searchPodcast(@NotNull String title) {
        return remoteRepo.searchPodcast(title);
    }

    @Nullable
    @Override
    public Podcast getPodcast(@NotNull String podcastId) {
        return localRepo.getPodcast(podcastId);
    }

    @NotNull
    @Override
    public List<Podcast> getAllPodcast() {
        return localRepo.getAllPodcast();
    }

    @Nullable
    @Override
    public Channel downloadFeed(@NotNull String feedUrl) {
        return remoteRepo.fetchEpisodesFromFeedUrl(feedUrl);
    }

    @NotNull
    @Override
    public List<Episode> getAllEpisodesOfPodcast(@NotNull String podcastId) {
        return localRepo.getAllEpisodesOfPodcast(podcastId);
    }

    @NotNull
    @Override
    public Episode getEpisode(@NotNull String episodeTitle, @NotNull String podcastId) {
        return localRepo.getEpisode(episodeTitle, podcastId);
    }

    @Override
    public boolean updatePodcast(@NotNull ContentValues cv, @NotNull String podcastId) {
       return  localRepo.updatePodcast(cv, podcastId);
    }

    @Override
    public boolean updateEpisode(@NotNull ContentValues cv, @NotNull Episode episode) {
        return localRepo.updateEpisode(cv,episode);
    }

    @Override
    public boolean insertPodcast(@NotNull Podcast pod) {
        return localRepo.insertPodcast(pod);
    }

    @Override
    public boolean insertEpisode(@NotNull Episode episode) {
        return localRepo.insertEpisode(episode);
    }

    @Override
    public boolean insertEpisodes(@NotNull List<Episode> episodes) {
        return localRepo.insertEpisodes(episodes);
    }

    @Override
    public Channel fetchEpisodesFromFeedUrl(String feedUrl) {
        return remoteRepo.fetchEpisodesFromFeedUrl(feedUrl);
    }

    @Override
    public void clearOldUpdates() {
        localRepo.clearOldUpdates();
    }

    @Override
    public Map<Podcast, List<Episode>> getNewUpdate() {
       return localRepo.getNewUpdate();
    }

    @Override
    public boolean deleteEpisodes(String epUniqueId) {
        return localRepo.deleteEpisodes(epUniqueId);
    }

    @Override
    public boolean deletePodcast(String podcastId) {
        return localRepo.deletePodcast(podcastId);
    }

    @Override
    public boolean deleteEpisodeOfPodcast(String podcastId) {
        return localRepo.deleteEpisodeOfPodcast(podcastId);
    }

    @Override
    public List<Episode> getDownloadedEpisodes() {
        return localRepo.getDownloadedEpisodes();
    }
}
