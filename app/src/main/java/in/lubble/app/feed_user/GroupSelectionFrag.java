package in.lubble.app.feed_user;

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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.MissingFormatArgumentException;

import in.lubble.app.R;
import in.lubble.app.UploadImageFeedService;
import in.lubble.app.UploadVideoFeedService;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.models.FeedGroupData;
import in.lubble.app.models.FeedPostData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.services.FeedServices;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

public class GroupSelectionFrag extends Fragment {

    private static final String ARG_POST_DATA = "LBL_ARG_POST_DATA";
    private static final String ARG_IS_QnA = "LBL_ARG_IS_QnA";
    private static final String TAG = "GroupSelectionFrag";

    private ShimmerRecyclerView groupsRv;
    private SearchView groupSv;
    private MaterialButton postSubmitBtn;
    private FeedPostData feedPostData;
    @Nullable
    private GroupSelectionAdapter groupSelectionAdapter;
    private List<FeedGroupData> feedGroupDataList,exploreGroupDataList;
    private ProgressBar postProgressBar;
    private boolean isQnA;

    public static GroupSelectionFrag newInstance(FeedPostData feedPostData,boolean isQnA) {
        GroupSelectionFrag groupSelectionFrag = new GroupSelectionFrag();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_POST_DATA, feedPostData);
        bundle.putSerializable(ARG_IS_QnA,isQnA);
        groupSelectionFrag.setArguments(bundle);
        return groupSelectionFrag;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_group_selection, container, false);

        groupsRv = view.findViewById(R.id.rv_groups);
        groupSv = view.findViewById(R.id.sv_group_selection);
        postSubmitBtn = view.findViewById(R.id.btn_post);
        postProgressBar = view.findViewById(R.id.progressbar_post);

        Analytics.triggerScreenEvent(requireContext(), this.getClass());

        groupsRv.setLayoutManager(new LinearLayoutManager(requireContext()));
        getFeedGroups();

        if (getArguments() != null && getArguments().containsKey(ARG_POST_DATA)) {
            feedPostData = (FeedPostData) getArguments().getSerializable(ARG_POST_DATA);
        } else {
            throw new MissingFormatArgumentException("no ARG_POST_DATA passed while opening GroupSelectionFrag");
        }

        if (getArguments() != null && getArguments().containsKey(ARG_IS_QnA)) {
            isQnA = (boolean) getArguments().getSerializable(ARG_IS_QnA);
        } else {
            throw new MissingFormatArgumentException("no ARG_IS_QnA passed while opening GroupSelectionFrag");
        }

        groupSv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (groupSelectionAdapter != null) {
                    groupSelectionAdapter.filter(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (groupSelectionAdapter != null) {
                    groupSelectionAdapter.filter(newText);
                }
                return true;
            }
        });

        postSubmitBtn.setOnClickListener(v -> {
            String text = feedPostData.getText();
//            int lastCheckedPos = groupSelectionAdapter.getLastCheckedPos();
            FeedGroupData selectedGroupData = groupSelectionAdapter.getLastSelectedGroup();
            if (selectedGroupData == null) {
                Toast.makeText(requireContext(), "Please select a group for this post", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean isGroupJoined = selectedGroupData.isGroupJoined();
            String groupNameText = selectedGroupData.getName();
            String feedNameText = selectedGroupData.getFeedName();
            String uploadPath = "feed_photos";
            if (text != null) {
                if (feedPostData.getImgUri() != null) {
                    Uri imgUri = Uri.parse(feedPostData.getImgUri());
                    Intent serviceIntent = new Intent(getContext(), UploadImageFeedService.class)
                            .putExtra(UploadImageFeedService.EXTRA_BUCKET, UploadImageFeedService.BUCKET_DEFAULT)
                            .putExtra(UploadImageFeedService.EXTRA_FILE_NAME, imgUri.getLastPathSegment())
                            .putExtra(UploadImageFeedService.EXTRA_FILE_URI, imgUri)
                            .putExtra(UploadImageFeedService.EXTRA_UPLOAD_PATH, uploadPath)
                            .putExtra(UploadImageFeedService.EXTRA_FEED_GROUP_NAME, groupNameText)
                            .putExtra(UploadImageFeedService.EXTRA_FEED_FEED_NAME, feedNameText)
                            .putExtra(UploadImageFeedService.EXTRA_FEED_IS_GROUP_JOINED, isGroupJoined)
                            .putExtra(UploadImageFeedService.EXTRA_FEED_POST_DATA, feedPostData)
                            .setAction(UploadImageFeedService.ACTION_UPLOAD);
                    ContextCompat.startForegroundService(requireContext(), serviceIntent);
                    requireActivity().setResult(RESULT_OK);
                    requireActivity().finish();
                    getActivity().overridePendingTransition(R.anim.none, R.anim.slide_to_bottom_fast);
                }
                else if(feedPostData.getVidUri() != null){
                    uploadPath = "feed_videos";
                    Uri vidUri = Uri.parse(feedPostData.getVidUri());
                    Intent serviceIntent = new Intent(getContext(), UploadVideoFeedService.class)
                            .putExtra(UploadVideoFeedService.EXTRA_BUCKET, UploadVideoFeedService.BUCKET_DEFAULT)
                            .putExtra(UploadVideoFeedService.EXTRA_FILE_NAME, vidUri.getLastPathSegment())
                            .putExtra(UploadVideoFeedService.EXTRA_FILE_URI, vidUri)
                            .putExtra(UploadVideoFeedService.EXTRA_UPLOAD_PATH, uploadPath)
                            .putExtra(UploadVideoFeedService.EXTRA_FEED_GROUP_NAME, groupNameText)
                            .putExtra(UploadVideoFeedService.EXTRA_FEED_FEED_NAME, feedNameText)
                            .putExtra(UploadVideoFeedService.EXTRA_FEED_IS_GROUP_JOINED, isGroupJoined)
                            .putExtra(UploadVideoFeedService.EXTRA_FEED_POST_DATA, feedPostData)
                            .setAction(UploadVideoFeedService.ACTION_UPLOAD);
                    ContextCompat.startForegroundService(requireContext(), serviceIntent);
                    requireActivity().setResult(RESULT_OK);
                    requireActivity().finish();
                    getActivity().overridePendingTransition(R.anim.none, R.anim.slide_to_bottom_fast);
                }
                else {
                    postSubmitBtn.setVisibility(View.GONE);
                    postProgressBar.setVisibility(View.VISIBLE);
                    FeedServices.post(feedPostData, groupNameText, feedNameText, null,null, 0, isGroupJoined,new Callback<Void>() {
                        @Override
                        public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                            if (isAdded() && response.isSuccessful()) {
                                requireActivity().setResult(RESULT_OK);
                                requireActivity().finish();
                                getActivity().overridePendingTransition(R.anim.none, R.anim.slide_to_bottom_fast);
                            } else if (isAdded()) {
                                postProgressBar.setVisibility(View.GONE);
                                Snackbar.make(getView(), "Failed: " + response.message(), Snackbar.LENGTH_SHORT);
                            }
                        }

                        @Override
                        public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                            if (isAdded()) {
                                postProgressBar.setVisibility(View.GONE);
                                Snackbar.make(getView(), "Failed: " + t.getMessage(), Snackbar.LENGTH_SHORT);
                            }
                        }
                    });
                }
            }
        });

        return view;
    }

    private void getFeedGroups() {
        postSubmitBtn.setEnabled(false);
        groupsRv.showShimmerAdapter();

        Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        Call<List<FeedGroupData>> call = endpoints.getAllFeedGroupList();
        call.enqueue(new Callback<List<FeedGroupData>>() {
            @Override
            public void onResponse(@NotNull Call<List<FeedGroupData>> call, @NotNull Response<List<FeedGroupData>> response) {
                feedGroupDataList = response.body();
                if (groupsRv.getActualAdapter() != groupsRv.getAdapter()) {
                    // recycler view is currently holding shimmer adapter so hide it
                    groupsRv.hideShimmerAdapter();
                }
                if (response.isSuccessful() && isAdded() && feedGroupDataList != null && !feedGroupDataList.isEmpty()) {
                    groupSelectionAdapter = new GroupSelectionAdapter(feedGroupDataList,postSubmitBtn,isQnA);
                    groupsRv.setAdapter(groupSelectionAdapter);
                    postSubmitBtn.setEnabled(true);
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

}