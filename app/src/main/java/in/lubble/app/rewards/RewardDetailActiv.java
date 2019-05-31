package in.lubble.app.rewards;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.text.HtmlCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.crashlytics.android.Crashlytics;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import in.lubble.app.BaseActivity;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleApp;
import in.lubble.app.R;
import in.lubble.app.models.ProfileData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.referrals.ReferralActivity;
import in.lubble.app.rewards.data.RewardCodesAirtableData;
import in.lubble.app.rewards.data.RewardCodesData;
import in.lubble.app.rewards.data.RewardsAirtableData;
import in.lubble.app.rewards.data.RewardsData;
import in.lubble.app.utils.UiUtils;
import okhttp3.RequestBody;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingFormatArgumentException;

import static in.lubble.app.Constants.MEDIA_TYPE;
import static in.lubble.app.firebase.RealtimeDbHelper.getThisUserRef;
import static in.lubble.app.rewards.RewardsFrag.REQ_REWARD_REFRESH;

public class RewardDetailActiv extends BaseActivity {

    private static final String TAG = "RewardDetailActiv";
    private static final String ARG_REWARD_DATA = "ARG_REWARD_DATA";
    private static final String ARG_REWARD_CODE_DATA = "ARG_REWARD_CODE_DATA";

    private CoordinatorLayout rootview;
    private RewardsData rewardsData;
    private RewardCodesData rewardCodesData;
    private ImageView rewardIv;
    private ImageView logoIv;
    private TextView detailsTv;
    private TextView brandNameTv;
    private TextView titleTv;
    private TextView descTv;
    private TextView costTv;
    private TextView detailBrandName;
    private TextView detailTitleTv;
    private TextView detailDescTv;
    private TextView tncTv;
    private NestedScrollView bottomSheet;
    private BottomSheetBehavior bottomSheetBehavior;
    private TextView showDetailsTv;
    private MaterialButton getThisBtn;
    private MaterialButton detailGetThisBtn;
    private RelativeLayout rewardCodeContainer;
    private TextView rewardCodeTv;
    private TextView rewardClaimTv;
    private ProgressDialog progressDialog;
    private ImageView rewardClaimIv;

    public static void open(Fragment fragment, RewardsData rewardsData) {
        final Intent intent = new Intent(fragment.getContext(), RewardDetailActiv.class);
        intent.putExtra(ARG_REWARD_DATA, rewardsData);
        fragment.startActivityForResult(intent, REQ_REWARD_REFRESH);
    }

    public static void open(Context context, RewardCodesData rewardCodesData) {
        final Intent intent = new Intent(context, RewardDetailActiv.class);
        intent.putExtra(ARG_REWARD_CODE_DATA, rewardCodesData);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_detail);

        rootview = findViewById(R.id.rootview_reward_detail);
        rewardIv = findViewById(R.id.iv_reward);
        logoIv = findViewById(R.id.iv_logo);
        brandNameTv = findViewById(R.id.tv_brand_name);
        titleTv = findViewById(R.id.tv_reward_title);
        descTv = findViewById(R.id.tv_reward_desc);
        costTv = findViewById(R.id.tv_cost);
        detailBrandName = findViewById(R.id.tv_detail_brand_name);
        detailTitleTv = findViewById(R.id.tv_detail_title);
        detailDescTv = findViewById(R.id.tv_detail_desc);
        detailsTv = findViewById(R.id.tv_details);
        tncTv = findViewById(R.id.tv_tnc);
        bottomSheet = findViewById(R.id.bottomsheet_reward);
        showDetailsTv = findViewById(R.id.tv_show_details);
        getThisBtn = findViewById(R.id.btn_get_this);
        detailGetThisBtn = findViewById(R.id.btn_get_this_detail);
        rewardCodeContainer = findViewById(R.id.container_reward_code);
        rewardClaimTv = findViewById(R.id.tv_claimed_text);
        rewardCodeTv = findViewById(R.id.tv_reward_code);
        rewardClaimIv = findViewById(R.id.iv_claimed_icon);

