package getyourcasts.jd.com.getyourcasts.repository.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import getyourcasts.jd.com.getyourcasts.repository.remote.DataRepository;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Channel;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast;

/**
 * Created by chuondao on 8/8/17.
 */

public class LocalDataRepository implements DataRepository {

    private Context context;

    public LocalDataRepository(Context ctx) {
        context = ctx;
    }

    @NotNull
    @Override
    public List searchPodcast(@NotNull String podcastId) {
       return null;
    }

    @Nullable
    @Override
    public Podcast getPodcast(@NotNull String podcastId) {
        Uri uri = Contract.PodcastTable.URI_ID.buildUpon().appendPath(podcastId).build();
        try {
            Cursor cursor = this.context.getContentResolver().query(uri,
                                            null,null,null,null);
            return Podcast.fromCursor(cursor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  null;
    }

    @NotNull
    @Override
    public List<Podcast> getAllPodcast() {
        List<Podcast> list = new ArrayList();
        Uri uri = Contract.PodcastTable.URI;
        try {
            Cursor cursor = this.context.getContentResolver().query(uri,
                    null,null,null,null);
            return convertToPodcastList(cursor);
        }
        catch(Exception e ) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * convert cursor query to podcast list
     * @param cursor
     * @return
     */
    private  List<Podcast> convertToPodcastList (Cursor cursor) {
        List<Podcast> list = new ArrayList<>();
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount() ; i++) {
            cursor.moveToPosition(i);
            list.add(Podcast.fromCursor(cursor));
        }
        return list;
    }

    @Nullable
    @Override
    public Channel downloadFeed(@NotNull String var1) {
        return null;
    }

    @NotNull
    @Override
    public List getAllEpisodesOfPodcast(@NotNull String podcastId) {
        List<Episode> list = new ArrayList<>();
        try {
            Uri uri = Contract.EpisodeTable.URI_OF_PODCAST.buildUpon().appendPath(podcastId).build();
            Cursor cursor = this.context.getContentResolver().query(uri,
                    null,null,null,null);
            return convertToEpisodeList(cursor);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


    private List<Episode> convertToEpisodeList (Cursor cursor) {
        List<Episode> list = new ArrayList<>();
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount() ; i++) {
            cursor.moveToPosition(i);
            list.add(Episode.fromCursor(cursor));
        }
        return list;
    }

    @NotNull
    @Override
    public Episode getEpisode(@NotNull String episodeUniqueId, @NotNull String podcastId) {
         Episode ep = new Episode("",
                                    "",
                                        "",
                                    "","",
                                    "","",
                                        "","",0,
                                    0,0,0);
        Cursor cursor = null;
        try {
            Uri uri = Contract.EpisodeTable.URI_EPISODE_ID.buildUpon().appendPath(episodeUniqueId).build();
            cursor = this.context.getContentResolver().query(uri,
                    null,null,null,null);
            return Episode.fromCursor(cursor);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ep;
    }

    @Override
    public boolean updatePodcast(@NotNull ContentValues cv, @NotNull String podcastId) {
        int count = -1;
        try {
            Uri uri = Contract.PodcastTable.URI_ID.buildUpon().appendPath(podcastId).build();
            count = this.context.getContentResolver().update(uri,
                    cv,null,null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count == 1;
    }

    @Override
    public boolean updateEpisode(@NotNull ContentValues cv, @NotNull Episode ep) {
        int count = -1;
        try {
            Uri uri = Contract.EpisodeTable.URI_EPISODE_ID.buildUpon().appendPath(ep.getUniqueId()).build();
            count = this.context.getContentResolver().update(uri,
                    cv,null,null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count == 1;
    }

    @Override
    public boolean insertPodcast(@NotNull Podcast podcast) {
        ContentValues cv = podcast.toContentValues(context);
        Uri returnUri = null;
        try {
            Uri uri = Contract.PodcastTable.URI_ID.buildUpon().appendPath(podcast.getCollectionId()).build();
            returnUri = this.context.getContentResolver().insert(uri,
                    cv);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // need to start glide download for image also

        return returnUri != null;
    }

    @Override
    public boolean insertEpisode(@NotNull Episode ep) {
        ContentValues cv = ep.toContentValues();
        Uri returnUri = null;
        try {
            Uri uri = Contract.EpisodeTable.URI_EPISODE_ID.buildUpon().appendPath(ep.getUniqueId()).build();
            returnUri = this.context.getContentResolver().insert(uri,
                    cv);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnUri != null;
    }

    @Override
    public boolean insertEpisodes(@NotNull List<Episode> episodeList ) {
        List<ContentValues> cvs = new ArrayList<>();
        int count = 0;
        // convert all episode to cv list
        for (Episode ep: episodeList) {
            cvs.add(ep.toContentValues());
        }

        try {

            String podcastId = episodeList.get(0).getPodcastId();

            Uri uri = Contract.EpisodeTable.URI_OF_PODCAST.buildUpon().appendPath(podcastId).build();

            ContentValues[] cvas = new ContentValues[cvs.size()];

            cvs.toArray(cvas);

            count = this.context.getContentResolver().bulkInsert(uri,cvas);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return count == cvs.size();
    }

    @Override
    public Channel fetchEpisodesFromFeedUrl(String feedUrl) {
        return null;
    }

    @Override
    public void clearOldUpdates() {
            Uri uri = Contract.EpisodeTable.URI_OF_NEW_UPDATES;
            Uri updateEp = Contract.EpisodeTable.URI_EPISODE_ID;
            Cursor c = this.context.getContentResolver().query(uri,null,null,null,null);
            ContentValues cv = new ContentValues();
            // clear out the is new update field
            cv.put(Contract.EpisodeTable.IS_NEW_UPDATE, 0);
            if (c != null && c.getCount() > 0){
                c.moveToFirst();
                for (int i = 0; i < c.getCount() ; i++) {
                    c.moveToPosition(i);
                    Episode ep = Episode.fromCursor(c);
                    updateEpisode(cv,ep);
                }
            }
    }

    @Override
    public Map<Podcast, List<Episode>> getNewUpdate() {
        Map<Podcast, List<Episode>> updateList = new HashMap<>();
        List<Podcast> podcasts = getAllPodcast();
        Map<String, Podcast> podcastMap = new HashMap<>();
        // create list for each podcast
        for (Podcast p : podcasts){
            podcastMap.put(p.getCollectionId(),p);
        }
        Uri newUpdateUri = Contract.EpisodeTable.URI_OF_NEW_UPDATES;
        Cursor c  = this.context
                .getContentResolver().query(newUpdateUri,null,null,null,null);
        if (c != null && c.getCount() > 0){
            c.moveToFirst();
            for (int i = 0; i < c.getCount() ; i++) {
                c.moveToPosition(i);
                Episode ep = Episode.fromCursor(c);
                Podcast parentPod = podcastMap.get(ep.getPodcastId());
                if (! updateList.containsKey(parentPod)) {
                    updateList.put(parentPod, new ArrayList<>());
                }
                updateList.get(parentPod).add(ep);
            }
        }
        return updateList;
    }

    @Override
    public boolean deleteEpisodes(String uniqueId) {
        int count = 0;
        try {
            Uri uri = Contract.EpisodeTable.URI_EPISODE_ID.buildUpon().appendPath(uniqueId).build();
            count = this.context.getContentResolver().delete(uri, null,null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count == 1;
    }
}
