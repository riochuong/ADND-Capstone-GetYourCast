package getyourcasts.jd.com.getyourcasts.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.CircleProgress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import getyourcasts.jd.com.getyourcasts.R;
import getyourcasts.jd.com.getyourcasts.exoplayer.MediaPlayBackService;
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast;
import getyourcasts.jd.com.getyourcasts.util.ButtonStateUtil;
import getyourcasts.jd.com.getyourcasts.util.StorageUtil;
import getyourcasts.jd.com.getyourcasts.util.TimeUtil;
import getyourcasts.jd.com.getyourcasts.view.EpisodeInfoActivity;
import getyourcasts.jd.com.getyourcasts.view.EpisodeListFragment;
import getyourcasts.jd.com.getyourcasts.viewmodel.EpisodeState;
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Created by chuondao on 8/14/17.
 */

public final class EpisodesRecyclerViewAdapter extends RecyclerView.Adapter<EpisodesRecyclerViewAdapter
        .EpisodeItemViewHolder> {

    private PodcastViewModel viewModel;

    private Context ctx;

    private int bgColor;

    public static final String TAG = EpisodesRecyclerViewAdapter.class.getSimpleName();
    public static final String PODCAST_KEY = "podcast_key";
    public static final String IS_DOWNLOADING = "item_key";
    public static final String DL_TRANS_ID = "dl_trans_id";
    public static final String BG_COLOR_KEY = "bg_color";
    public static final String PODAST_IMG_KEY = "podcast_img";
    public static final  String EPISODE_KEY = "episode";
    public static final int REQUEST_CODE = 1;
    public static final String IS_DOWNLOADING_KEY = "is_downloading";

    private List<Episode> episodeList;
    private EpisodeListFragment fragment;
    private Podcast podcast;

    /*to keep track of downloaidng items to send it to details view */
    private Map<String, EpisodeDownloadListener> downloadItemMaps = new HashMap<>();
    private List<Disposable> disposableList = new ArrayList<>();

    public EpisodesRecyclerViewAdapter(List<Episode> newList,
                                        EpisodeListFragment fragment,
                                       Podcast podcast) {
        episodeList = newList;
        this.fragment = fragment;
        this.podcast = podcast;
        viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(fragment.getActivity().getApplicationContext()));
        ctx = fragment.getActivity();
        // quick hack to get bgColor
        bgColor =
                ((ColorDrawable) ((fragment.getView().findViewById(R.id.podcast_detail_appbar)).getBackground()))
                        .getColor();
    }


    @Override
    public EpisodeItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.episode_item_layout, parent, false);
        // set view onClickListener
        EpisodeItemViewHolder vh = new EpisodeItemViewHolder(view);

        // always make the progress to be red
        vh.progressView.setFinishedColor(ContextCompat.getColor(ctx, R.color.unfin_color));

        return vh;
    }




    public void cleanUpAllDisposables() {

        for (Disposable disposable : disposableList){
            disposable.dispose();
        }

        disposableList = new ArrayList<>();
    }


    @Override
    public void onBindViewHolder(EpisodeItemViewHolder holder, int position) {
        Episode episode = episodeList.get(position);
        // load episode info
        EpisodeItemViewHolder vh = (EpisodeItemViewHolder) holder;
        vh.nameText.setText(episode.getTitle());
        // load date
        if (episode.getPubDate() != null) {
            TimeUtil.DatePub dateParsed = TimeUtil.parseDatePub(episode.getPubDate());
            if (dateParsed != null) {
                vh.monthText.setText(dateParsed.getMonth()+","+dateParsed.getDayOfMonth());
                vh.yearText.setText(dateParsed.getYear());
            }
        }
        // load size
        if (episode.getFileSize() != null) {
            vh.fileSize.setText(StorageUtil.INSTANCE.convertToMbRep(episode.getFileSize()));
        }

        // load download or play icons depends on podcast url link available or not
        loadCorrectDownOrPlayImg(episode, vh);

        // set on click listener to download file and updat progress
        setViewHolderOnClickListener(vh, episode, position);

        // set on click listener for episode detail info
        setOnClickListenerForEpisodeInfo(vh, episode, position);
    }

    @Override
    public int getItemCount() {
        return episodeList.size();
    }


    private void setOnClickListenerForEpisodeInfo(final EpisodeItemViewHolder vh,
                                                  final Episode ep,
                                                 int itemPos
    ) {
        subscribeToEpisodeSyncSubject(ep, vh, itemPos);
        vh.mainLayout.setOnClickListener(

                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ctx, EpisodeInfoActivity.class);
                        intent.putExtra(BG_COLOR_KEY, bgColor);
                        intent.putExtra(EPISODE_KEY, ep);
                        intent.putExtra(PODAST_IMG_KEY, podcast.getImgLocalPath());
                        intent.putExtra(IS_DOWNLOADING, downloadItemMaps.containsKey(ep.getEpisodeUniqueKey()));
                        intent.putExtra(DL_TRANS_ID, vh.transId);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        ctx.startActivity(intent);
                    }
                });

    }


    private void subscribeToEpisodeSyncSubject(final Episode ep, final EpisodeItemViewHolder vh, final int itemPos) {
        PodcastViewModel.subscribeEpisodeSubject(new Observer<EpisodeState>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposableList.add(d);
            }

            @Override
            public void onNext(EpisodeState epState) {
                if (epState.getUniqueId().equals(ep.getEpisodeUniqueKey())) {
                    Pair<String, String> paths = StorageUtil.INSTANCE.getPathToStoreEp(ep, fragment.getActivity()
                            .getApplicationContext());
                    String fullUrl = paths.first+"/"+paths.second;

                    switch(epState.getState()) {
                        case EpisodeState.DOWNLOADING:
                            if (!downloadItemMaps.containsKey(ep.getEpisodeUniqueKey())) {
                                Log.d(TAG, "downloading update for episode ${ep.toString()}");


                                // start showing progress
                                EpisodeDownloadListener listener = getListenerForDownload(epState.getTransId(), vh, ep);
                                // would be really wrong if transId is not available
                                downloadItemMaps.put(ep.getEpisodeUniqueKey(), listener);
                                showProgressView(vh);
                                EpisodesRecyclerViewAdapter.this.fragment.registerListener(listener);
                            }
                            // else it's just a progress update
                            break;

                        case EpisodeState.FETCHED:
                            if (downloadItemMaps.containsKey(ep.getEpisodeUniqueKey())) {
                                EpisodesRecyclerViewAdapter.this.updateItemData(ep, itemPos);
                                downloadItemMaps.remove(ep.getEpisodeUniqueKey());
                            }
                            break;

                        case EpisodeState.DOWNLOADED:
                            if (downloadItemMaps.containsKey(ep.getEpisodeUniqueKey())) {
                                EpisodeDownloadListener oldListener = downloadItemMaps.get(ep.getEpisodeUniqueKey());
                                EpisodesRecyclerViewAdapter.this.fragment.unRegisterListener(oldListener);
                                downloadItemMaps.remove(ep.getEpisodeUniqueKey());
                                EpisodesRecyclerViewAdapter.this.updateItemData(ep, itemPos);
                                vh.state = ButtonStateUtil.PRESS_TO_PLAY;
                                vh.downPlayImg.setImageResource(R.mipmap.ic_ep_play);
                            }
                            break;
                    }
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
    }

    /**
     * show porgress view and hide button
     */
    private void showProgressView(EpisodeItemViewHolder vh) {
        vh.downPlayImg.setVisibility(View.GONE);
        vh.progressView.setVisibility(View.VISIBLE);
    }

    private void hideProgressView(EpisodeItemViewHolder vh) {
        vh.downPlayImg.setVisibility( View.VISIBLE);
        vh.progressView.setVisibility(View.GONE);
    }


    /**
     * Set viewholder logic for pressing download button or play
     */
    private void setViewHolderOnClickListener(final EpisodeItemViewHolder vh,
                                              final Episode episode,
                                              final int pos) {
        if (episode.getDownloaded() == 1) {
            vh.state = ButtonStateUtil.PRESS_TO_PLAY;
            vh.downPlayImg.setImageResource(R.mipmap.ic_ep_play);
        } else {
            vh.state = ButtonStateUtil.PRESS_TO_DOWNLOAD;
            vh.downPlayImg.setImageResource(R.mipmap.ic_ep_down);
        }
        // subscribe to media service info to change the icon appropriately
        MediaPlayBackService.subscribeMediaPlaybackSubject(new Observer<android.util.Pair<Episode, Integer>>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposableList.add(d);
            }

            @Override
            public void onNext(android.util.Pair<Episode, Integer> info) {
                Episode ep = info.first;
                if (ep != null && ep.getUniqueId().equals(episode.getUniqueId())) {
                    int state = info.second;
                    switch(state){
                        case MediaPlayBackService.MEDIA_PAUSE:
                            vh.state = ButtonStateUtil.PRESS_TO_UNPAUSE;
                            vh.downPlayImg.setImageResource(R.mipmap.ic_ep_play);
                            break;
                        case MediaPlayBackService.MEDIA_PLAYING:
                            vh.state = ButtonStateUtil.PRESS_TO_PAUSE;
                            vh.downPlayImg.setImageResource(R.mipmap.ic_pause_for_list);
                            break;
                        case MediaPlayBackService.MEDIA_REMOVED_FROM_PLAYLIST:
                        case MediaPlayBackService.MEDIA_STOPPED:
                            vh.state = ButtonStateUtil.PRESS_TO_PLAY;
                            vh.downPlayImg.setImageResource(R.mipmap.ic_ep_play);
                            break;
                    }
                } else{
                    // this episode is not playing or anything reset it to original state
                    if (vh.state != ButtonStateUtil.PRESS_TO_DOWNLOAD
                            && vh.state != ButtonStateUtil.PRESS_TO_PLAY){
                        vh.state = ButtonStateUtil.PRESS_TO_PLAY;
                        vh.downPlayImg.setImageResource(R.mipmap.ic_ep_play);
                    }

                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

        vh.downPlayImg.setOnClickListener(

                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (vh.state) {
                            case ButtonStateUtil.PRESS_TO_DOWNLOAD:
                                // check if episode is already state or not
                                // bind download service
                                vh.state = ButtonStateUtil.PRESS_TO_STOP_DOWNLOAD;
                                startDownloadEpisodeFile(episode, vh, pos);
                                break;

                            case ButtonStateUtil.PRESS_TO_STOP_DOWNLOAD:
                                if (vh.transId != -1) {
                                    fragment.requestStopDownload(vh.transId);
                                    hideProgressView(vh);
                                }
                                vh.state = ButtonStateUtil.PRESS_TO_DOWNLOAD;
                                break;

                            case ButtonStateUtil.PRESS_TO_PLAY:
                                // limited to downloaded episode only for now
                                vh.state = ButtonStateUtil.PRESS_TO_PAUSE;
                                EpisodesRecyclerViewAdapter.this.fragment.requestToPlaySong(episode);
                                vh.downPlayImg.setImageResource(R.mipmap.ic_pause_for_list);
                                break;

                            case ButtonStateUtil.PRESS_TO_PAUSE:
                                vh.state = ButtonStateUtil.PRESS_TO_UNPAUSE;
                                vh.downPlayImg.setImageResource(R.mipmap.ic_ep_play);
                                EpisodesRecyclerViewAdapter.this.fragment.requestToPause();
                                break;

                            case ButtonStateUtil.PRESS_TO_UNPAUSE:
                                vh.state = ButtonStateUtil.PRESS_TO_PAUSE;
                                vh.downPlayImg.setImageResource(R.mipmap.ic_pause_for_list);
                                EpisodesRecyclerViewAdapter.this.fragment.requestToResume();
                                break;

                        }
                    }
                }
        );

    }

    /**
     * start to download episode here
     */
    private void startDownloadEpisodeFile(Episode episode,
                                          EpisodeItemViewHolder vh,
                                         int itempos) {
        // Now start Downloading
        String url = episode.getDownloadUrl();
        Pair <String, String> pathItems = StorageUtil.INSTANCE.getPathToStoreEp(episode, fragment.getActivity().getApplicationContext());
        // TODO :detect duplicate here to avoid crash
        if (url != null) {
            long transactionId = fragment.requestDownload(episode, url, pathItems.first, pathItems.second);
            vh.transId = transactionId;
            if (transactionId < 0) {
                // TODO : Show some error here
                Log.e(TAG, "Failed to start Download $episode");
            }
        }
    }


    private EpisodeDownloadListener getListenerForDownload(Long transactionId,
                                                           final EpisodeItemViewHolder vh,
                                                           final Episode episode
    )

    {


        return new EpisodeDownloadListener(transactionId) {
            @Override
            public void onProgressUpdate(int progress) {
                vh.progressView.setProgress(progress);
            }

            @Override
            public void onComplete() {

            }

            @Override
            public void onStop() {

            }

            @Override
            public void onError() {
                    Log.e(TAG, "Failed to download episode "+episode.getTitle());
                vh.downPlayImg.setImageResource(R.mipmap.ic_ep_down);
                vh.downPlayImg.setVisibility(View.VISIBLE);
                vh.progressView.setVisibility(View.GONE);
            }
        };

    }



    private void updateItemData(Episode ep,  final int pos) {
        viewModel.getEpisodeObsevable(ep)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Episode>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposableList.add(d);
                    }

                    @Override
                    public void onNext(Episode episode) {
                        episodeList.remove(pos);
                        episodeList.add(pos, episode);
                        notifyItemChanged(pos);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    // suggest to download or play episode
    private void loadCorrectDownOrPlayImg(Episode ep, EpisodeItemViewHolder vh) {
        // check if the file is already state or not
        if (ep.getDownloaded() == 0) {
            vh.downPlayImg.setImageResource(R.mipmap.ic_ep_down);
        } else {
            vh.downPlayImg.setImageResource(R.mipmap.ic_ep_play);
        }
        hideProgressView(vh);
    }


    class EpisodeItemViewHolder extends RecyclerView.ViewHolder

    {

        View mainLayout;
        ImageView downPlayImg;
        TextView monthText;
        TextView nameText;
        TextView yearText;
        TextView fileSize;
        CircleProgress progressView;
        int state = ButtonStateUtil.PRESS_TO_DOWNLOAD;
        // quick hack for stop download
        long transId = -1;

        // bind item to view here
        public EpisodeItemViewHolder (View itemView){
            super(itemView);
            mainLayout = itemView.findViewById(R.id.episode_main_view_layout);
            downPlayImg = (ImageView) itemView.findViewById(R.id.episode_down_play_img);
            nameText = (TextView) itemView.findViewById(R.id.episode_name);
            monthText = (TextView) itemView.findViewById(R.id.episode_month_text);
            yearText = (TextView) itemView.findViewById(R.id.episode_year_text);
            fileSize = (TextView) itemView.findViewById(R.id.episode_file_size);
            progressView = (CircleProgress) itemView.findViewById(R.id.circle_progress);
        }
    }

    public List<Episode> getEpisodeList() {
        return episodeList;
    }

    public void setEpisodeList(List<Episode> episodeList) {
        this.episodeList = episodeList;
    }
}