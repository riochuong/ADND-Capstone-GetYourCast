package getyourcasts.jd.com.getyourcasts.repository.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.jetbrains.anko.db.ManagedSQLiteOpenHelper;

/**
 * Created by chuondao on 8/8/17.
 */

public class SqliteHelper extends SQLiteOpenHelper {


    static final String DB_NAME = "PODCAST_DB";
    static final int VERSION = 1;
    private static SqliteHelper instance = null;

    public SqliteHelper(Context ctx, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(ctx, name, factory, version);
    }

    public static synchronized SqliteHelper getInstance(Context ctx) {
        if (instance == null) {
            instance = new SqliteHelper(ctx, DB_NAME, null, VERSION);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create two tables
        db.execSQL(getCreateEpisodeTableCmd());
        db.execSQL(getCreatePodcastTableCmd());
    }


    private String getCreatePodcastTableCmd() {

        return "CREATE TABLE " + Contract.PodcastTable.TABLE_NAME + " ("
                +Contract.PodcastTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                +Contract.PodcastTable.PODCAST_NAME + " TEXT, "
                +Contract.PodcastTable.LAST_UPDATE + " TEXT, "
                +Contract.PodcastTable.LAST_UPDATE + " TEXT, "
                +Contract.PodcastTable.IMG_ONLINE_PATH + " TEXT, "
                +Contract.PodcastTable.FEED_URL + " TEXT, "
                +Contract.PodcastTable.TRACK_COUNT + " INTEGER, "
                +Contract.PodcastTable.ARTIST_NAME + " TEXT, "
                +Contract.PodcastTable.IMG_LOCAL_PATH + " TEXT, "
                +Contract.PodcastTable.RELEASE_DATE + " TEXT, "
                +Contract.PodcastTable.DESCRIPTION + " TEXT, "
                +Contract.PodcastTable.UNIQUE_ID + " TEXT UNIQUE, ";

    }

    private String getCreateEpisodeTableCmd() {

        return "CREATE TABLE " + Contract.EpisodeTable.TABLE_NAME + " ("
                +Contract.EpisodeTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                +Contract.EpisodeTable.EPISODE_NAME + " TEXT, "
                +Contract.EpisodeTable.PODCAST_ID + " TEXT, "
                +Contract.EpisodeTable.UNIQUE_ID + " TEXT UNIQUE, "
                +Contract.EpisodeTable.DATE_DOWNLOADED + " TEXT, "
                +Contract.EpisodeTable.DATE_RELEASED + " TEXT, "
                +Contract.EpisodeTable.PROGRESS + " TEXT, "
                +Contract.EpisodeTable.FILE_SIZE + " TEXT, "
                +Contract.EpisodeTable.LOCAL_URL + " TEXT, "
                +Contract.EpisodeTable.MEDIA_TYPE + " TEXT, "
                +Contract.EpisodeTable.DESCRIPTION + " TEXT, "
                +Contract.EpisodeTable.FAVORITE + " TEXT, "
                +Contract.EpisodeTable.FETCH_URL + " TEXT, "
                +Contract.EpisodeTable.DOWNLOADED + " TEXT, ";
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(" DROP TABLE IF EXISTS " + Contract.EpisodeTable.TABLE_NAME);
        db.execSQL(" DROP TABLE IF EXISTS " + Contract.PodcastTable.TABLE_NAME);
        onCreate(db);
    }
}


