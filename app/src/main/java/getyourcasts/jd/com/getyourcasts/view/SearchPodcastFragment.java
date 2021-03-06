package getyourcasts.jd.com.getyourcasts.view;


import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import getyourcasts.jd.com.getyourcasts.R;
import getyourcasts.jd.com.getyourcasts.repository.remote.DataSourceRepo;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast;
import getyourcasts.jd.com.getyourcasts.repository.remote.network.NetworkHelper;
import getyourcasts.jd.com.getyourcasts.view.adapter.SearchPodcastRecyclerViewAdapter;
import getyourcasts.jd.com.getyourcasts.viewmodel.PodcastViewModel;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Created by chuondao on 9/10/17.
 */

public class SearchPodcastFragment extends Fragment{
    RecyclerView recyclerView;
    PodcastViewModel searchViewModel;
    SearchPodcastRecyclerViewAdapter searchAdapter;
    EditText search_term_text;
    TextView search_empty_view;
    AVLoadingIndicatorView searching_prog_view;
    ImageView podcast_go_back_icon;
    private static final int LOADER_SEARCH_POD_ID = 417;

    private static final String SEARCH_RESULT_LIST = "search_list_key";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.search_podcast_fragment, container, false);
        recyclerView = root.findViewById(R.id.podcast_list_recycler_view);
        search_term_text = root.findViewById(R.id.search_term_text);
        search_empty_view = root.findViewById(R.id.search_empty_view);
        searching_prog_view = root.findViewById(R.id.searching_prog_view);
        podcast_go_back_icon = root.findViewById(R.id.podcast_go_back_icon);
        searchAdapter = new SearchPodcastRecyclerViewAdapter(new ArrayList<>(), this);
        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Podcast[] savedPodcastArr = new Podcast[searchAdapter.getPodcastList().size()];
        searchAdapter.getPodcastList().toArray(savedPodcastArr);
        outState.putParcelableArray(SEARCH_RESULT_LIST,savedPodcastArr);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        searchViewModel = PodcastViewModel.getInstance(DataSourceRepo.getInstance(this.getContext()));
        setupRecyclerView();
        // set
        podcast_go_back_icon.setOnClickListener(
                v -> SearchPodcastFragment.this.getActivity().finish()
        );
        // restored search adapter instances
        if (savedInstanceState != null ){
            Parcelable[] savedPodcastArr = savedInstanceState.getParcelableArray(SEARCH_RESULT_LIST);
            if (savedPodcastArr != null && savedPodcastArr.length > 0){
                Podcast[] podcastArr = new Podcast[savedPodcastArr.length];
                System.arraycopy(savedPodcastArr, 0, podcastArr, 0, savedPodcastArr.length);
                updatePodcastList(Arrays.asList(podcastArr));
            }
        }
        recyclerView.setAdapter(searchAdapter);

        // SEARCH SUMISSION
        search_term_text.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (! NetworkHelper.isConnectedToNetwork(getContext())){
                    NetworkHelper.showNetworkErrorDialog(getContext());
                    return false;
                }
                // search for podcast here
                startLoadingAnim();
                String searchTerm = search_term_text.getText().toString();


                io.reactivex.Observable<List<Podcast>> searchObsrv = searchViewModel.getPodcastSearchObservable(searchTerm);
                searchObsrv.observeOn(AndroidSchedulers.mainThread()).subscribe(
                        // OnNext
                        new Observer<List<Podcast>>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(List<Podcast> podcastList) {
                                updatePodcastList(podcastList);
                                stopLoadingAnim();
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
                return true;
            }
            return false;
        });
    }



    /**
     * update adapter with newdata from search
     * @newData : newData passed from the results of network fetching
     */
    private void updatePodcastList(List<Podcast> newData ) {
        searchAdapter.setPodcastList(newData);
        searchAdapter.notifyDataSetChanged();
        if (newData != null && newData.size() > 0){
            search_empty_view.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        else{
            recyclerView.setVisibility(View.INVISIBLE);
            search_empty_view.setVisibility(View.VISIBLE);
        }

    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
    }



    private void startLoadingAnim() {
        recyclerView.setVisibility(View.INVISIBLE);
        search_empty_view.setVisibility(View.GONE);
        searching_prog_view.setVisibility(View.VISIBLE);
        searching_prog_view.show();
    }

    private void stopLoadingAnim() {
        searching_prog_view.hide();
        searching_prog_view.setVisibility(View.GONE);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        searchAdapter.cleanUpAllDisposable();
    }

}
