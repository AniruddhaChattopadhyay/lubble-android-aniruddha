package in.lubble.app.rewards;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.referrals.ReferralActivity;
import in.lubble.app.rewards.data.RewardsAirtableData;
import in.lubble.app.rewards.data.RewardsRecordData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class RewardsFrag extends Fragment {

    private static final String TAG = "RewardsFrag";
    static final int REQ_REWARD_REFRESH = 576;

    private TextView claimedRewardsTv;
    private TextView earnMoreTv;
    private TextView noRewardsTv;
    private ShimmerRecyclerView shimmerRecyclerView;
    private RewardsAdapter rewardsAdapter;

    public RewardsFrag() {
    }

    @SuppressWarnings("unused")
    public static RewardsFrag newInstance() {
        RewardsFrag fragment = new RewardsFrag();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_rewards, container, false);

        claimedRewardsTv = view.findViewById(R.id.tv_claimed_rewards);
        earnMoreTv = view.findViewById(R.id.tv_earn_more);
        noRewardsTv = view.findViewById(R.id.tv_no_rewards);
        shimmerRecyclerView = view.findViewById(R.id.rv_rewards);
        shimmerRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        claimedRewardsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClaimedRewardsActiv.open(requireContext());
            }
        });
        earnMoreTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ReferralActivity) getActivity()).openReferralFrag();
            }
        });

        LubbleSharedPrefs.getInstance().setIsRewardsOpened(true);

        rewardsAdapter = new RewardsAdapter(GlideApp.with(requireContext()), this);
        shimmerRecyclerView.setAdapter(rewardsAdapter);
        fetchRewards();

        return view;
    }

    private void fetchRewards() {
        shimmerRecyclerView.showShimmerAdapter();
        noRewardsTv.setVisibility(View.GONE);
        shimmerRecyclerView.setVisibility(View.VISIBLE);
        String formula = "LubbleId=\'" + LubbleSharedPrefs.getInstance().getLubbleId() + "\',FIND(\'" + FirebaseAuth.getInstance().getUid() + "\', ClaimedUids)=0";
        String url = "https://api.airtable.com/v0/appbhSWmy7ZS6UeTy/Rewards?filterByFormula=AND(" + formula + ")&view=Grid%20view";

        final Endpoints endpoints = ServiceGenerator.createAirtableService(Endpoints.class);
        endpoints.fetchRewards(url).enqueue(new Callback<RewardsAirtableData>() {
            @Override
            public void onResponse(Call<RewardsAirtableData> call, Response<RewardsAirtableData> response) {
                final RewardsAirtableData airtableData = response.body();
                if (response.isSuccessful() && airtableData != null && isAdded() && isVisible()) {
                    if (airtableData.getRecords().size() > 0) {
                        shimmerRecyclerView.hideShimmerAdapter();
                        final List<RewardsRecordData> activeRewardList = new ArrayList<>();
                        for (RewardsRecordData reward : airtableData.getRecords()) {
                            if (!reward.getFields().isExpired() && reward.getFields().isAvailable()) {
                                activeRewardList.add(reward);
                            }
                        }
                        if (!activeRewardList.isEmpty()) {
                            rewardsAdapter.setList(activeRewardList);
                        } else {
                            noRewardsTv.setVisibility(View.VISIBLE);
                        }
                    } else {
                        noRewardsTv.setVisibility(View.VISIBLE);
                        shimmerRecyclerView.hideShimmerAdapter();
                    }

                } else {
                    if (isAdded() && isVisible()) {
                        shimmerRecyclerView.hideShimmerAdapter();
                        Toast.makeText(requireContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<RewardsAirtableData> call, Throwable t) {
                if (isAdded() && isVisible()) {
                    Toast.makeText(requireContext(), R.string.check_internet, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onFailure: ");
                    shimmerRecyclerView.hideShimmerAdapter();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_REWARD_REFRESH && resultCode == Activity.RESULT_OK) {
            rewardsAdapter.clearAll();
            fetchRewards();
        }
    }
}
