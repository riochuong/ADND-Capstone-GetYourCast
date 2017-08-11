package getyourcasts.jd.com.getyourcasts.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast;
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import kotlin.Pair;

/**
 * Created by chuondao on 8/9/17.
 */

public final class StorageUtil {


    private static final String TAG = "StorageUtil";
    private static final int PODCAST_IMG_TYPE = 0;
    private static final int EPISODE_IMG_TYPE = 1;
    private static final int EPISODE_MEDIA_FILE_TYPE = 2;
    private static final int ONE_MB = 1024 * 1024;
    private static final String NOT_AVAIL_STR = "N/A";

    private static final String MEDIA_ROOT = "media";
    private static final String PODCAST_IMG_ROOT = "podcast_img";
    private static final String EPISODE_MEDIA_FILE_ROOT = "episode_media";
    private static final String PNG_FORMAT = ".png";


    public static String convertToMbRep(String rawSize) {
        try {
            Float fileSize = Float.parseFloat(rawSize);
            fileSize = fileSize / ONE_MB;
            return String.format("%.2f MB",fileSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "NOT_AVAIL_STR";
    }


    // METHODS
    public static String getPathToStorePodImg(Podcast pod, Context ctx) {
        String root = PODCAST_IMG_ROOT;
        // this api wil lcreate directory if needed to
        File file = ctx.getDir(root, Context.MODE_PRIVATE);
        File finalPath = new File(file, pod.getCollectionId() + PNG_FORMAT);
        if (finalPath.exists()) {
            return null;
        }
        // get absolute path
        return finalPath.getAbsolutePath();
    }

    /**
     * check and give the abspath to store ep
     */
    public static Pair<String, String> getPathToStoreEp(Episode ep, Context ctx) {
        String root = MEDIA_ROOT;
        File file = ctx.getDir(root, Context.MODE_PRIVATE);
        String fileName = ep.getEpisodeUniqueKey();
        File finalPath = new File(file, fileName);
        return new Pair(file.getAbsolutePath(), fileName);
    }


    public static void startGlideImageDownload(Podcast pod, Context ctx) {
        GlideApp.with(ctx)
                .asBitmap()
                .load(pod.getArtworkUrl100())
                .into(getStorageTarget(pod, ctx));
    }

    private static SimpleTarget<Bitmap> getStorageTarget(final Podcast pod, final Context ctx) {

        // create bitmap target will save image
        return new SimpleTarget<Bitmap>() {

            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                String file = StorageUtil.getPathToStorePodImg(pod, ctx);
                if (file != null && resource != null) {
                    try {
                        final FileOutputStream os = new FileOutputStream(file);
                        Observable.just(resource.compress(Bitmap.CompressFormat.PNG, 100, os))
                                .subscribeOn(Schedulers.io())
                                .subscribe(
                                        new Observer<Boolean>() {
                                            @Override
                                            public void onSubscribe(Disposable d) {

                                            }

                                            @Override
                                            public void onNext(Boolean aBoolean) {

                                                if (aBoolean) {
                                                    Log.d(TAG, "Successfully download limage"+pod.getArtworkUrl100());
                                                }

                                                try {
                                                    os.close();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                try {
                                                    e.printStackTrace();
                                                    os.close();
                                                } catch (IOException ex) {
                                                    ex.printStackTrace();
                                                }
                                            }

                                            @Override
                                            public void onComplete() {

                                            }
                                        }
                                );

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

    }
}




