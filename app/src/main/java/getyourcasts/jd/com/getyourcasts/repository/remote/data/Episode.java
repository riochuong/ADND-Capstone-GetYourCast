package getyourcasts.jd.com.getyourcasts.repository.remote.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import getyourcasts.jd.com.getyourcasts.repository.local.Contract;
import getyourcasts.jd.com.getyourcasts.repository.local.CursorHelper;
import getyourcasts.jd.com.getyourcasts.util.TimeUtil;

/**
 * Created by chuondao on 8/9/17.
 */

public final class Episode implements Parcelable, Comparable<Episode> {
    private String podcastId;
    private String title;
    private String uniqueId;
    private String pubDate;
    private String description;
    private String downloadUrl;
    private String localUrl;
    private String fileSize;
    private String type;
    private int favorite;
    private int progress;
    private int downloaded;
    private int isNewUpdate;

    public Episode(String podcastId,
                   String title,
                   String uniqueId,
                   String pubDate,
                   String description,
                   String downloadUrl,
                   String localUrl,
                   String fileSize,
                   String type,
                   int favorite,
                   int progress,
                   int downloaded,
                   int isNewUpdate) {
        this.podcastId = podcastId;
        this.title = title;
        this.uniqueId = uniqueId;
        this.pubDate = pubDate;
        this.description = description;
        this.downloadUrl = downloadUrl;
        this.localUrl = localUrl;
        this.fileSize = fileSize;
        this.type = type;
        this.favorite = favorite;
        this.progress = progress;
        this.downloaded = downloaded;
        this.isNewUpdate = isNewUpdate;
    }

    public String getPodcastId() {
        return podcastId;
    }

