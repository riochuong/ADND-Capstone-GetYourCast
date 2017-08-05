package getyourcasts.jd.com.getyourcasts.repository.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*

/**
 * Created by chuondao on 7/21/17.
 */
class PodCastSqliteHelper (ctx: Context): ManagedSQLiteOpenHelper(ctx, PodCastSqliteHelper.DB_NAME, null,
        PodCastSqliteHelper.VERSION ) {


    companion object {
        val DB_NAME = "PODCAST_DB"
        val VERSION = 1
        private var instance: PodCastSqliteHelper? = null

        @Synchronized
        fun getInstance(ctx: Context): PodCastSqliteHelper {
            if (instance == null) {
                instance = PodCastSqliteHelper(ctx.getApplicationContext())
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {

        if (db != null){
            // create PODCAST TABLE
            db.createTable(
                    PodcastsTable.NAME, true,
                    PodcastsTable.ID to INTEGER + PRIMARY_KEY,
                    PodcastsTable.UNIQUE_ID to TEXT + UNIQUE,
                    PodcastsTable.PODCAST_NAME to TEXT,
                    PodcastsTable.LAST_UPDATE to TEXT,
                    PodcastsTable.IMG_ONLINE_PATH to TEXT,
                    PodcastsTable.FEED_URL to TEXT ,
                    PodcastsTable.TRACK_COUNT to INTEGER,
                    PodcastsTable.ARTIST_NAME to TEXT,
                    PodcastsTable.IMG_LOCAL_PATH to TEXT,
                    PodcastsTable.RELEASE_DATE to TEXT,
                    PodcastsTable.DESCRIPTION to TEXT
            )

            // create EPISODE TABLE
            db.createTable(
                    EpisodeTable.NAME, true,
                    EpisodeTable.ID to INTEGER + PRIMARY_KEY,
                    EpisodeTable.EPISODE_NAME to TEXT,
                    EpisodeTable.PODCAST_ID to TEXT,
                    EpisodeTable.UNIQUE_ID to TEXT,
                    EpisodeTable.DATE_DOWNLOADED  to TEXT,
                    EpisodeTable.DATE_RELEASED to TEXT,
                    EpisodeTable.PROGRESS to INTEGER,
                    EpisodeTable.FILE_SIZE to TEXT,
                    EpisodeTable.FETCH_URL to TEXT,
                    EpisodeTable.LOCAL_URL to TEXT,
                    EpisodeTable.MEDIA_TYPE to TEXT,
                    EpisodeTable.DESCRIPTION to TEXT,
                    EpisodeTable.FAVORITE to INTEGER,
                    EpisodeTable.DOWNLOADED to INTEGER
            )

        }

        // create EPISODE TABLE
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (db != null){
            db.dropTable(PodcastsTable.NAME, true)
            db.dropTable(EpisodeTable.NAME, true)
            onCreate(db)
        }
    }

}

val Context.database: PodCastSqliteHelper
    get() = PodCastSqliteHelper.getInstance(applicationContext)
