package getyourcasts.jd.com.getyourcasts.view.media;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import getyourcasts.jd.com.getyourcasts.R;
import getyourcasts.jd.com.getyourcasts.exoplayer.MediaPlayBackService;
import getyourcasts.jd.com.getyourcasts.view.adapter.MediaPlaylistRecyclerAdapter;
import io.reactivex.disposables.Disposable;

/**
 * Created by chuondao on 8/11/17.
 */

public class PlayListFragment extends Fragment {

    RecyclerView playListRecyclerView;
    MediaPlaylistRecyclerAdapter adapter;
    Disposable mediaServiceDisposable;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root =inflater.inflate(R.layout.fragment_playlist, container, false);
        initRecyclerView(root);
        return root;

    }

    @Override
    public void onResume() {
        super.onResume();
        bindMediaService();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mediaService != null){
            getContext().unbindService(mediaServiceConnection);
            mediaService = null;
        }

        if (mediaServiceDisposable != null){
            mediaServiceDisposable.dispose();
            mediaServiceDisposable = null;
        }
    }

    public static PlayListFragment newInstance() {
        PlayListFragment fragment = new PlayListFragment();
        return fragment;
    }

    private void initRecyclerView(View root) {
        playListRecyclerView = (RecyclerView) root.findViewById(R.id.ep_play_list);
        LinearLayoutManager lm = new LinearLayoutManager(this.getContext());
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        playListRecyclerView.setLayoutManager(lm);
        adapter = new MediaPlaylistRecyclerAdapter(this);
        playListRecyclerView.setAdapter(adapter);
    }


    private void bindMediaService() {
        Intent intent = new Intent(this.getContext(), MediaPlayBackService.class);
        this.getContext().bindService(intent, mediaServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /* ============================ CONNECT TO MEDIA SERVICE ========================================= */
    private boolean boundToMediaService = false;
    private MediaPlayBackService mediaService = null;

    // connection to service
    private ServiceConnection mediaServiceConnection =
            new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    boundToMediaService = true;
                    mediaService = ((MediaPlayBackService.MediaPlayBackServiceBinder) (service)).getService();
                    // initialize the adapter playlist
                    // after this they should be in sync
                    adapter.setEpisodeList(mediaService.getMediaPlaylist());
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    boundToMediaService = false;
                }
            };
}
