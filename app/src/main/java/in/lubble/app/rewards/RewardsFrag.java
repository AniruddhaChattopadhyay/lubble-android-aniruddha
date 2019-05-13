package in.lubble.app.rewards;

import android.graphics.drawable.Drawable;
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
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.rewards.data.RewardsAirtableData;
import in.lubble.app.rewards.data.RewardsRecordData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class RewardsFrag extends Fragment {

    private static final String TAG = "RewardsFrag";

    private TextView noRewardsTv;
    private ShimmerRecyclerView shimmerRecyclerView;

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

        noRewardsTv = view.findViewById(R.id.tv_no_rewards);
        shimmerRecyclerView = view.findViewById(R.id.rv_rewards);
        shimmerRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

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
                        final String photoUrl = airtableData.getRecords().get(0).getFields().getPhoto();
                        GlideApp.with(requireContext()).load(photoUrl).diskCacheStrategy(DiskCacheStrategy.NONE).listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                shimmerRecyclerView.hideShimmerAdapter();
                                Toast.makeText(requireContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                shimmerRecyclerView.hideShimmerAdapter();
                                final List<RewardsRecordData> activeRewardList = new ArrayList<>();
                                for (RewardsRecordData reward : airtableData.getRecords()) {
                                    if (!reward.getFields().isExpired() && reward.getFields().isAvailable()) {
                                        activeRewardList.add(reward);
                                    }
                                }
                                shimmerRecyclerView.setAdapter(new RewardsAdapter(activeRewardList, GlideApp.with(requireContext())));
                                return false;
                            }
                        }).preload();
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

}
