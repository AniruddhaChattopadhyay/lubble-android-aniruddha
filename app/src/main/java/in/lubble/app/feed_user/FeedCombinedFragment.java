package in.lubble.app.feed_user;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.jetbrains.annotations.NotNull;

import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.feed_groups.FeedExploreActiv;
import in.lubble.app.feed_groups.FeedGroupsFrag;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static in.lubble.app.utils.UiUtils.reduceDragSensitivity;

public class FeedCombinedFragment extends Fragment {

    private static final int REQ_CODE_JOIN_GROUPS = 628;

    private SwipeRefreshLayout.OnRefreshListener feedRefreshListener;

    public FeedCombinedFragment() {
        // Required empty public constructor
    }

    public static FeedCombinedFragment newInstance() {
        return new FeedCombinedFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.frag_feed_combined, container, false);
        TabLayout tabLayout = view.findViewById(R.id.tabLayout_feed);
        ViewPager2 viewPager = view.findViewById(R.id.tab_pager);

        MyTabPagerAdapter tabPager = new MyTabPagerAdapter(requireActivity());
        viewPager.setAdapter(tabPager);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) ->
                        tab.setText(position == 0 ? "Nearby Feed" : "Groups")
        ).attach();
        reduceDragSensitivity(viewPager);

        if (!LubbleSharedPrefs.getInstance().getCheckIfFeedGroupJoined()) {
            // User might not have joined any feed groups, check with backend
            fetchNewFeedUserStatus();
        }

        return view;
    }

    public void fetchNewFeedUserStatus() {
        final ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setTitle("Setting up Nearby Feed for you...");
        progressDialog.setMessage(getString(R.string.all_please_wait));
        progressDialog.setCancelable(false);
        progressDialog.show();

        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.checkIfGroupJoined().enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                if (response.isSuccessful() && isAdded()) {
                    progressDialog.dismiss();
                    String message = response.body();
                    if (message != null && message.equals("New User")) {
                        startActivityForResult(FeedExploreActiv.getIntent(requireContext(), true), REQ_CODE_JOIN_GROUPS);
                    } else {
                        LubbleSharedPrefs.getInstance().setCheckIfFeedGroupJoined();
                    }
                } else if (isAdded()) {
                    progressDialog.dismiss();
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Failed to set up Feed")
                            .setMessage("Error: " + (response.message() == null ? getString(R.string.check_internet) : response.message()))
                            .setPositiveButton(R.string.all_retry, (dialog, which) -> fetchNewFeedUserStatus())
                            .setCancelable(false)
                            .show();
                }
            }

            @Override
            public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                if (isAdded()) {
                    progressDialog.dismiss();
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Failed to set up Feed")
                            .setMessage("Error: " + (t.getMessage() == null ? getString(R.string.check_internet) : t.getMessage()))
                            .setPositiveButton(R.string.all_retry, (dialog, which) -> fetchNewFeedUserStatus())
                            .setCancelable(false)
                            .show();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_JOIN_GROUPS && resultCode == RESULT_OK
                && feedRefreshListener != null) {
            feedRefreshListener.onRefresh();
        }
    }

    void setRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        this.feedRefreshListener = listener;
    }

    static class MyTabPagerAdapter extends FragmentStateAdapter {
        MyTabPagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public int getItemCount() {
            return 2;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new FeedFrag();
                case 1:
                    return new FeedGroupsFrag();
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

}