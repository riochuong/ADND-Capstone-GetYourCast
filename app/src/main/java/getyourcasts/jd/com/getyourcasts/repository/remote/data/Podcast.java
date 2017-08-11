package getyourcasts.jd.com.getyourcasts.repository.remote.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import getyourcasts.jd.com.getyourcasts.repository.local.Contract;
import getyourcasts.jd.com.getyourcasts.repository.local.CursorHelper;
import getyourcasts.jd.com.getyourcasts.util.StorageUtil;
import getyourcasts.jd.com.getyourcasts.util.TimeUtil;

/**
 * Created by chuondao on 8/9/17.
 */

public final class Podcast implements Parcelable {

    private String collectionId;
    private String collectionName;
    private String artistName;
    private String imgLocalPath;
    private String artworkUrl100;
    private String releaseDate;
    private String lastUpdate;
    private String feedUrl;
    private Long trackCount;
    private String description;

    public Podcast(String collectionId,
                   String collectionName,
                   String artistName,
                   String imgLocalPath,
                   String artworkUrl100,
                   String releaseDate,
                   String lastUpdate,
                   String feedUrl,
                   Long trackCount,
                   String description) {
        this.collectionId = collectionId;
        this.collectionName = collectionName;
        this.artistName = artistName;
        this.imgLocalPath = imgLocalPath;
        this.artworkUrl100 = artworkUrl100;
        this.releaseDate = releaseDate;
        this.lastUpdate = lastUpdate;
        this.feedUrl = feedUrl;
        this.trackCount = trackCount;
        this.description = description;
    }


    public String getCollectionId() {
        return collectionId;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getImgLocalPath() {
        return imgLocalPath;
    }

    public String getArtworkUrl100() {
        return artworkUrl100;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    public Long getTrackCount() {
        return trackCount;
    }

    public String getDescription() {
        return description;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public void setImgLocalPath(String imgLocalPath) {
        this.imgLocalPath = imgLocalPath;
    }

    public void setArtworkUrl100(String artworkUrl100) {
        this.artworkUrl100 = artworkUrl100;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
    }

    public void setTrackCount(Long trackCount) {
        this.trackCount = trackCount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.collectionId);
        dest.writeString(this.collectionName);
        dest.writeString(this.artistName);
        dest.writeString(this.imgLocalPath);
        dest.writeString(this.artworkUrl100);
        dest.writeString(this.releaseDate);
        dest.writeString(this.lastUpdate);
        dest.writeString(this.feedUrl);
        dest.writeValue(this.trackCount);
        dest.writeString(this.description);
    }



    protected Podcast(Parcel in) {
        this.collectionId = in.readString();
        this.collectionName = in.readString();
        this.artistName = in.readString();
        this.imgLocalPath = in.readString();
        this.artworkUrl100 = in.readString();
        this.releaseDate = in.readString();
        this.lastUpdate = in.readString();
        this.feedUrl = in.readString();
        this.trackCount = (Long) in.readValue(Long.class.getClassLoader());
        this.description = in.readString();
    }

    public static final Parcelable.Creator<Podcast> CREATOR = new Parcelable.Creator<Podcast>() {
        @Override
        public Podcast createFromParcel(Parcel source) {
            return new Podcast(source);
        }

        @Override
        public Podcast[] newArray(int size) {
            return new Podcast[size];
        }
    };

    public static Podcast fromCursor(Cursor cursor) {
        if (cursor.getCount() > 0) {


            return new Podcast(
                  CursorHelper.getStringValue(cursor, Contract.PodcastTable.UNIQUE_ID),
                    CursorHelper.getStringValue(cursor, Contract.PodcastTable.PODCAST_NAME),
                    CursorHelper.getStringValue(cursor, Contract.PodcastTable.ARTIST_NAME),
                    CursorHelper.getStringValue(cursor, Contract.PodcastTable.IMG_LOCAL_PATH),
                    CursorHelper.getStringValue(cursor, Contract.PodcastTable.IMG_ONLINE_PATH),
                    CursorHelper.getStringValue(cursor, Contract.PodcastTable.RELEASE_DATE),
                    CursorHelper.getStringValue(cursor, Contract.PodcastTable.LAST_UPDATE),
                    CursorHelper.getStringValue(cursor, Contract.PodcastTable.FEED_URL),
                    (long)CursorHelper.getIntValue(cursor, Contract.PodcastTable.TRACK_COUNT),
                    CursorHelper.getStringValue(cursor, Contract.PodcastTable.DESCRIPTION)
            );
        }
        return null;
    }

    public ContentValues toContentValues (Context ctx) {
        ContentValues cv = new ContentValues();
        cv.put(Contract.PodcastTable.UNIQUE_ID, this.getCollectionId());
        cv.put(Contract.PodcastTable.PODCAST_NAME, this.getCollectionName());
        cv.put(Contract.PodcastTable.FEED_URL, this.getFeedUrl());
        cv.put(Contract.PodcastTable.RELEASE_DATE, this.getReleaseDate());
                // only inject this for episode that get inserted to db
        cv.put(Contract.PodcastTable.IMG_LOCAL_PATH, StorageUtil.getPathToStorePodImg(this, ctx));
        cv.put(Contract.PodcastTable.IMG_ONLINE_PATH, this.getArtworkUrl100());
        cv.put(Contract.PodcastTable.ARTIST_NAME, this.getArtistName());
        cv.put(Contract.PodcastTable.TRACK_COUNT, this.getTrackCount());
        cv.put(Contract.PodcastTable.LAST_UPDATE, TimeUtil.getCurrentTimeInMs());
        return cv;
    }

    public static Podcast getEmptyPodcast() {
        return new Podcast(
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                0L,
                ""
        );
    }
}
