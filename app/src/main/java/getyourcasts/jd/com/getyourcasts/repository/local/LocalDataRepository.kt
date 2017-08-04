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
import org.jetbrains.anko.runOnUiThread


/**
 * Created by chuondao on 7/22/17.
 */

class LocalDataRepository(val ctx: Context): DataRepository {

    /**
     * insert episodes in bulk
     */
    override fun insertEpisodes(episodes: List<Episode>): Boolean{
        try {
            var res = true
            ctx.database.use {
               episodes.forEach {
                  res = res && insertOrThrow(
                           EpisodeTable.NAME,
                           EpisodeTable.EPISODE_NAME to it.title,
                           EpisodeTable.PODCAST_ID to it.podcastId,
                           EpisodeTable.UNIQUE_ID to it.uniqueId,
                           EpisodeTable.DOWNLOADED to 0,
                           EpisodeTable.FILE_SIZE to it.fileSize,
                           EpisodeTable.DATE_RELEASED to it.pubDate,
                           EpisodeTable.MEDIA_TYPE to it.type,
                           EpisodeTable.DESCRIPTION to it.description,
                           EpisodeTable.FETCH_URL to it.downloadUrl,
                           EpisodeTable.FAVORITE to 0
                   ) > 0
               }
           }
            return res
        } catch(e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    companion object {
        val TAG = "LocalDataRepo"
        val EPISODE_UPDATE_SELECT = "(${EpisodeTable.UNIQUE_ID}=?) and (${EpisodeTable.PODCAST_ID}=?)"
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
                // start image download first
                ctx.runOnUiThread {
                    try {
                        StorageUtil.startGlideImageDownload(pod, ctx)
                    } catch(e: Exception) {
                        e.printStackTrace()
                    }
                }
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
        // download image to local storage also

        return false
    }

    override fun insertEpisode(episode: Episode): Boolean {
        // check if Podcast is already inserted before
        val isInDb = ctx.database.use{
            select(EpisodeTable.NAME).whereArgs("("+EpisodeTable.PODCAST_ID+" = ${episode
                    .podcastId} ) " +
                    "and ("
                    +EpisodeTable.UNIQUE_ID+" = ${episode.uniqueId}").exec {
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
                        EpisodeTable.DOWNLOADED to episode.downloaded,
                        EpisodeTable.FILE_SIZE to episode.fileSize,
                        EpisodeTable.UNIQUE_ID to episode.uniqueId,
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
        var list : List<Episode> = ArrayList<Episode>()
        ctx.database.use {
            select(EpisodeTable.NAME).whereArgs(EpisodeTable.PODCAST_ID+" = $podcastId").exec {
                list = convertToEpisodeList(this)
            }
        }
        return list
    }

    override fun getEpisode(episodeUniqueId: String, podcastID: String): Episode {
        var ep : Episode = Episode("","","","","","","","","",0,0,0)
        ctx.database.use {
             select(EpisodeTable.NAME).whereArgs("("+EpisodeTable.PODCAST_ID+" = $podcastID) and ("
                +EpisodeTable.UNIQUE_ID+" = \"$episodeUniqueId\")").exec {
                 if (this.count > 0){
                     this.moveToFirst()
                     ep = Episode.fromCursor(this)!!
                 }

            }
        }
        return ep
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
           val arrArgs = arrayOf(episode.uniqueId, episode.podcastId)
           this.update(EpisodeTable.NAME,cv, EPISODE_UPDATE_SELECT, arrArgs)
       }

       return res == 1
    }

    /**
     *  Convert to episode list from cursor
     */
    private fun convertToEpisodeList (cursor:Cursor): List<Episode>{
        val list = ArrayList<Episode>()
        cursor.moveToFirst()
        for (i in 0..(cursor.count - 1)) {
            cursor.moveToPosition(i)
            list.add(Episode.fromCursor(cursor)!!)
        }
        return list
    }

    private fun convertToPodcastList (cursor:Cursor): List<Podcast>{
        return ArrayList<Podcast>()
    }


}
