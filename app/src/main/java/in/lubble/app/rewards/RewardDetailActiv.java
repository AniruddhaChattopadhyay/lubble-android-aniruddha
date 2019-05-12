package in.lubble.app.rewards;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.text.HtmlCompat;
import androidx.core.widget.NestedScrollView;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.crashlytics.android.Crashlytics;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import in.lubble.app.BaseActivity;
import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.chat.books.OrderDoneActiv;
import in.lubble.app.models.ProfileData;
import in.lubble.app.referrals.ReferralActivity;
import in.lubble.app.rewards.data.RewardsData;
import in.lubble.app.utils.UiUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.MissingFormatArgumentException;

import static in.lubble.app.firebase.RealtimeDbHelper.getThisUserRef;

public class RewardDetailActiv extends BaseActivity {

    private static final String TAG = "RewardDetailActiv";
    private static final String ARG_REWARD_DATA = "ARG_REWARD_DATA";

    private CoordinatorLayout rootview;
    private RewardsData rewardsData;
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
    private TextView showDetailsTv;
    private MaterialButton getThisBtn;
    private MaterialButton detailGetThisBtn;

    public static void open(Context context, RewardsData rewardsData) {
        final Intent intent = new Intent(context, RewardDetailActiv.class);
        intent.putExtra(ARG_REWARD_DATA, rewardsData);
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

        rewardsData = (RewardsData) getIntent().getSerializableExtra(ARG_REWARD_DATA);
        if (rewardsData == null) {
            Toast.makeText(this, "No Data Found", Toast.LENGTH_SHORT).show();
            Crashlytics.logException(new MissingFormatArgumentException("no rewards data found"));
            finish();
            return;
        }

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

        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

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

    private void showCoinsConfirmation() {
        UiUtils.showBottomSheetAlertLight(this, getLayoutInflater(), "Spend " + rewardsData.getCost() + " el coins?", R.drawable.ic_coin, "Confirm", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                claimReward();
            }
        });
    }

    private void claimReward() {
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
                    //todo progressDialog.dismiss();
                    OrderDoneActiv.open(RewardDetailActiv.this);
                    finish();
                } else if (!isFinishing()) {
                    //todo progressDialog.dismiss();
                    UiUtils.showBottomSheetAlertLight(RewardDetailActiv.this, getLayoutInflater(), "Not enough coins", R.drawable.ic_error_outline_black_24dp, "Earn More",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ReferralActivity.open(RewardDetailActiv.this);
                                    finish();
                                }
                            });
                }
            }
        });
    }

}
