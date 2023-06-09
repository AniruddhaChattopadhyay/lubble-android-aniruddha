package in.lubble.app.referrals;

import static in.lubble.app.firebase.RealtimeDbHelper.getThisUserRef;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.freshchat.consumer.sdk.Freshchat;
import com.freshchat.consumer.sdk.FreshchatMessage;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import in.lubble.app.BaseActivity;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.rewards.RewardsFrag;
import in.lubble.app.utils.FragUtils;

public class ReferralActivity extends BaseActivity {
    private static final String TAG = "ReferralActivity";

    private static final String ARG_OPEN_REFERRALS = "ARG_OPEN_REFERRALS";

    private ValueEventListener thisUserListener;
    private TextView myCoinsTv;

    public static void open(Context context, boolean openReferrals) {
        final Intent intent = new Intent(context, ReferralActivity.class);
        intent.putExtra(ARG_OPEN_REFERRALS, openReferrals);
        context.startActivity(intent);
    }

    public static void open(Context context) {
        final Intent intent = new Intent(context, ReferralActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_referral);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Rewards");

        Analytics.triggerScreenEvent(this, this.getClass());
        boolean isOpenReferrals = getIntent().getBooleanExtra(ARG_OPEN_REFERRALS, false);

        // Set up the ViewPager with the sections adapter.
        myCoinsTv = findViewById(R.id.tv_my_coins);
        FrameLayout container = findViewById(R.id.container);

        if (isOpenReferrals) {
            getSupportActionBar().setTitle("Referrals");
            FragUtils.replaceFrag(getSupportFragmentManager(), ReferralsFragment.newInstance(), R.id.container);
        } else {
            getSupportActionBar().setTitle("Invites");
            FragUtils.replaceFrag(getSupportFragmentManager(), RewardsFrag.newInstance(), R.id.container);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchCoins();
    }

    private void fetchCoins() {
        thisUserListener = getThisUserRef().child("coins").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final Long coins = dataSnapshot.getValue(Long.class);
                if (coins != null) {
                    myCoinsTv.setText(String.valueOf(coins));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void openReferralFrag() {
        FragUtils.addFrag(getSupportFragmentManager(), R.id.container, ReferralsFragment.newInstance());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (thisUserListener != null) {
            getThisUserRef().child("coins").removeEventListener(thisUserListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.referrals_menu, menu);
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
                FreshchatMessage FreshchatMessage = new FreshchatMessage().setTag("REFERRAL_HELP").setMessage("Please help me with Referrals");
                Freshchat.sendMessage(this, FreshchatMessage);
                Freshchat.showConversations(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
