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
                    PodcastsTable.IMG_PATH to TEXT,
                    PodcastsTable.URL_FETCH to TEXT
            )

            // create EPISODE TABLE
            db.createTable(
                    EpisodeTable.NAME, true,
                    EpisodeTable.ID to INTEGER + PRIMARY_KEY,
                    EpisodeTable.EPISODE_NAME to TEXT,
                    EpisodeTable.POD_UNIQUE_ID to TEXT + UNIQUE,
                    EpisodeTable.DATE_DOWNLOADED  to TEXT,
                    EpisodeTable.DATE_RELEASED to TEXT,
                    EpisodeTable.CONSUME_PERC to INTEGER
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
