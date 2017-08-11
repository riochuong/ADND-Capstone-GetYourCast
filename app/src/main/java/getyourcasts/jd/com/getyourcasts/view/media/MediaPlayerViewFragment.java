package getyourcasts.jd.com.getyourcasts.view.media;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import getyourcasts.jd.com.getyourcasts.R;
import getyourcasts.jd.com.getyourcasts.exoplayer.MediaPlayBackService;

/**
 * Created by chuondao on 8/11/17.
 */

public class MediaPlayerViewFragment extends Fragment {


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_media_player_view, container, false);
    }


    @Override
    public void onResume() {
        bindMediaService();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
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
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    boundToMediaService = false;
                }
            };

}
