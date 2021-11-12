package in.lubble.app.feed_user;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.jetbrains.annotations.NotNull;

import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.feed_groups.FeedExploreActiv;
import in.lubble.app.feed_groups.FeedGroupsFrag;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.utils.UiUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static in.lubble.app.utils.UiUtils.reduceDragSensitivity;

public class FeedCombinedFragment extends Fragment {

    private static final int REQ_CODE_JOIN_GROUPS = 628;
    private static final String TAG = "FeedCombinedFragment";

    private SwipeRefreshLayout.OnRefreshListener feedRefreshListener;
    private ViewPager2 viewPager;
    private LinearLayout postBtnsLL;
    private MyTabPagerAdapter tabPager;

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

        viewPager = view.findViewById(R.id.tab_pager);
        postBtnsLL = view.findViewById(R.id.post_btn_LL);

        tabPager = new MyTabPagerAdapter(getChildFragmentManager(), getLifecycle());
        viewPager.setAdapter(tabPager);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) ->
                        tab.setText(position == 0 ? "Nearby Feed" : "Groups")
        ).attach();
        reduceDragSensitivity(viewPager);
        viewPager.registerOnPageChangeCallback(pageChangeListener);

        if (!LubbleSharedPrefs.getInstance().getCheckIfFeedGroupJoined()) {
            // User might not have joined any feed groups, check with backend
            fetchNewFeedUserStatus();
        }

        LubbleSharedPrefs.getInstance().setIsFeedVisited(true);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).toggleSearchInToolbar(false);
        }
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
                        startActivityForResult(FeedExploreActiv.getIntent(requireContext(), true, true), REQ_CODE_JOIN_GROUPS);
                    } else {
                        LubbleSharedPrefs.getInstance().setCheckIfFeedGroupJoined();
                    }
                } else if (isAdded()) {
                    progressDialog.dismiss();
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Failed to set up Feed")
                            .setMessage("Error: " + response.message())
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

    void setCurrentTabPos(int pos) {
        if (pos < 0 || pos >= tabPager.getItemCount()) {
            Log.e(TAG, "setCurrentTabPos: invalid pos");
            return;
        }
        viewPager.setCurrentItem(pos);
    }

    ViewPager2.OnPageChangeCallback pageChangeListener = new ViewPager2.OnPageChangeCallback() {
        /**
         * This method will be invoked when a new page becomes selected. Animation is not
         * necessarily complete.
         *
         * @param position Position index of the new selected page.
         */
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            if (position == 1) {
                UiUtils.animateSlideDownHide(getContext(), postBtnsLL);
            } else {
                UiUtils.animateSlideUpShow(getContext(), postBtnsLL);
            }
        }
    };

    static class MyTabPagerAdapter extends FragmentStateAdapter {
        MyTabPagerAdapter(FragmentManager fm, Lifecycle lifecycle) {
            super(fm, lifecycle);
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