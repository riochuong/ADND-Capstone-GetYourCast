package getyourcasts.jd.com.getyourcasts.util

import android.content.Context
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast
import java.io.File

/**
 * Created by chuondao on 7/28/17.
 */
class StorageUtil {

    companion object {

        const val PODCAST_IMG_TYPE = 0
        const val EPISODE_IMG_TYPE = 1
        const val EPISODE_MEDIA_FILE_TYPE = 2


        val MEDIA_ROOT = "media"
        val PODCAST_IMG_ROOT = "podcast_img"
        val EPISODE_MEDIA_FILE_ROOT = "episode_media"
        val PNG_FORMAT = ".png"



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

    }


}