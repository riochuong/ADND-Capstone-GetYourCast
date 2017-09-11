package getyourcasts.jd.com.getyourcasts.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Pair;
import android.util.Log;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast;
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


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
    private static final String PLAYLIST_ROOT = "playlist";
    private static final String PLAYLIST_FILE_NAME = "currPlaylist";
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
        File file = ctx.getDir(MEDIA_ROOT, Context.MODE_PRIVATE);
        String fileName = ep.getEpisodeUniqueKey();
        return new Pair(file.getAbsolutePath(), fileName);
    }

    public static boolean cleanUpOldFile(Episode ep, Context ctx) {
        boolean res = false;
        try {
            File file = ctx.getDir(MEDIA_ROOT, Context.MODE_PRIVATE);
            String fileName = ep.getEpisodeUniqueKey();
            File finalPath = new File(file, fileName);
            // clean up file to prepare for new download
            if (file.exists()) res = finalPath.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }


    public static void startGlideImageDownload(Podcast pod, Context ctx) {
        GlideApp.with(ctx)
                .asBitmap()
                .load(pod.getArtworkUrl100())
                .into(getStorageTarget(pod, ctx));
    }

    public static List<Episode> loadMediaPlayList (Context context) {
        File playListFile = new File(context.getDir(PLAYLIST_ROOT, Context.MODE_PRIVATE), PLAYLIST_FILE_NAME);
        StringBuilder sb = new StringBuilder();
        if (!playListFile.exists()) {
            return new ArrayList<>();
        }
        // read data back in
        try {
            BufferedReader bf = new BufferedReader(new FileReader(playListFile));
            String newLine = bf.readLine();
            while (newLine != null) {
                sb.append(newLine);
                newLine = bf.readLine();
            }
            Type episodeListType = new TypeToken<ArrayList<Episode>>(){}.getType();
            Gson gson = new Gson();
            return gson.fromJson(sb.toString(),episodeListType);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static boolean saveMediaPlayList (Context context, List<Episode> playList) {
        FileWriter fw = null;
        try {
            File playListFile = context.getDir(PLAYLIST_ROOT, Context.MODE_PRIVATE);
            fw = new FileWriter(new File(playListFile, PLAYLIST_FILE_NAME));
            Gson gson = new Gson();
            gson.toJson(playList, fw);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (fw != null){
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fw = null;
            }
        }
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
                                                    Log.d(TAG, "Successfully download image :"+pod.getArtworkUrl100());
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




