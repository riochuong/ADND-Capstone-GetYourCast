package getyourcasts.jd.com.getyourcasts.util

import android.content.Context
import android.os.Build
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageView
import com.github.florent37.glidepalette.BitmapPalette
import com.github.florent37.glidepalette.GlidePalette
import getyourcasts.jd.com.getyourcasts.R
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
                .intoCallBack {
                    if (it != null) {
                        val primaryColor = ContextCompat.getColor(ctx,R.color.colorPrimaryDark)
                        val colorBackground =
                                when (profile) {
                                    BitmapPalette.Profile.VIBRANT_DARK -> it.getDarkVibrantColor(primaryColor)
                                    BitmapPalette.Profile.MUTED_DARK -> it.getDarkMutedColor(primaryColor)
                                    BitmapPalette.Profile.VIBRANT_LIGHT -> it.getLightVibrantColor(primaryColor)
                                    BitmapPalette.Profile.MUTED_LIGHT -> it.getLightMutedColor(primaryColor)
                                    else -> ContextCompat.getColor(ctx,R.color.colorPrimaryDark)
                                }
                        backgroundView.setBackgroundColor(colorBackground)
                    }

                }
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