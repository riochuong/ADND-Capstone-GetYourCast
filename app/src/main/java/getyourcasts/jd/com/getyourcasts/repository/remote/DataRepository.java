package getyourcasts.jd.com.getyourcasts.repository.remote;

import android.content.ContentValues;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import getyourcasts.jd.com.getyourcasts.repository.remote.data.Channel;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast;


/**
 * Created by chuondao on 8/8/17.
 */


public interface DataRepository {
    @NotNull
    List<Podcast> searchPodcast(@NotNull String var1);

    @Nullable
    Podcast getPodcast(@NotNull String var1);

    @NotNull
    List getAllPodcast();

    @Nullable
    Channel downloadFeed(@NotNull String var1);

    @NotNull
    List<Episode> getAllEpisodesOfPodcast(@NotNull String var1);

    @NotNull
    Episode getEpisode(@NotNull String var1, @NotNull String var2);

    boolean updatePodcast(@NotNull ContentValues var1, @NotNull String var2);

    boolean updateEpisode(@NotNull ContentValues var1, @NotNull Episode var2);

    boolean insertPodcast(@NotNull Podcast var1);

    boolean insertEpisode(@NotNull Episode var1);

    boolean insertEpisodes(@NotNull List<Episode> var1);

    Channel fetchEpisodesFromFeedUrl(String feedUrl);
}


