package getyourcasts.jd.com.getyourcasts.util

import android.content.Context
import android.view.View
import android.widget.ImageView
import com.github.florent37.glidepalette.BitmapPalette
import com.github.florent37.glidepalette.GlidePalette
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp

/**
 * Created by chuondao on 11/18/17.
 */
object GlideUtil {

    /**
     *
     */
    fun loadImageAndSetColorOfViews (ctx: Context,
                                     imagePath: String,
                                     imgView: ImageView,
                                     backgroundView: View,
                                     profile: Int) {

        // palette for request listener
        val glidePalette  = GlidePalette
                .with(imagePath)
                .skipPaletteCache(false)
                .use(profile)
                .intoBackground(backgroundView)
        // this fix the null requests issue
        // clear all the pending requests
        GlideApp.with(ctx).clear(imgView)
        // request the load to pallete
        GlideApp.with(ctx)
                .load(imagePath)
                .listener(glidePalette)
                .into(imgView)
    }
}