        rewardsData = (RewardsData) getIntent().getSerializableExtra(ARG_REWARD_DATA);
        rewardCodesData = (RewardCodesData) getIntent().getSerializableExtra(ARG_REWARD_CODE_DATA);
        if (rewardsData == null && rewardCodesData == null) {
            Toast.makeText(this, "No Data Found", Toast.LENGTH_SHORT).show();
            Crashlytics.logException(new MissingFormatArgumentException("no rewards data found"));
            finish();
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        if (rewardsData == null) {
            fetchRewardsData();
        } else {
            initReward();
        }
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    showDetailsTv.setText("Hide Details");
                    showDetailsTv.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_keyboard_arrow_down_black_24dp, 0, 0);
                } else {
                    showDetailsTv.setText("Show Details");
                    showDetailsTv.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_keyboard_arrow_up_black_24dp, 0, 0);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        getThisBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCoinsConfirmation();
            }
        });

        detailGetThisBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCoinsConfirmation();
            }
        });
    }

    private void fetchRewardsData() {
        progressDialog.setTitle("Fetching your reward");
        progressDialog.setMessage(getText(R.string.all_please_wait));
        progressDialog.setCancelable(false);
        progressDialog.show();
        String formula = "RECORD_ID()=\'" + rewardCodesData.getRewardId().get(0) + "\'";
        String url = "https://api.airtable.com/v0/appbhSWmy7ZS6UeTy/Rewards?filterByFormula=AND(" + formula + ")&view=Grid%20view";

        final Endpoints endpoints = ServiceGenerator.createAirtableService(Endpoints.class);
        endpoints.fetchRewards(url).enqueue(new Callback<RewardsAirtableData>() {
            @Override
            public void onResponse(Call<RewardsAirtableData> call, Response<RewardsAirtableData> response) {
                final RewardsAirtableData airtableData = response.body();
                if (response.isSuccessful() && airtableData != null && !isFinishing()) {
                    if (airtableData.getRecords().size() == 1) {
                        progressDialog.dismiss();
                        rewardsData = airtableData.getRecords().get(0).getFields();
                        initReward();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(RewardDetailActiv.this, "Something went terribly wrong", Toast.LENGTH_SHORT).show();
                        Crashlytics.logException(new IllegalStateException("More than 1 records for reward code: " + rewardCodesData.getRewardRecordId()));
                        finish();
                    }

                } else {
                    if (!isFinishing()) {
                        progressDialog.dismiss();
                        Toast.makeText(RewardDetailActiv.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }

            @Override
            public void onFailure(Call<RewardsAirtableData> call, Throwable t) {
                if (!isFinishing()) {
                    progressDialog.dismiss();
                    Toast.makeText(RewardDetailActiv.this, R.string.check_internet, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onFailure: ");
                    finish();
                }
            }
        });
    }

    private void initReward() {
        rootview.setBackgroundColor(Color.parseColor("#" + rewardsData.getColor()));
        GlideApp.with(this).load(rewardsData.getDetailPhoto()).diskCacheStrategy(DiskCacheStrategy.NONE).into(rewardIv);
        GlideApp.with(this).load(rewardsData.getBrandLogo()).diskCacheStrategy(DiskCacheStrategy.NONE).into(logoIv);
        brandNameTv.setText(rewardsData.getBrand());
        detailBrandName.setText(rewardsData.getBrand());
        titleTv.setText(rewardsData.getTitle());
        detailTitleTv.setText(rewardsData.getTitle());
        descTv.setText(rewardsData.getDescription());
        detailDescTv.setText(rewardsData.getDescription());
        costTv.setText(rewardsData.getCost() + " el coins");

        detailsTv.setText(HtmlCompat.fromHtml(rewardsData.getDetails(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        detailsTv.setMovementMethod(LinkMovementMethod.getInstance());

        tncTv.setText(HtmlCompat.fromHtml(rewardsData.getTnc(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        tncTv.setMovementMethod(LinkMovementMethod.getInstance());

        if (rewardCodesData != null) {
            showRewardCode(rewardCodesData);
        } else {
            getThisBtn.setVisibility(View.VISIBLE);
            rewardCodeContainer.setVisibility(View.GONE);
        }
    }

    private void showCoinsConfirmation() {
        UiUtils.showBottomSheetAlertLight(this, getLayoutInflater(), "Spend " + rewardsData.getCost() + " el coins?", R.drawable.ic_coin, "Confirm", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                claimReward();
            }
        });
    }

    private void claimReward() {

        progressDialog.setTitle("Claiming Your Reward");
        progressDialog.setMessage(getText(R.string.all_please_wait));
        progressDialog.setCancelable(false);
        progressDialog.show();

        getThisUserRef().runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                ProfileData profileData = mutableData.getValue(ProfileData.class);
                if (profileData == null) {
                    return Transaction.success(mutableData);
                }

                if (profileData.getCoins() >= rewardsData.getCost()) {
                    // Set value and report transaction success
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("coins", profileData.getCoins() - rewardsData.getCost());
                    getThisUserRef().updateChildren(childUpdates);
                    return Transaction.success(mutableData);
                } else {
                    return Transaction.abort();
                }
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                // Transaction completed
                if (committed && !isFinishing()) {
                    fetchEmptyRewardCode();
                } else if (!isFinishing()) {
                    progressDialog.dismiss();
                    UiUtils.showBottomSheetAlertLight(RewardDetailActiv.this, getLayoutInflater(), "Not enough coins", R.drawable.ic_error_outline_black_24dp, "Earn More",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ReferralActivity.open(RewardDetailActiv.this, true);
                                    finish();
                                }
                            });
                }
            }
        });
    }

    private void fetchEmptyRewardCode() {
        setResult(RESULT_OK);
        String formula = "RewardId=\'" + rewardsData.getRecordId() + "\',Uid=\'\'";

        String url = "https://api.airtable.com/v0/appbhSWmy7ZS6UeTy/RewardCodes?filterByFormula=AND(" + formula + ")&view=Grid%20view&maxRecords=1";

        final Endpoints endpoints = ServiceGenerator.createAirtableService(Endpoints.class);
        endpoints.fetchRewardCodes(url).enqueue(new Callback<RewardCodesAirtableData>() {
            @Override
            public void onResponse(Call<RewardCodesAirtableData> call, Response<RewardCodesAirtableData> response) {
                final RewardCodesAirtableData airtableData = response.body();
                if (response.isSuccessful() && airtableData != null && !isFinishing()) {
                    if (airtableData.getRecords().size() > 0) {
                        uploadRewardClaim(airtableData.getRecords().get(0).getFields());
                    } else {
                        Crashlytics.logException(new IllegalStateException("trying to claim reward with no more codes left, reward id: " + rewardsData.getRecordId()));
                        Toast.makeText(RewardDetailActiv.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                        finish();
                    }

                } else {
                    if (!isFinishing()) {
                        progressDialog.dismiss();
                        Toast.makeText(RewardDetailActiv.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<RewardCodesAirtableData> call, Throwable t) {
                if (!isFinishing()) {
                    Toast.makeText(RewardDetailActiv.this, R.string.check_internet, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onFailure: ");
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void uploadRewardClaim(final RewardCodesData rewardCodesData) {

        HashMap<String, Object> params = new HashMap<>();
        HashMap<String, Object> fieldParams = new HashMap<>();
        fieldParams.put("Uid", FirebaseAuth.getInstance().getUid());
        params.put("fields", fieldParams);
        RequestBody body = RequestBody.create(MEDIA_TYPE, new JSONObject(params).toString());

        String url = "https://api.airtable.com/v0/appbhSWmy7ZS6UeTy/RewardCodes/" + rewardCodesData.getRewardRecordId();

        final Endpoints endpoints = ServiceGenerator.createAirtableService(Endpoints.class);
        endpoints.uploadRewardClaim(url, body).enqueue(new Callback<RewardsAirtableData>() {
            @Override
            public void onResponse(Call<RewardsAirtableData> call, Response<RewardsAirtableData> response) {
                final RewardsAirtableData airtableData = response.body();
                if (response.isSuccessful() && airtableData != null && !isFinishing()) {
                    progressDialog.dismiss();
                    if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                    showRewardCode(rewardCodesData);

                } else {
                    if (!isFinishing()) {
                        progressDialog.dismiss();
                        Toast.makeText(RewardDetailActiv.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<RewardsAirtableData> call, Throwable t) {
                if (!isFinishing()) {
                    progressDialog.dismiss();
                    Toast.makeText(RewardDetailActiv.this, R.string.check_internet, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onFailure: ");
                }
            }
        });
    }

    private void showRewardCode(final RewardCodesData rewardCodesData) {
        getThisBtn.setVisibility(View.GONE);
        detailGetThisBtn.setVisibility(View.GONE);
        rewardCodeContainer.setVisibility(View.VISIBLE);
        rewardCodeTv.setText(rewardCodesData.getRewardCode());

        if (rewardCodesData.getIsLink()) {
            rewardClaimTv.setText("Reward Link");
            rewardClaimIv.setImageResource(R.drawable.ic_open_in_new_black_24dp);
            rewardCodeContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(rewardCodesData.getRewardCode()));
                    startActivity(browserIntent);
                }
            });
        } else {
            rewardClaimTv.setText("Reward Code");
            rewardClaimIv.setImageResource(R.drawable.ic_content_copy_black_24dp);
            rewardCodeContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager) LubbleApp.getAppContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    String message = rewardCodesData.getRewardCode();
                    ClipData clip = ClipData.newPlainText("lubble_reward_code", message);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(RewardDetailActiv.this, "COPIED!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
