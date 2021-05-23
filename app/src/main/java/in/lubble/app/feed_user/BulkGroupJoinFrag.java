package in.lubble.app.feed_user;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.MissingFormatArgumentException;

import in.lubble.app.LubbleApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.UploadImageFeedService;
import in.lubble.app.models.FeedGroupData;
import in.lubble.app.models.FeedPostData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.services.FeedServices;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;

import static android.app.Activity.RESULT_OK;
import static in.lubble.app.Constants.MEDIA_TYPE;

public class BulkGroupJoinFrag extends Fragment {

    private static final String ARG_POST_DATA = "LBL_ARG_POST_DATA";
    private static final String TAG = "BulkGroupJoin";
    private ShimmerRecyclerView groupsRv;
    private SearchView groupSv;
    private MaterialButton joinBtn;
    @Nullable
    private BulkGroupJoinAdapter bulkGroupJoinAdapter;
    private List<FeedGroupData> feedGroupDataList;
    private ProgressBar joinProgress;

    public static BulkGroupJoinFrag newInstance() {
       BulkGroupJoinFrag bulkGroupJoinFrag = new BulkGroupJoinFrag();
       return bulkGroupJoinFrag;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bulk_group_join, container, false);

        groupsRv = view.findViewById(R.id.rv_groups);
        groupSv = view.findViewById(R.id.sv_group_selection);
        joinBtn = view.findViewById(R.id.btn_join);
        joinProgress = view.findViewById(R.id.progressbar_post);

        groupsRv.setLayoutManager(new LinearLayoutManager(requireContext()));
        getFeedGroups();

//        if (getArguments() != null && getArguments().containsKey(ARG_POST_DATA)) {
//            feedPostData = (FeedPostData) getArguments().getSerializable(ARG_POST_DATA);
//        } else {
//            throw new MissingFormatArgumentException("no ARG_POST_DATA passed while opening BulkGroupJoin");
//        }

        groupSv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (bulkGroupJoinAdapter != null) {
                    bulkGroupJoinAdapter.filter(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (bulkGroupJoinAdapter != null) {
                    bulkGroupJoinAdapter.filter(newText);
                }
                return true;
            }
        });

        joinBtn.setOnClickListener(v -> {
            ArrayList<String> groupNamesList = new ArrayList<>();
            HashSet<Integer> groupSet = bulkGroupJoinAdapter.getLastCheckedPosSet();
            for(int pos: groupSet){
                groupNamesList.add(feedGroupDataList.get(pos).getFeedName());
            }
            joinBtn.setVisibility(View.GONE);
            joinProgress.setVisibility(View.VISIBLE);
            RequestBody body = RequestBody.create(MEDIA_TYPE, getParamsForGroupList(groupNamesList).toString());
            final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
            endpoints.batchFollowGroups(body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                    if (response.isSuccessful() && isAdded()) {
                        joinProgress.setVisibility(View.GONE);
                        Toast.makeText(LubbleApp.getAppContext(), "Groups Joined", Toast.LENGTH_SHORT).show();
                        LubbleSharedPrefs.getInstance().setCheckIfFeedGroupJoined();
                        FragmentManager fm = getActivity().getSupportFragmentManager();
                        fm.beginTransaction().replace(R.id.group_selection, FeedCombinedFragment.newInstance()).commitAllowingStateLoss();
                    } else {
                        if (isAdded()) {
                            joinProgress.setVisibility(View.GONE);
                            Toast.makeText(getContext(), response.message() == null ? getString(R.string.check_internet) : response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                    if (isAdded()) {
                       joinProgress.setVisibility(View.GONE);
                       Toast.makeText(getContext(), t.getMessage() == null ? getString(R.string.check_internet) : t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        return view;
    }

    private void getFeedGroups() {
        joinBtn.setEnabled(false);
        groupsRv.showShimmerAdapter();

        Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        Call<List<FeedGroupData>> call = endpoints.getExploreFeedGroupList();
        call.enqueue(new Callback<List<FeedGroupData>>() {
            @Override
            public void onResponse(@NotNull Call<List<FeedGroupData>> call, @NotNull Response<List<FeedGroupData>> response) {
                feedGroupDataList = response.body();
                if (groupsRv.getActualAdapter() != groupsRv.getAdapter()) {
                    // recycler view is currently holding shimmer adapter so hide it
                    groupsRv.hideShimmerAdapter();
                }
                if (response.isSuccessful() && isAdded() && feedGroupDataList != null && !feedGroupDataList.isEmpty()) {
                    bulkGroupJoinAdapter = new BulkGroupJoinAdapter(feedGroupDataList);
                    groupsRv.setAdapter(bulkGroupJoinAdapter);
                    joinBtn.setEnabled(true);
                } else if (isAdded()) {
                    Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<FeedGroupData>> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), R.string.all_something_wrong_try_again, Toast.LENGTH_SHORT).show();
                    if (groupsRv.getActualAdapter() != groupsRv.getAdapter()) {
                        // recycler view is currently holding shimmer adapter so hide it
                        groupsRv.hideShimmerAdapter();
                    }
                }
            }
        });
    }
    private JSONObject getParamsForGroupList(ArrayList<String> groupIdList) {
        HashMap<String, Object> params = new HashMap<>();

        JSONArray groupIdArray = new JSONArray();
        for (String id : groupIdList) {
            groupIdArray.put(id);
        }

        params.put("group_list", groupIdArray);
        return new JSONObject(params);
    }
}
