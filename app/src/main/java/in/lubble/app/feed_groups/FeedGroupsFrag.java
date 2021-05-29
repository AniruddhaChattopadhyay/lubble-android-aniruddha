package in.lubble.app.feed_groups;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import in.lubble.app.GlideApp;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.feed_groups.SingleGroupFeed.GroupFeedActivity;
import in.lubble.app.models.FeedGroupData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedGroupsFrag extends Fragment implements FeedGroupAdapter.OnListFragmentInteractionListener {

    private ShimmerRecyclerView groupRecyclerView;
    private List<FeedGroupData> feedGroupDataList;
    private TextView joinedAllTv;
    private FeedGroupAdapter.OnListFragmentInteractionListener mListener;
    private TextView titleTv;
    private TextView descTv;
    private FeedGroupAdapter adapter;
    private boolean isOnboarding;
    private LinearLayout searchContainer;
    private EditText searchEt;

    public FeedGroupsFrag() {
        // Required empty public constructor
    }

    public static FeedGroupsFrag newInstance() {
        FeedGroupsFrag fragment = new FeedGroupsFrag();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Analytics.triggerScreenEvent(getContext(), this.getClass());
        isOnboarding = getActivity() instanceof FeedGroupAdapter.OnListFragmentInteractionListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_feed_groups, container, false);

        joinedAllTv = rootView.findViewById(R.id.tv_joined_all);
        titleTv = rootView.findViewById(R.id.tv_title);
        descTv = rootView.findViewById(R.id.tv_desc);
        groupRecyclerView = rootView.findViewById(R.id.feed_group_recyclerview);
        searchContainer = rootView.findViewById(R.id.container_search);
        searchEt = rootView.findViewById(R.id.et_search);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        groupRecyclerView.setLayoutManager(layoutManager);
        groupRecyclerView.showShimmerAdapter();

        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null)
                    adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Log.d(TAG,s+"*******************");
            }
        });

        joinedAllTv.setVisibility(View.GONE);
        if (isOnboarding) {
            titleTv.setVisibility(View.GONE);
            descTv.setVisibility(View.GONE);
        } else {
            titleTv.setVisibility(View.VISIBLE);
            descTv.setVisibility(View.VISIBLE);
        }

        getFeedGroups();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).toggleSearchInToolbar(false);
        }
    }

    private void getFeedGroups() {
        Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        Call<List<FeedGroupData>> call = endpoints.getExploreFeedGroupList();
        call.enqueue(new Callback<List<FeedGroupData>>() {
            @Override
            public void onResponse(@NotNull Call<List<FeedGroupData>> call, @NotNull Response<List<FeedGroupData>> response) {
                feedGroupDataList = response.body();
                if (response.isSuccessful() && isAdded() && feedGroupDataList != null && !feedGroupDataList.isEmpty()) {
                    initRecyclerView();
                    if (isOnboarding) {
                        titleTv.setVisibility(View.GONE);
                        descTv.setVisibility(View.GONE);
                    } else {
                        titleTv.setVisibility(View.VISIBLE);
                        descTv.setVisibility(View.VISIBLE);
                    }
                } else if (isAdded()) {
                    if (groupRecyclerView.getActualAdapter() != groupRecyclerView.getAdapter()) {
                        // recycler view is currently holding shimmer adapter so hide it
                        groupRecyclerView.hideShimmerAdapter();
                    }
                    if (feedGroupDataList != null && feedGroupDataList.isEmpty()) {
                        if (isOnboarding) {
                            // exit the explore activity if opened during onboarding & there are no unjoined groups to be shown
                            // will happen if an existing user logs back in and has already joined every group
                            getActivity().finish();
                            getActivity().overridePendingTransition(R.anim.none, R.anim.slide_to_bottom);
                        } else {
                            joinedAllTv.setVisibility(View.VISIBLE);
                            titleTv.setVisibility(View.GONE);
                            descTv.setVisibility(View.GONE);
                            searchEt.setVisibility(View.GONE);
                            searchContainer.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<FeedGroupData>> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), R.string.all_something_wrong_try_again, Toast.LENGTH_SHORT).show();
                    if (groupRecyclerView.getActualAdapter() != groupRecyclerView.getAdapter()) {
                        // recycler view is currently holding shimmer adapter so hide it
                        groupRecyclerView.hideShimmerAdapter();
                    }
                }
            }
        });
    }

    private void initRecyclerView() {
        if (groupRecyclerView.getActualAdapter() != groupRecyclerView.getAdapter()) {
            // recycler view is currently holding shimmer adapter so hide it
            groupRecyclerView.hideShimmerAdapter();
        }
        adapter = new FeedGroupAdapter(feedGroupDataList, mListener, GlideApp.with(requireContext()), isOnboarding);
        groupRecyclerView.setAdapter(adapter);
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
    public void onListFragmentInteraction(String groupId, boolean isAdded) {
        if (getActivity() instanceof FeedGroupAdapter.OnListFragmentInteractionListener) {
            ((FeedGroupAdapter.OnListFragmentInteractionListener) getActivity()).onListFragmentInteraction(groupId, isAdded);
        }
    }

    @Override
    public void openGroup(FeedGroupData feedGroupData) {
        GroupFeedActivity.open(requireContext(), feedGroupData);
    }

}