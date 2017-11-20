package getyourcasts.jd.com.getyourcasts.util

import android.content.Context
import android.graphics.Bitmap
import android.util.Pair
import android.util.Log

import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import java.util.ArrayList

import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.*


/**
 * Created by chuondao on 8/9/17.
 */

object StorageUtil {
    private val TAG = "StorageUtil"
    private val ONE_MB = 1024 * 1024
    private val MEDIA_ROOT = "media"
    private val PODCAST_IMG_ROOT = "podcast_img"
    private val PLAYLIST_ROOT = "playlist"
    private val PLAYLIST_FILE_NAME = "currPlaylist"
    private val PNG_FORMAT = ".png"

    fun convertToMbRep(rawSize: String): String {
        try {
            var fileSize: Float? = java.lang.Float.parseFloat(rawSize)
            fileSize = fileSize!! / ONE_MB
            return String.format("%.2f MB", fileSize)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return "NOT_AVAIL_STR"
    }

    // METHODS
    fun getPathToStorePodImg(pod: Podcast, ctx: Context): String? {
        val root = PODCAST_IMG_ROOT
        // this api wil lcreate directory if needed to
        val file = ctx.getDir(root, Context.MODE_PRIVATE)
        val finalPath = File(file, pod.collectionId + PNG_FORMAT)
        // remove old duplicate
        if (finalPath.exists()) { finalPath.delete(); }
        return finalPath.absolutePath
    }

    /**
     * check and give the abspath to store ep
     */
    fun getPathToStoreEp(ep: Episode, ctx: Context): Pair<String, String> {
        val file = ctx.getDir(MEDIA_ROOT, Context.MODE_PRIVATE)
        val fileName = ep.episodeUniqueKey
        return Pair(file.absolutePath, fileName)
    }

    fun cleanUpOldFile(ep: Episode, ctx: Context): Boolean {
        var res = false
        try {
            val file = ctx.getDir(MEDIA_ROOT, Context.MODE_PRIVATE)
            val fileName = ep.episodeUniqueKey
            val finalPath = File(file, fileName)
            // clean up file to prepare for new download
            if (file.exists()) res = finalPath.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return res
    }


    fun startGlideImageDownload(pod: Podcast, ctx: Context): Boolean {
        val downloadFile = GlideApp.with(ctx).downloadOnly().load(pod.artworkUrl100).submit().get()
        if (downloadFile != null) {
            // write file to storage
            var source: FileInputStream? = null
            var dest: FileOutputStream? = null
            try {
                val imgFile = File(StorageUtil.getPathToStorePodImg(pod, ctx))
                source = FileInputStream(downloadFile)
                dest = FileOutputStream(imgFile)
                dest.channel.transferFrom(source.channel, 0, source.channel.size())
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d(TAG, "Failed to transfer cache image after download")
            } finally {
                if (source != null) source.close()
                if (dest != null) dest.close()
            }
        }
        return false
    }

    fun loadMediaPlayList(context: Context): List<Episode> {
        val playListFile = File(context.getDir(PLAYLIST_ROOT, Context.MODE_PRIVATE), PLAYLIST_FILE_NAME)
        val sb = StringBuilder()
        if (!playListFile.exists()) {
            return ArrayList()
        }
        // read data back in
        try {
            val bf = BufferedReader(FileReader(playListFile))
            var newLine: String? = bf.readLine()
            while (newLine != null) {
                sb.append(newLine)
                newLine = bf.readLine()
            }
            val episodeListType = object : TypeToken<ArrayList<Episode>>() {

            }.type
            val gson = Gson()
            return gson.fromJson(sb.toString(), episodeListType)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return ArrayList()
    }

    fun saveMediaPlayList(context: Context, playList: List<Episode>): Boolean {
        var fw: FileWriter? = null
        try {
            val playListFile = context.getDir(PLAYLIST_ROOT, Context.MODE_PRIVATE)
            fw = FileWriter(File(playListFile, PLAYLIST_FILE_NAME))
            val gson = Gson()
            gson.toJson(playList, fw)
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } finally {
            if (fw != null) {
                try {
                    fw.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                fw = null
            }
        }
    }

}