    public String getTitle() {
        return title;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public String getPubDate() {
        return pubDate;
    }

    public String getDescription() {
        return description;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getLocalUrl() {
        return localUrl;
    }

    public String getFileSize() {
        return fileSize;
    }

    public String getType() {
        return type;
    }

    public int getFavorite() {
        return favorite;
    }

    public int getProgress() {
        return progress;
    }

    public int getDownloaded() {
        return downloaded;
    }

    public void setPodcastId(String podcastId) {
        this.podcastId = podcastId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public void setLocalUrl(String localUrl) {
        this.localUrl = localUrl;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void setDownloaded(int downloaded) {
        this.downloaded = downloaded;
    }

    public void setIsNewUpdate(int isNewUpdate) {
        this.isNewUpdate = isNewUpdate;
    }


    public int getIsNewUpdate() {
        return isNewUpdate;
    }



    public String getEpisodeUniqueKey() {
        return (this.podcastId + "_" + this.title).hashCode() + "";
    }

    /**
     * Construct episode from cursor
     *
     * @param cursor
     * @return
     */
    public static Episode fromCursor(Cursor cursor) {
        if (cursor.getCount() > 0) {

            String podcastId = CursorHelper.getStringValue(cursor, Contract.EpisodeTable.PODCAST_ID);
            String epName = CursorHelper.getStringValue(cursor, Contract.EpisodeTable.EPISODE_NAME);
            String uniqueId = (podcastId + "_" + epName).hashCode() + "";


            return new Episode(
                    CursorHelper.getStringValue(cursor, Contract.EpisodeTable.PODCAST_ID),
                    CursorHelper.getStringValue(cursor, Contract.EpisodeTable.EPISODE_NAME),
                    uniqueId,
                    CursorHelper.getStringValue(cursor, Contract.EpisodeTable.DATE_RELEASED),
                    CursorHelper.getStringValue(cursor, Contract.EpisodeTable.DESCRIPTION),
                    CursorHelper.getStringValue(cursor, Contract.EpisodeTable.FETCH_URL),
                    CursorHelper.getStringValue(cursor, Contract.EpisodeTable.LOCAL_URL),
                    CursorHelper.getStringValue(cursor, Contract.EpisodeTable.FILE_SIZE),
                    CursorHelper.getStringValue(cursor, Contract.EpisodeTable.MEDIA_TYPE),
                    CursorHelper.getIntValue(cursor, Contract.EpisodeTable.FAVORITE),
                    CursorHelper.getIntValue(cursor, Contract.EpisodeTable.PROGRESS),
                    CursorHelper.getIntValue(cursor, Contract.EpisodeTable.DOWNLOAD_STATUS),
                    CursorHelper.getIntValue(cursor, Contract.EpisodeTable.IS_NEW_UPDATE)
            );
        }
        return null;
    }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(Contract.EpisodeTable.EPISODE_NAME, this.getTitle());
        cv.put(Contract.EpisodeTable.PODCAST_ID, this.getPodcastId());
        cv.put(Contract.EpisodeTable.FILE_SIZE, this.getFileSize());
        cv.put(Contract.EpisodeTable.UNIQUE_ID, this.getUniqueId());
        cv.put(Contract.EpisodeTable.DATE_RELEASED, this.getPubDate());
        cv.put(Contract.EpisodeTable.MEDIA_TYPE, this.getType());
        cv.put(Contract.EpisodeTable.DESCRIPTION, this.getDescription());
        cv.put(Contract.EpisodeTable.FETCH_URL, this.getDownloadUrl());
        cv.put(Contract.EpisodeTable.IS_NEW_UPDATE, this.getIsNewUpdate());
        return cv;
    }

    public static Episode fromFeedItem(FeedItem feedItem, String podcastId) {
        return new Episode(
                podcastId,
                feedItem.getTitle().trim(),
                (podcastId.trim()+"_"+feedItem.getTitle().trim()).hashCode()+"",
                feedItem.getPubDate(),
                feedItem.getDescription(),
                feedItem.getMediaInfo().getUrl(),
                null, // no local url available
                feedItem.getMediaInfo().getSize(),
                feedItem.getMediaInfo().getType(),
                0, // initilize favorite to NOT
                0,
                0,
                0
            );
    }


    public Episode (Episode ep) {
        this.podcastId =  ep.getPodcastId();
        this.title = ep.getTitle();
        this.uniqueId = ep.getUniqueId();
        this.pubDate = ep.getPubDate();
        this.description = ep.getDescription();
        this.downloadUrl = ep.getDownloadUrl();
        this.localUrl = ep.getLocalUrl();
        this.fileSize = ep.getFileSize();
        this.type = ep.getType();
        this.favorite = ep.getFavorite();
        this.progress = ep.getProgress();
        this.downloaded = ep.getDownloaded();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.podcastId);
        dest.writeString(this.title);
        dest.writeString(this.uniqueId);
        dest.writeString(this.pubDate);
        dest.writeString(this.description);
        dest.writeString(this.downloadUrl);
        dest.writeString(this.localUrl);
        dest.writeString(this.fileSize);
        dest.writeString(this.type);
        dest.writeInt(this.favorite);
        dest.writeInt(this.progress);
        dest.writeInt(this.downloaded);
        dest.writeInt(this.isNewUpdate);
    }

    protected Episode(Parcel in) {
        this.podcastId = in.readString();
        this.title = in.readString();
        this.uniqueId = in.readString();
        this.pubDate = in.readString();
        this.description = in.readString();
        this.downloadUrl = in.readString();
        this.localUrl = in.readString();
        this.fileSize = in.readString();
        this.type = in.readString();
        this.favorite = in.readInt();
        this.progress = in.readInt();
        this.downloaded = in.readInt();
        this.isNewUpdate = in.readInt();
    }

    public static final Creator<Episode> CREATOR = new Creator<Episode>() {
        @Override
        public Episode createFromParcel(Parcel source) {
            return new Episode(source);
        }

        @Override
        public Episode[] newArray(int size) {
            return new Episode[size];
        }
    };

    @Override
    public int compareTo(@NonNull Episode o) {

        try {
            SimpleDateFormat epDateFormat = TimeUtil.getEpisodeSimpleDateFormat();
            return (-1) * epDateFormat.parse(this.pubDate).compareTo(epDateFormat.parse(o.getPubDate()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // if some format is bad then put them together dont have to sort
        return 0;

    }
}
