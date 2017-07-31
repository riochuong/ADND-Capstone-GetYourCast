package getyourcasts.jd.com.getyourcasts.util

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import getyourcasts.jd.com.getyourcasts.repository.local.LocalDataRepository
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream

/**
 * Created by chuondao on 7/28/17.
 */
class StorageUtil {

    companion object {
        const val TAG = "StorageUtil"
        const val PODCAST_IMG_TYPE = 0
        const val EPISODE_IMG_TYPE = 1
        const val EPISODE_MEDIA_FILE_TYPE = 2
        const val ONE_MB = 1024 * 1024
        const val NOT_AVAIL_STR = "N/A"

        val MEDIA_ROOT = "media"
        val PODCAST_IMG_ROOT = "podcast_img"
        val EPISODE_MEDIA_FILE_ROOT = "episode_media"
        val PNG_FORMAT = ".png"


        fun convertToMbRep (rawSize : String): String {
            try {
                var fileSize : Float = rawSize.toFloat()
                fileSize = fileSize / ONE_MB
                return "%.2f MB".format(fileSize)
            } catch(e: Exception) {
                e.printStackTrace()
            }
            return "NOT_AVAIL_STR"
        }


        // METHODS
        fun getPathToStorePodImg(pod: Podcast, ctx: Context): String? {
            val root = "$PODCAST_IMG_ROOT"
            // this api wil lcreate directory if needed to
            val file = ctx.getDir(root, Context.MODE_PRIVATE)
            val finalPath = File(file, pod.collectionId + PNG_FORMAT)
            if (finalPath.exists()) {
                return null
            }
            // get absolute path
            return finalPath.absolutePath
        }

        /**
         * check and give the abspath to store ep
         */
        fun getPathToStoreEp(podcastId: String, ep: Episode, ctx: Context): Pair<String,String>? {
            val root = "$MEDIA_ROOT/$podcastId"
            val file = ctx.getDir(root, Context.MODE_PRIVATE)
            val fileName = "$podcastId/${ep.title.hashCode()}"
            val finalPath = File(file, fileName)
            if (finalPath.exists()){
                return null
            }
            return Pair(file.absolutePath, fileName)
        }


        fun startGlideImageDownload(pod:Podcast,  ctx: Context){
            GlideApp.with(ctx)
                    .asBitmap()
                    .load(pod.artworkUrl100)
                    .into(getStorageTarget(pod,ctx))
        }

        fun getStorageTarget (pod:Podcast,ctx: Context):SimpleTarget<Bitmap> {

                // create bitmap target will save image
               return object: SimpleTarget<Bitmap>(){
                    override fun onResourceReady(resource: Bitmap?, transition: Transition<in Bitmap>?) {
                        val file = StorageUtil.getPathToStorePodImg(pod, ctx)
                        if (file != null && resource != null){
                            val os = FileOutputStream(file)
                            Observable.just(resource.compress(Bitmap.CompressFormat.PNG, 100, os))
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(
                                            {
                                                if (it) {Log.d(TAG,"Successfully download limage ${pod
                                                        .artworkUrl100}")}
                                                // clean up
                                                os.close()
                                            },
                                            {
                                                it.printStackTrace()
                                                Log.e(TAG,"Failed to download Image ${pod
                                                        .artworkUrl100}")
                                                //clean up
                                                os.close()
                                            }
                                    )
                        }
                    }
                }

        }

    }


}