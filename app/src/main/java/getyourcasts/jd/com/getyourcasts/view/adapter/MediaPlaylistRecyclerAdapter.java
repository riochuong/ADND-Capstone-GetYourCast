package getyourcasts.jd.com.getyourcasts.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import getyourcasts.jd.com.getyourcasts.R;
import getyourcasts.jd.com.getyourcasts.exoplayer.MediaPlayBackService;
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast;
import getyourcasts.jd.com.getyourcasts.util.TimeUtil;
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp;
import getyourcasts.jd.com.getyourcasts.view.media.PlayListFragment;
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static getyourcasts.jd.com.getyourcasts.exoplayer.MediaPlayBackService.MEDIA_REMOVED_FROM_PLAYLIST;

/**
 * Created by chuondao on 8/12/17.
 */

public class MediaPlaylistRecyclerAdapter extends RecyclerView.Adapter<MediaPlaylistRecyclerAdapter.PlaylistItemViewHolder>
         {

    private PlayListFragment playListFragment;
    private List<Episode> episodeList;
    private PodcastViewModel viewModel;

    public MediaPlaylistRecyclerAdapter(PlayListFragment frag) {
        this.playListFragment = frag;
        episodeList = new ArrayList<>();
        viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(this.playListFragment.getContext()));
        // attach touch helper for swipe to remove effects
    }

    /*helper to add / remove item from list */
    public void setEpisodeList(List<Episode> newList) {
         episodeList = newList;
         this.notifyDataSetChanged();
    }

    public void addItemToTopList(Episode ep){
        episodeList.add(0, ep);
        this.notifyDataSetChanged();
    }

    public void addItemToEndList(Episode ep){
        episodeList.add(ep);
        this.notifyItemChanged(episodeList.size() - 1);
    }


    @Override
    public PlaylistItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.episode_playlist_item_layout,
                parent, false);
        PlaylistItemViewHolder vh = new PlaylistItemViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(PlaylistItemViewHolder holder, int position) {
        Episode ep = episodeList.get(position);
        TimeUtil.DatePub datePub = TimeUtil.parseDatePub(ep.getPubDate());
        holder.epDate.setText(datePub.getMonth()+","+datePub.getDayOfMonth()+" "+datePub.getYear());
        holder.epName.setText(ep.getTitle());
        // get the local podcast image from db and load it
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
                                  GlideApp.with(playListFragment.getContext())
                                          .load(podcast.getImgLocalPath())
                                          .into(holder.podcastImg);
                              }

                              @Override
                              public void onError(Throwable e) {

                              }

                              @Override
                              public void onComplete() {

                              }
                          }
                  );

        // set listenter
        setItemRemoveListener(holder, ep);
    }



    private void setItemRemoveListener(PlaylistItemViewHolder vh, Episode ep) {
        vh.itemRemove.setOnClickListener(
                v -> {
                    MediaPlayBackService.publishMediaPlaybackSubject(ep, MEDIA_REMOVED_FROM_PLAYLIST);
                    episodeList.remove(vh.getAdapterPosition());
                    if (episodeList.size() == 0){
                        MediaPlayBackService.publishMediaPlaybackSubject(null, MediaPlayBackService
                                .MEDIA_PLAYLIST_EMPTY);
                    }
                    notifyItemRemoved(vh.getAdapterPosition());
                }
        );
    }

    @Override
    public int getItemCount() {
        return episodeList.size();
    }




    class PlaylistItemViewHolder extends  RecyclerView.ViewHolder {
        TextView epDate;
        ImageView podcastImg;
        TextView epName;
        ImageView itemRemove;


        public PlaylistItemViewHolder(View itemView) {
            super(itemView);
            epDate = (TextView) itemView.findViewById(R.id.episode_date);
            epName = (TextView)itemView.findViewById(R.id.episode_name);
            podcastImg = (ImageView) itemView.findViewById(R.id.ep_img);
            itemRemove = (ImageView) itemView.findViewById(R.id.remove_item_img);
        }
    }



}
