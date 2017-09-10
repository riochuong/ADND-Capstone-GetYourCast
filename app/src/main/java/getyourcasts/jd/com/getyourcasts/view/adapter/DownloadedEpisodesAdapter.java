package getyourcasts.jd.com.getyourcasts.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import getyourcasts.jd.com.getyourcasts.R;
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast;
import getyourcasts.jd.com.getyourcasts.view.DownloadsFragment;
import getyourcasts.jd.com.getyourcasts.view.EpisodeInfoActivity;
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp;
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Created by chuondao on 9/9/17.
 */

public class DownloadedEpisodesAdapter extends RecyclerView.Adapter<DownloadedEpisodesAdapter.DownloadedEpViewHolder> {

    List<Episode> episodeList;
    DownloadsFragment fragment;
    PodcastViewModel viewModel;

    public static final  String EPISODE_KEY = "episode";
    public static final String BG_COLOR_KEY = "bg_color";
    public static final String PODAST_IMG_KEY = "podcast_img";

    public DownloadedEpisodesAdapter(List<Episode> episodeList, DownloadsFragment fragment) {
        this.episodeList = episodeList;
        this.fragment = fragment;
        this.viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(fragment.getContext()));
    }

    public void updateEpisodeList(List<Episode> updatedData) {
        this.episodeList = updatedData;
        this.notifyDataSetChanged();
    }

    @Override
    public DownloadedEpViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.download_item_layout, parent, false);
        return new DownloadedEpViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DownloadedEpViewHolder holder, int position) {
            Episode ep = episodeList.get(position);
            holder.epTitle.setText(ep.getTitle());
            // remove item from list
            holder.removeImg.setOnClickListener(
                    view -> {
                         episodeList.remove(holder.getAdapterPosition());
                         notifyItemRemoved(holder.getAdapterPosition());
                    }
            );

            viewModel.getPodcastObservable(ep.getPodcastId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Observer<Podcast>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(Podcast podcast) {
                                // now load image to imgview
                                GlideApp.with(DownloadedEpisodesAdapter.this.fragment.getContext())
                                        .load(podcast.getImgLocalPath())
                                        .into(holder.podImg);
                                holder.mainLayout.setOnClickListener(
                                        view -> {
                                            // start episode info details layout.
                                            Intent startEpInfo = new Intent(fragment.getContext(),
                                                    EpisodeInfoActivity.class);
                                            startEpInfo.putExtra(EPISODE_KEY, ep);
                                            startEpInfo.putExtra(BG_COLOR_KEY, Integer.parseInt(podcast
                                                    .getVibrantColor()));
                                            startEpInfo.putExtra(PODAST_IMG_KEY, podcast.getImgLocalPath());
                                            fragment.getContext().startActivity(startEpInfo);
                                        }
                                );
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {

                            }
                        }
                );

    }

    @Override
    public int getItemCount() {
        if (episodeList != null) {return episodeList.size();}
        return 0;
    }

    class DownloadedEpViewHolder extends  RecyclerView.ViewHolder {
        ImageView podImg;
        TextView epTitle;
        ImageView removeImg;
        CardView mainLayout;

        DownloadedEpViewHolder(View itemView) {
            super(itemView);
            podImg = (ImageView) itemView.findViewById(R.id.download_ep_img);
            epTitle = (TextView) itemView.findViewById(R.id.download_ep_title);
            removeImg = (ImageView) itemView.findViewById(R.id.download_remove_img);
            mainLayout = (CardView) itemView.findViewById(R.id.download_item_main_layout);
        }
    }
}
