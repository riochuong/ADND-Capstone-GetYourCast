package getyourcasts.jd.com.getyourcasts.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import getyourcasts.jd.com.getyourcasts.R;
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast;
import getyourcasts.jd.com.getyourcasts.repository.remote.network.NetworkHelper;
import getyourcasts.jd.com.getyourcasts.util.StorageUtil;
import getyourcasts.jd.com.getyourcasts.view.PodcastDetailsActivity;
import getyourcasts.jd.com.getyourcasts.view.SearchPodcastFragment;
import getyourcasts.jd.com.getyourcasts.view.glide.GlideApp;
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastState;
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Created by chuondao on 9/10/17.
 */

public class SearchPodcastRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final  String TAG = "PocastAdapter";
    private static final  String PODCAST_KEY = "podcast_key";
    private static final  String ITEM_POS_KEY = "item_pos_key";
    private static final  int REQUEST_CODE = 1;

    List<Podcast> podcastList;
    SearchPodcastFragment fragment;
    private PodcastViewModel viewModel;
    private Context ctx;
    private List<Disposable> disposableList = new ArrayList<Disposable>();



    public SearchPodcastRecyclerViewAdapter(List<Podcast> podcastList, SearchPodcastFragment fragment) {
        this.podcastList = podcastList;
        this.fragment = fragment;
        viewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(fragment.getContext()));
        ctx = fragment.getContext();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.podcast_item_layout, parent, false);
        // set view onClickListener
        RecyclerView.ViewHolder vh = new PodcastItemViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final Podcast podcast = podcastList.get(position);
        final PodcastItemViewHolder podcastVh = (PodcastItemViewHolder) holder;
        podcastVh.author.setText(podcast.getArtistName());
        podcastVh.title.setText(podcast.getCollectionName());
        // need glide to load the image here
        Observable<Podcast> checkPodcastDbObs = viewModel.getPodcastObservable(podcast.getCollectionId());
        // check to decide where to load image
        checkPodcastDbObs.observeOn(AndroidSchedulers.mainThread()).subscribe(
                new Observer<Podcast>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Podcast it) {
                        // this true mean podcast is already subscribed
                        Podcast podcastToPass = null;
                        if (! it.getCollectionId().trim().equals("")) {
                            podcastVh.downloadedView.setImageResource(R.mipmap.ic_downloaded);
                            // disable on click listener
                            Log.d(TAG,"Load image from local path ${it.imgLocalPath}");
                            GlideApp.with(fragment).load(it.getImgLocalPath().trim()).into(podcastVh.imgView);
                            podcastToPass = it;
                        } else {
                            podcastVh.downloadedView.setImageResource(R.mipmap.ic_subscribe);
                            if (podcast.getArtworkUrl100() != null) {
                                GlideApp.with(fragment).load(podcast.getArtworkUrl100().trim()).into(podcastVh.imgView);
                            }
                            podcastToPass = podcast;
                        }

                        // subscribe to listen to change in podcast
                        if (podcastToPass != null){
                            subscribeToPodcastUpdate(podcastToPass.getCollectionId(),position);
                        }

                        // set onclickListenter to launch details podcast
                        Podcast finalPodcastToPass = podcastToPass;
                        podcastVh.itemView.setOnClickListener(viewItem -> {

                            // need to get the podcast from db to make sure it's updated
                            viewModel.getPodcastObservable(finalPodcastToPass.getCollectionId())
                                .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                            new Observer<Podcast>() {
                                                @Override
                                                public void onSubscribe(Disposable d) {

                                                }

                                                @Override
                                                public void onNext(Podcast it) {
                                                    Intent intent = new Intent(SearchPodcastRecyclerViewAdapter.this
                                                            .ctx, PodcastDetailsActivity.class);
                                                    // just in case the podcast is not in the db
                                                    if (it.getCollectionId().equals("")){
                                                        intent.putExtra(PODCAST_KEY, podcast);
                                                    } else{
                                                        intent.putExtra(PODCAST_KEY, it);
                                                    }

                                                    intent.putExtra(ITEM_POS_KEY,position);
                                                    fragment.getActivity().startActivityForResult(intent, REQUEST_CODE);
                                                }

                                                @Override
                                                public void onError(Throwable e) {
                                                    e.printStackTrace();
                                                    Log.e(TAG,"Unexpected Error before launch detailed podcast " +
                                                            "activity");
                                                }

                                                @Override
                                                public void onComplete() {

                                                }
                                            }
                                    );



                        });


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

        // set onclick listener for download image locally and insert podcast to db
        podcastVh.downloadedView.setOnClickListener(
                view ->{
                    if (! NetworkHelper.isConnectedToNetwork(SearchPodcastRecyclerViewAdapter.this.ctx)) {
                        NetworkHelper.showNetworkErrorDialog(ctx);
                        return;
                    }
                    viewModel.getSubscribeObservable(podcast)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Observer<Boolean>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onNext(Boolean it) {
                                    if (it){
                                        Log.d(SearchPodcastRecyclerViewAdapter.TAG,
                                                "Insert Podcast To DB Complete ${podcast.collectionName}");
                                        StorageUtil.startGlideImageDownload(podcast, ctx);
                                        // change the icon
                                        podcastVh.downloadedView.setImageResource(R.mipmap.ic_downloaded);

                                    } else{
                                        Log.e(SearchPodcastRecyclerViewAdapter.TAG, "Insert Podcast to DB " +
                                                "Failed. Maybe a duplicate  ");
                                    }
                                }

                                @Override
                                public void onError(Throwable it) {
                                    it.printStackTrace();
                                }

                                @Override
                                public void onComplete() {

                                }
                            });
        });

    }

    @Override
    public int getItemCount() {
        return podcastList.size();
    }

    private void subscribeToPodcastUpdate(final String podcastId , final int pos) {
        PodcastViewModel.subscribePodcastSubject(
                new Observer<PodcastState>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposableList.add(d);
                    }

                    @Override
                    public void onNext(PodcastState t) {
                        if (t.getUniqueId().equals(podcastId)) {
                            // only the button and state have to change
                            SearchPodcastRecyclerViewAdapter.this.notifyItemChanged(pos);
                        }
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

    public void cleanUpAllDisposable(){
        for (Disposable d: disposableList){
            d.dispose();
        }
        disposableList = new ArrayList<>();
    }

    public void setPodcastList(List<Podcast> podcastList) {
        this.podcastList = podcastList;
    }

    class PodcastItemViewHolder extends  RecyclerView.ViewHolder {

        ImageView imgView;
        TextView author;
        TextView title;
        ImageView downloadedView;

        PodcastItemViewHolder(View itemView) {
            super(itemView);
            imgView = (ImageView) itemView.findViewById(R.id.podcast_image);
            author = (TextView) itemView.findViewById(R.id.podcast_author);
            title = (TextView) itemView.findViewById(R.id.podcast_title);
            downloadedView = (ImageView) itemView.findViewById(R.id.podcast_downloaded_img);
        }
    }
}
