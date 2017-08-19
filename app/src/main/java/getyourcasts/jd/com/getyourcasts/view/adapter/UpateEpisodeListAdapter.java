package getyourcasts.jd.com.getyourcasts.view.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import getyourcasts.jd.com.getyourcasts.R;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast;
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp;

/**
 * Created by chuondao on 8/18/17.
 */

public class UpateEpisodeListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Map<Podcast, List<Episode>> epUpdateMap;
    // quick hack for episode to acquire parent podcast
    private Map<String, Podcast>podcastMap;
    private Map<Integer, Podcast> podIndexMap;
    /*map between ep index and ep*/
    private Map<Integer, Episode> epIndexMap;
    private Context context ;
    private static final int VIEW_TYPE_POD = 0;
    private static final int VIEW_TYPE_EPISODE = 1;

    public UpateEpisodeListAdapter(Map<Podcast, List<Episode>> epUpdateMap, Context ctx) {
        super();
        this.epUpdateMap = epUpdateMap;
        context = ctx;
        calculatePosition();
    }

    public void setEpUpdateMap(Map<Podcast, List<Episode>> epUpdateMap) {
        this.epUpdateMap = epUpdateMap;
        calculatePosition();
        notifyDataSetChanged();
    }

    private void calculatePosition() {
        int index = 0;
        // add pocast index to map
        podIndexMap = new HashMap<>();
        podcastMap = new HashMap<>();
        epIndexMap = new HashMap<>();
        for (Podcast pod : epUpdateMap.keySet()){
            podIndexMap.put(index++, pod);
            podcastMap.put(pod.getCollectionId(), pod);
            // add episode index to map
            for (Episode ep : epUpdateMap.get(pod)){
                epIndexMap.put(index++, ep);
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_POD){
            View podView = LayoutInflater.from(parent.getContext()).inflate(R.layout.update_pod_title_layout, parent,
                    false);
            RecyclerView.ViewHolder podVh = new UpdatePodViewHolder(podView);
            return podVh;
        }
        else{
            View podView = LayoutInflater.from(parent.getContext()).inflate(R.layout.episode_playlist_item_layout, parent,
                    false);
            RecyclerView.ViewHolder podVh = new UpdateEpViewHolder(podView);
            return podVh;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (podIndexMap.containsKey(position)){
            return VIEW_TYPE_POD;
        }
        return VIEW_TYPE_EPISODE;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_POD){
            // view type podcast
            Podcast pod = podIndexMap.get(position);
            ((UpdatePodViewHolder) holder).podTitle.setText(pod.getCollectionName());
            if (pod.getArtistName() != null) ((UpdatePodViewHolder) holder).artistTitle.setText(pod.getArtistName());

        } else {
            // view type episode
            Episode ep = epIndexMap.get(position);
            Podcast relatedPod = podcastMap.get(ep.getPodcastId());
            // load podcast image to ep item
            GlideApp.with(context)
                    .load(relatedPod.getImgLocalPath())
                    .into( ((UpdateEpViewHolder)holder).podImg);
            UpdateEpViewHolder epViewHolder = (UpdateEpViewHolder) holder;
            epViewHolder.epTitle.setText(ep.getTitle());
            epViewHolder.epPubDate.setText(ep.getPubDate());
        }
    }

    @Override
    public int getItemCount() {
        // quick check for error
        int numPod = epUpdateMap.keySet().size();
        int numEp = 0;
        if (epUpdateMap == null){
            return 0;
        }

        for (Podcast key : epUpdateMap.keySet()) {
            numEp += epUpdateMap.get(key).size();
        }

        return numEp + numPod;
    }


    class UpdateEpViewHolder extends RecyclerView.ViewHolder {
        ImageView podImg;
        TextView epTitle;
        TextView epPubDate;
        public UpdateEpViewHolder(View itemView) {
            super(itemView);
            podImg = (ImageView) itemView.findViewById(R.id.ep_img);
            epTitle = (TextView) itemView.findViewById(R.id.episode_name);
            epPubDate = (TextView) itemView.findViewById(R.id.episode_date);
        }
    }

    class UpdatePodViewHolder extends RecyclerView.ViewHolder {
        TextView podTitle;
        TextView artistTitle;
        public UpdatePodViewHolder(View itemView) {
            super(itemView);
            podTitle = (TextView) itemView.findViewById(R.id.pod_update_title_text);
            artistTitle = (TextView) itemView.findViewById(R.id.pod_update_artist_text);
        }
    }
}
