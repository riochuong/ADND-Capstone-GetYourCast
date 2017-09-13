package getyourcasts.jd.com.getyourcasts.view;


import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.tonyodev.fetch.listener.FetchListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

import getyourcasts.jd.com.getyourcasts.R;
import getyourcasts.jd.com.getyourcasts.exoplayer.MediaPlayBackService;
import getyourcasts.jd.com.getyourcasts.repository.local.Contract;
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast;
import getyourcasts.jd.com.getyourcasts.repository.remote.network.DownloadService;
import getyourcasts.jd.com.getyourcasts.view.adapter.EpisodesRecyclerViewAdapter;
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp;
import getyourcasts.jd.com.getyourcasts.view.media.MediaServiceBoundListener;
import getyourcasts.jd.com.getyourcasts.viewmodel.EpisodeOfPodcastLoader;
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * A placeholder fragment containing a simple view.
 */
public class EpisodeListFragment extends Fragment implements MediaServiceBoundListener, PopupMenu
        .OnMenuItemClickListener, LoaderManager.LoaderCallbacks<List<Episode>> {


    private Podcast podcast;

    private PodcastViewModel viewModel;

    private EpisodesRecyclerViewAdapter episodeAdapter;

    private MediaPlayBackService mediaService = null;

    LoaderManager loaderManager;


    private static final String PODCAST_KEY = "podcast_key";
    private static final String TAG = EpisodeListFragment.class.getSimpleName();
    private static final int PALETTE_BG_MASK = 0x00555555;
    private static final int EPISODE_LIST_LOADER_ID_ = 852;

    // UI ITEMs
    ImageButton show_menu_btn;
    TextView episode_podcast_title;
    RecyclerView episode_list_recylcer_view;
    AppBarLayout podcast_detail_appbar;
    ImageView episode_podcast_img;
    AVLoadingIndicatorView episode_list_loading_prog_view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        podcast = getPodcastFromIntent();
        viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(getContext()));
        // register listener for when media service is bound
        if (mediaService == null) {
            ((EpisodeListActivity)getActivity()).registerMediaServiceBoundListenter( service -> mediaService  =service);
        }
        View root = inflater.inflate(R.layout.fragment_episode_list, container, false);
        show_menu_btn = (ImageButton) root.findViewById(R.id.show_menu_btn);
        episode_podcast_title = (TextView) root.findViewById(R.id.episode_podcast_title);
        episode_list_recylcer_view = (RecyclerView) root.findViewById(R.id.episode_list_recylcer_view);
        podcast_detail_appbar = (AppBarLayout) root.findViewById(R.id.podcast_detail_appbar);
        episode_podcast_img = (ImageView) root.findViewById(R.id.episode_podcast_img);
        episode_list_loading_prog_view = (AVLoadingIndicatorView) root.findViewById(R.id
                .episode_list_loading_prog_view);
        loaderManager = getActivity().getSupportLoaderManager();
        return root;
    }




    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.action_unsubscribe){
            Log.d(TAG, "Unsubscribe from Podcast ${podcast.collectionName}");
            viewModel.getUnsubscribeObservable(podcast.getCollectionId())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Boolean>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Boolean res) {
                            if (res) {
                                getActivity().finish();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
                            // on next here


            return true;
        }
        return false;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        startAnim();
        super.onViewCreated(view, savedInstanceState);
        // now load image
        initViews();
        bindDownloadService();

        // set menu options for unsubscribed
        show_menu_btn.setOnClickListener( viewItem -> {
            PopupMenu popup = new PopupMenu(this.getContext(), viewItem);
            // This activity implements OnMenuItemClickListener
            popup.setOnMenuItemClickListener(this);
            popup.inflate(R.menu.menu_episode_list_details);
            popup.show();
        } );
    }


    @Override
    public void onResume() {
        super.onResume();
        ((EpisodeListActivity)getActivity()).registerMediaServiceBoundListenter(this);
        bindDownloadService();
    }


    @Override
    public void onPause() {
        super.onPause();
        mediaService = null;
    }

    private void initViews() {
        initRecyclerView();
        loadPodcastImage();
        // now load title
        episode_podcast_title.setText(podcast.getCollectionName());
        loaderManager.initLoader(EPISODE_LIST_LOADER_ID_,new Bundle(),this).forceLoad();
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        episode_list_recylcer_view.setLayoutManager(layoutManager);
        // initialize with empty list for now
        episodeAdapter = new EpisodesRecyclerViewAdapter( new ArrayList<>(), this, podcast);
        episode_list_recylcer_view.setAdapter(episodeAdapter);
    }

    private void loadPodcastImage() {
        GlideApp.with(getContext())
                .load(podcast.getImgLocalPath())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e,
                                                Object model, Target<Drawable> target, boolean isFirstResource) {
                        if (e != null){
                            e.printStackTrace();
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource,
                                                    Object model,
                                                   Target<Drawable> target,
                                                   DataSource dataSource,
                                                   boolean isFirstResource) {

                        if (resource != null &&
                                            resource instanceof BitmapDrawable){
                            Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                            // this will done in background thread with
                            Palette.from(bitmap).generate(palette -> {
                                int vibrantColor = palette.getDarkVibrantColor(PALETTE_BG_MASK);
                                podcast_detail_appbar.setBackgroundColor(vibrantColor);
                                if (palette.getDarkVibrantSwatch() != null){
                                    episode_podcast_title.setTextColor(palette.getDarkVibrantSwatch()
                                            .getTitleTextColor());
                                }
                                ContentValues cv = new ContentValues();
                                cv.put(Contract.PodcastTable.VIBRANT_COLOR, vibrantColor+"");
                                viewModel.getUpdatePodcastObservable(podcast, cv)
                                        .subscribe(
                                                new Observer<Boolean>() {
                                                    @Override
                                                    public void onSubscribe(Disposable d) {

                                                    }

                                                    @Override
                                                    public void onNext(Boolean aBoolean) {
                                                        Log.d(TAG," Finish update vibrant color for podcast ");
                                                    }

                                                    @Override
                                                    public void onError(Throwable e) {
                                                        e.printStackTrace();
                                                    }

                                                    @Override
                                                    public void onComplete() {

                                                    }
                                                }
                                        );
                            });
                        }
                        return false;
                    }
                }).into(episode_podcast_img);


    }


    private Podcast getPodcastFromIntent()
    {
        Podcast podcast = getActivity().getIntent().getParcelableExtra(PODCAST_KEY);
        if (podcast != null) {
            return podcast;
        }
        return null;
    }

    /* ============================ CONNECT TO DOWNLOAD SERVICE ========================================= */
    private boolean boundToDownload = false;
    private DownloadService downloadService = null;

    // connection to service
    private ServiceConnection downloadServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            boundToDownload = true;
            downloadService = ((DownloadService.DownloadServiceBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            boundToDownload = false;
        }
    };

    public Long requestDownload(Episode ep,
                         String url,
                         String dir,
                         String fileName)

    {
        if (downloadService != null) {
            return downloadService.requestDownLoad(ep, url, dir, fileName);
        }
        return -1L;
    }

    public void  requestStopDownload(Long id) {
        if (downloadService != null) {

        }
    }

    public void registerListener(FetchListener listener) {
        if (downloadService != null) {
            downloadService.registerListener(listener);
        }
    }

    public void  unRegisterListener(FetchListener listener) {
        if (downloadService != null) {
            downloadService.unregisterListener(listener);
        }
    }

    private void bindDownloadService() {
        Intent intent = new Intent(this.getContext(), DownloadService.class);
        this.getContext().bindService(intent, downloadServiceConnection, Context.BIND_AUTO_CREATE);
    }


    public void  requestToPlaySong(Episode episode) {
        if (mediaService != null) {
            mediaService.playMediaFile(episode);
        }

    }

    public void requestToPause() {
        if (mediaService != null) {
            mediaService.pausePlayback();
        }

    }

    public void requestToResume() {
        if (mediaService != null) {
            mediaService.resumePlayback();
        }

    }


    /*=================================================================================================== */


    @Override
    public void onDestroy() {
        if (downloadService != null && boundToDownload) {
            boundToDownload = false;
            getContext().unbindService(downloadServiceConnection);
        }
        super.onDestroy();
        episodeAdapter.cleanUpAllDisposables();
    }

    @Override
    public void onMediaServiceBound(MediaPlayBackService service) {
        stopAnim();
        mediaService = service;
    }


    private void startAnim() {
        episode_list_loading_prog_view.show();
        episode_list_loading_prog_view.setVisibility(View.VISIBLE);
        podcast_detail_appbar.setVisibility(View.INVISIBLE);
    }

    private void stopAnim() {
        episode_list_loading_prog_view.hide();
        episode_list_loading_prog_view.setVisibility(View.GONE);
        podcast_detail_appbar.setVisibility(View.VISIBLE);
    }

    @Override
    public Loader<List<Episode>> onCreateLoader(int id, Bundle args) {
        if (id != EPISODE_LIST_LOADER_ID_) {
            throw new IllegalArgumentException("Wrong loader id received...must be weird");
        }
        return new EpisodeOfPodcastLoader(getActivity(),podcast.getCollectionId());
    }

    @Override
    public void onLoadFinished(Loader<List<Episode>> loader, List<Episode> episodeList) {
        if (episodeList.size() > 0) {
            episodeAdapter.setEpisodeList(episodeList);
            episodeAdapter.notifyDataSetChanged();
            // now show the image
            stopAnim();
            // make the main view visible now
            Log.d(TAG,
                    "Successfully fetched all Episodes of Podcast : ${podcast" +
                            ".collectionName}");
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Episode>> loader) {

    }
}
