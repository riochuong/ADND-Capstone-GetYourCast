package getyourcasts.jd.com.getyourcasts.repository.local


import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import getyourcasts.jd.com.getyourcasts.repository.DataRepository
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Channel
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import getyourcasts.jd.com.getyourcasts.util.StorageUtil
import getyourcasts.jd.com.getyourcasts.util.TimeUtil
import org.jetbrains.anko.db.insertOrThrow
import org.jetbrains.anko.db.select


/**
 * Created by chuondao on 7/22/17.
 */

class LocalDataRepository(val ctx: Context): DataRepository {

    companion object {
        val TAG = "LocalDataRepo"
        val EPISODE_UPDATE_SELECT = "(${EpisodeTable.EPISODE_NAME}=?) and (${EpisodeTable.PODCAST_ID}=?)"
        val PODCAST_UPDATE_SELECT = "${PodcastsTable.UNIQUE_ID}=?"

    }

    override fun insertPodcastToDb(pod: Podcast): Boolean {
            // check if Podcast is already inserted before
            val isInDb = ctx.database.use{
                select(PodcastsTable.NAME).whereArgs(PodcastsTable.UNIQUE_ID+" = ${pod.collectionId}").exec {
                    this.count > 0
                }
            }

                // Only insert if not part of DB yet
            if (! isInDb){
                val res = ctx.database.use {

                    insertOrThrow(
                            PodcastsTable.NAME,
                            PodcastsTable.UNIQUE_ID to pod.collectionId,
                            PodcastsTable.PODCAST_NAME to pod.collectionName,
                            PodcastsTable.FEED_URL to pod.feedUrl,
                            PodcastsTable.RELEASE_DATE to pod.releaseDate,
                            // only inject this for episode that get inserted to db
                            PodcastsTable.IMG_LOCAL_PATH to StorageUtil.getPathToStorePodImg(pod, ctx),
                            PodcastsTable.IMG_ONLINE_PATH to pod.artworkUrl100,
                            PodcastsTable.ARTIST_NAME to pod.artistName,
                            PodcastsTable.TRACK_COUNT to pod.trackCount,
                            PodcastsTable.LAST_UPDATE to TimeUtil.getCurrentTimeInMs()
                    )
                }
                return res > 0
            }
        return false
    }

    override fun insertEpisode(episode: Episode): Boolean {
        // check if Podcast is already inserted before
        val isInDb = ctx.database.use{
            select(EpisodeTable.NAME).whereArgs("("+EpisodeTable.PODCAST_ID+" = ${episode
                    .podcastId} ) " +
                    "and ("
                    +EpisodeTable.EPISODE_NAME+" = ${episode.title}").exec {
                this.count > 0
            }
        }

        // Only insert if not part of DB yet
        if (! isInDb){
            val res = ctx.database.use {

                insertOrThrow(
                        EpisodeTable.NAME,
                        EpisodeTable.EPISODE_NAME to episode.title,
                        EpisodeTable.PODCAST_ID to episode.podcastId,
                        EpisodeTable.DOWNLOADED to 0,
                        EpisodeTable.FILE_SIZE to episode.fileSize,
                        EpisodeTable.DATE_RELEASED to episode.pubDate,
                        EpisodeTable.MEDIA_TYPE to episode.type,
                        EpisodeTable.DESCRIPTION to episode.description,
                        EpisodeTable.FETCH_URL to episode.downloadUrl
                )
            }
            return res > 0
        }
        return false
    }



    override fun downloadFeed(feedUrl: String): Channel?{
        // NOOP
        return null
    }


    override fun searchPodcast(title: String): List<Podcast> {
        // NOOP
        return ArrayList<Podcast>()
    }

    override fun getPodcast(podcastId: String): Podcast? {
        var podcast :Podcast? = ctx.database.use {
            // this is a sqlite db instance
            val query = PodcastsTable.UNIQUE_ID+" = $podcastId"
            select(PodcastsTable.NAME).whereArgs(query).exec {
                if (this.count > 0){
                    this.moveToFirst()
                    Podcast.fromCursor(this)
                }
                else {
                    null
                }
            }
        }

        return podcast
    }

    override fun getAllEpisodesOfPodcast(podcastId: String): List<Episode> {
        val cursor =  ctx.database.use {
            select(EpisodeTable.NAME).whereArgs(EpisodeTable.PODCAST_ID+" = $podcastId").exec {this}
        }
        return convertToEpisodeList(cursor)
    }

    override fun getEpisode(episodeName: String, podcastID: String): List<Episode> {
        val cursor = ctx.database.use {
            select(EpisodeTable.NAME).whereArgs("("+EpisodeTable.PODCAST_ID+" = $podcastID ) and ("
                +EpisodeTable.EPISODE_NAME+" = $episodeName").exec {this}
        }
        return convertToEpisodeList(cursor)
    }

    override fun updatePodcast(cv: ContentValues, podcastId: String): Boolean {

        val res = ctx.database.use {
            val arrArgs = arrayOf(podcastId)
            this.update(PodcastsTable.NAME, cv, PODCAST_UPDATE_SELECT,arrArgs)
        }

        return res == 1
    }

    override fun updateEpisode(cv: ContentValues, episode: Episode): Boolean {
       val res = ctx.database.use {
           val arrArgs = arrayOf(episode.title, episode.podcastId)
           this.update(EpisodeTable.NAME,cv, EPISODE_UPDATE_SELECT, arrArgs)
       }
       return res == 1
    }


    fun convertToEpisodeList (cursor:Cursor): List<Episode>{
        return ArrayList<Episode>()
    }

    fun convertToPodcastList (cursor:Cursor): List<Podcast>{
        return ArrayList<Podcast>()
    }


}
