package getyourcasts.jd.com.getyourcasts.view.media;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;

import com.pixelcan.inkpageindicator.InkPageIndicator;

import getyourcasts.jd.com.getyourcasts.R;
import getyourcasts.jd.com.getyourcasts.exoplayer.MediaPlayBackService;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class MediaPlayerActivity extends AppCompatActivity {
    ViewPager viewPager;
    FragmentPagerAdapter pagerAdatper;
    InkPageIndicator circleIndicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player_layout);
        setupPager();
    }

    private void setupPager(){
        viewPager = (ViewPager) findViewById(R.id.media_pager);
        circleIndicator = (InkPageIndicator) findViewById(R.id.media_indicator);
        pagerAdatper = new MediaPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdatper);
        circleIndicator.setViewPager(viewPager);

    }

    public void notifyDataChangedToRedrawFragment(){
        pagerAdatper.notifyDataSetChanged();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    static class MediaPagerAdapter extends FragmentPagerAdapter {

        private static final int NUM_PAGE = 2;

        private static final int PLAYER_PAGE = 0;

        private static final int PLAYLIST_PAGE = 1;

        public MediaPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case PLAYER_PAGE:
                    return MediaPlayerViewFragment.newInstance();
                case PLAYLIST_PAGE:
                    return PlayListFragment.newInstance();
            }
            // should not get here
            return null;
        }

        @Override
        public int getCount() {
            return NUM_PAGE;
        }
    }
}
