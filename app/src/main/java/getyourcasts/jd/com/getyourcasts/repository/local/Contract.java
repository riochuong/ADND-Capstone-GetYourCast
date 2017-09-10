package getyourcasts.jd.com.getyourcasts.repository.local;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by chuondao on 8/8/17.
 */

public final class Contract {

    static final String AUTHORITY = "com.jd.udacity.getyourcast";
    static final String PATH_PODCAST = "podcast";
    static final String PATH_PODCAST_ID = "podcast/*";
    static final String PATH_EPISODES = "episode";
    static final String AUTHOR_PATH_ALL_EPISODES_OF_POCAST = "episode/podcast/*";
    static final String AUTHOR_PATH_ALL_EPISODES_DOWNLOADED = "episode/downloaded";
    static final String AUTHOR_PATH_EPISODES_ID = "episode/id/*";
    static final String PATH_EPISODES_NEW_UPDATE = "newupdate";
    static final String PATH_EPISODES_DOWNLOADED = "downloaded";
    static final String AUTHOR_EPISODES_NEW_UPDATE = "episode/newupdate";
    private static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);
    private static final String ID_EP_PATH = "id";

    private Contract () {

    }

    public static final class PodcastTable implements BaseColumns {
        public static final Uri URI = BASE_URI.buildUpon().appendPath(PATH_PODCAST).build();
        public static final Uri URI_ID = BASE_URI.buildUpon().appendPath(PATH_PODCAST).build();
        public static final String  ID = "_id";
        public static final String PODCAST_NAME="pod_cast_name";
        public static final String ARTIST_NAME="artist_name";
        public static final String LAST_UPDATE = "last_update";
        public static final String UNIQUE_ID = "unique_id"; // use it to link with episode table
        public static final String IMG_ONLINE_PATH = "img_online_path";
        public static final String IMG_LOCAL_PATH = "img_local_path";
        public static final String FEED_URL = "feed_url"; // url used for fetching update episodes
        public static final String TRACK_COUNT = "track_count";
        public static final String RELEASE_DATE = "release_date";
        public static final String DESCRIPTION = "desc";
        public static final String VIBRANT_COLOR = "vib_color";
        public  static final String TABLE_NAME="PodcastTable";
        public static final String getPodcastIdFromUri(Uri uri) {
            return uri.getLastPathSegment();
        }
    }


    public static final class EpisodeTable implements BaseColumns {
        static final Uri URI_EPISODE_ID = BASE_URI.buildUpon().appendPath(PATH_EPISODES).appendPath(ID_EP_PATH)
                .build();
        static final Uri URI_OF_PODCAST = BASE_URI.buildUpon().appendPath(PATH_EPISODES).appendPath
                (PATH_PODCAST).build();
        static final Uri URI_OF_NEW_UPDATES = BASE_URI.buildUpon().appendPath(PATH_EPISODES).appendPath
                (PATH_EPISODES_NEW_UPDATE).build();
        static final Uri URI_OF_DOWNLOADED = BASE_URI.buildUpon().appendPath(PATH_EPISODES).appendPath
                (PATH_EPISODES_DOWNLOADED).build();
        static final String  ID = "_id";
        public static final String PODCAST_ID = "podcast_id";
        public static final String UNIQUE_ID = "unique_id";
        public static final String EPISODE_NAME = "episode_name";
        public static final String DATE_RELEASED = "date_released";
        public static final String DATE_DOWNLOADED = "date_downloaded";
        public static final String DOWNLOADED = "downloaded";
        public static final String PROGRESS = "consume_percentage";
        public static final String DESCRIPTION = "description";
        public static final String LOCAL_URL = "local_url";
        public static final String FETCH_URL = "fetch_url";
        public static final String FILE_SIZE = "file_size";
        public static final String FAVORITE = "favor";
        public static final String IS_NEW_UPDATE = "is_new_update";
        public static final String MEDIA_TYPE = "media_type";
        public  static final String TABLE_NAME="EpisodeTable";

        public static final String getEpisodeIdFromUri(Uri uri) {
            return uri.getLastPathSegment();
        }
    }
}
