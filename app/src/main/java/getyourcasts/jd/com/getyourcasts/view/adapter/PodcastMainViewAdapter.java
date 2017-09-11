package getyourcasts.jd.com.getyourcasts.view.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import getyourcasts.jd.com.getyourcasts.R;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast;
import getyourcasts.jd.com.getyourcasts.view.EpisodeListActivity;
import getyourcasts.jd.com.getyourcasts.view.MainPodcastFragment;
import getyourcasts.jd.com.getyourcasts.view.PodcastDetailsFragment;
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp;

/**
 * Created by chuondao on 9/10/17.
 */

public class PodcastMainViewAdapter extends RecyclerView
        .Adapter<PodcastMainViewAdapter.PodcastViewHolder> {


    List<Podcast> podcastList ;
    MainPodcastFragment fragment;

    public PodcastMainViewAdapter(List<Podcast> podcastList, MainPodcastFragment fragment) {
        this.podcastList = podcastList;
        this.fragment = fragment;
    }

    @Override
    public PodcastViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.subscribed_pod_layout_item, parent, false);
        PodcastViewHolder vh = new PodcastViewHolder(view);

        return vh;
    }

    @Override
    public void onBindViewHolder(PodcastViewHolder holder, int position) {
        // load image to img view
        if (holder != null) {
            Podcast podcast = podcastList.get(position);
            if (holder != null && podcast.getImgLocalPath() != null) {
                GlideApp.with(this.fragment.getContext()).load(podcast.getImgLocalPath()).into(holder.imgView);
            }
            setImgViewOnClick(holder, podcast);
        }
    }

    @Override
    public int getItemCount() {
        return podcastList.size();
    }

    private void setImgViewOnClick(PodcastViewHolder vh , Podcast podcast ){
        vh.imgView.setOnClickListener(
            view ->{
                Intent intent = new Intent(fragment.getContext(), EpisodeListActivity.class);
                 intent.putExtra(PodcastDetailsFragment.PODCAST_KEY, podcast);
                fragment.getContext().startActivity(intent);
        });

    }

    public void setPodcastList(List<Podcast> podcastList) {
        this.podcastList = podcastList;
    }

    class PodcastViewHolder extends RecyclerView.ViewHolder{
        ImageView imgView;
        PodcastViewHolder(View itemView) {
            super(itemView);
            imgView = (ImageView) itemView.findViewById(R.id.subscribed_pod_img);
        }
    }
}
