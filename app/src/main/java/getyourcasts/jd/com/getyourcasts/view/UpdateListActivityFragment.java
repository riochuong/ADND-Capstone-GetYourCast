package getyourcasts.jd.com.getyourcasts.view;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import getyourcasts.jd.com.getyourcasts.R;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Episode;
import getyourcasts.jd.com.getyourcasts.repository.remote.data.Podcast;
import getyourcasts.jd.com.getyourcasts.view.adapter.UpateEpisodeListAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class UpdateListActivityFragment extends Fragment {

    RecyclerView listUpdateRecyclerView;
    UpateEpisodeListAdapter adapter;

    private static final String UPDATE_ITEM_KEY = "update_item_key";
    public UpdateListActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_update_list, container, false);
        listUpdateRecyclerView = (RecyclerView) rootView.findViewById(R.id.update_ep_recyclerview);
        Map<Podcast, List<Episode>> itemMap = getUpdateFromIntent();
        if (itemMap != null){
            adapter = new UpateEpisodeListAdapter(new HashMap<>(), this.getContext());
        }
        LinearLayoutManager lm = new LinearLayoutManager(this.getContext());
        lm.setOrientation(LinearLayout.VERTICAL);
        listUpdateRecyclerView.setLayoutManager(lm);
        listUpdateRecyclerView.setAdapter(adapter);
        return rootView;
    }

    private Map<Podcast, List<Episode>> getUpdateFromIntent(){
        return this.getActivity().getIntent().getParcelableExtra(UPDATE_ITEM_KEY);
    }


}
