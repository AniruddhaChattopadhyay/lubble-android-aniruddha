package in.lubble.app.rewards;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.freshchat.consumer.sdk.Freshchat;
import com.freshchat.consumer.sdk.FreshchatMessage;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Collections;
import java.util.Comparator;

import in.lubble.app.BaseActivity;
import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.rewards.data.RewardCodesAirtableData;
import in.lubble.app.rewards.data.RewardCodesRecordData;
import in.lubble.app.utils.mapUtils.MathUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClaimedRewardsActiv extends BaseActivity {

    private static final String TAG = "ClaimedRewardsActiv";

    private LinearLayout noRewardsContainer;
    private ShimmerRecyclerView shimmerRecyclerView;

    public static void open(Context context) {
        context.startActivity(new Intent(context, ClaimedRewardsActiv.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claimed_rewards);

        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("My Claimed Rewards");
        Analytics.triggerScreenEvent(this, this.getClass());

        noRewardsContainer = findViewById(R.id.container_no_claimed_rewards);
        shimmerRecyclerView = findViewById(R.id.rv_claimed_rewards);
        shimmerRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchClaimedRewards();
    }

    private void fetchClaimedRewards() {
        shimmerRecyclerView.showShimmerAdapter();

        String formula = "Uid=\'" + FirebaseAuth.getInstance().getUid() + "\'";
        String url = "https://api.airtable.com/v0/appbhSWmy7ZS6UeTy/RewardCodes?filterByFormula=" + formula + "&view=Grid%20view";

        final Endpoints endpoints = ServiceGenerator.createAirtableService(Endpoints.class);
        endpoints.fetchRewardCodes(url).enqueue(new Callback<RewardCodesAirtableData>() {
            @Override
            public void onResponse(Call<RewardCodesAirtableData> call, Response<RewardCodesAirtableData> response) {
                final RewardCodesAirtableData airtableData = response.body();
                if (response.isSuccessful() && airtableData != null && !isFinishing()) {
                    if (airtableData.getRecords().size() > 0) {
                        Collections.sort(airtableData.getRecords(), new Comparator<RewardCodesRecordData>() {
                            @Override
                            public int compare(RewardCodesRecordData o1, RewardCodesRecordData o2) {
                                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                                return MathUtil.compareDesc(o2.getFields().getCreatedTimeInLong(), o1.getFields().getCreatedTimeInLong());
                            }
                        });
                        final String photoUrl = airtableData.getRecords().get(0).getFields().getPhoto().get(0);
                        GlideApp.with(ClaimedRewardsActiv.this).load(photoUrl).diskCacheStrategy(DiskCacheStrategy.NONE).listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                if (!isFinishing()) {
                                    shimmerRecyclerView.hideShimmerAdapter();
                                    shimmerRecyclerView.setAdapter(new ClaimedRewardsAdapter(airtableData.getRecords(), GlideApp.with(ClaimedRewardsActiv.this)));
                                }
                                return false;
                            }

                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                if (!isFinishing()) {
                                    shimmerRecyclerView.hideShimmerAdapter();
                                    Toast.makeText(ClaimedRewardsActiv.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                                return false;
                            }
                        }).preload();
                    } else {
                        noRewardsContainer.setVisibility(View.VISIBLE);
                        shimmerRecyclerView.hideShimmerAdapter();
                    }

                } else {
                    if (!isFinishing()) {
                        shimmerRecyclerView.hideShimmerAdapter();
                        Toast.makeText(ClaimedRewardsActiv.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<RewardCodesAirtableData> call, Throwable t) {
                if (!isFinishing()) {
                    Toast.makeText(ClaimedRewardsActiv.this, R.string.check_internet, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onFailure: ");
                    shimmerRecyclerView.hideShimmerAdapter();
                    finish();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_item_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_help:
                FreshchatMessage FreshchatMessage = new FreshchatMessage().setTag("REWARDS_HELP").setMessage("Please help me with Rewards");
                Freshchat.sendMessage(this, FreshchatMessage);
                Freshchat.showConversations(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
