package in.lubble.app.explore;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;

public class ExploreFrag extends Fragment implements ExploreGroupAdapter.OnListFragmentInteractionListener {

    private static final String TAG = "ExploreFrag";
    private ExploreGroupAdapter.OnListFragmentInteractionListener mListener;
    private RecyclerView recyclerView;
    private ProgressBar progressbar;
    private TextView joinedAllTv;
    private ExploreGroupAdapter exploreGroupAdapter;

    public ExploreFrag() {
    }

    public static ExploreFrag newInstance() {
        return new ExploreFrag();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Analytics.triggerScreenEvent(getContext(), this.getClass());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        Context context = view.getContext();
        joinedAllTv = view.findViewById(R.id.tv_joined_all);
        recyclerView = view.findViewById(R.id.rv_interest_groups);
        progressbar = view.findViewById(R.id.progressbar);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        joinedAllTv.setVisibility(View.GONE);

        exploreGroupAdapter = new ExploreGroupAdapter(new ArrayList<ExploreGroupData>(), mListener, GlideApp.with(requireContext()), getActivity() instanceof ExploreGroupAdapter.OnListFragmentInteractionListener);
        recyclerView.setAdapter(exploreGroupAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchExploreGroups();
    }

    private void fetchExploreGroups() {
        progressbar.setVisibility(View.VISIBLE);
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.fetchExploreGroups(LubbleSharedPrefs.getInstance().getLubbleId()).enqueue(new Callback<ArrayList<ExploreGroupData>>() {
            @Override
            public void onResponse(Call<ArrayList<ExploreGroupData>> call, Response<ArrayList<ExploreGroupData>> response) {
                final ArrayList<ExploreGroupData> exploreGroupDataList = response.body();
                if (response.isSuccessful() && exploreGroupDataList != null && isAdded() && !exploreGroupDataList.isEmpty() && isAdded() && isVisible()) {
                    progressbar.setVisibility(View.GONE);
                    exploreGroupAdapter.updateList(exploreGroupDataList);
                } else {
                    if (isAdded() && isVisible()) {
                        progressbar.setVisibility(View.GONE);
                        if (exploreGroupDataList != null && exploreGroupDataList.isEmpty()) {
                            if (getActivity() instanceof ExploreGroupAdapter.OnListFragmentInteractionListener) {
                                // exit the explore activity if opened during onboarding & there are no unjoined groups to be shown
                                // will happen if an existing user logs back in and has already joined every group
                                getActivity().finish();
                                getActivity().overridePendingTransition(R.anim.none, R.anim.slide_to_bottom);
                            } else {
                                joinedAllTv.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<ExploreGroupData>> call, Throwable t) {
                Toast.makeText(getContext(), R.string.check_internet, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "onFailure: ");
                progressbar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = this;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListFragmentInteraction(ExploreGroupData item, boolean isAdded) {
        if (getActivity() instanceof ExploreGroupAdapter.OnListFragmentInteractionListener) {
            ((ExploreGroupAdapter.OnListFragmentInteractionListener) getActivity()).onListFragmentInteraction(item, isAdded);
        }
    }

}
