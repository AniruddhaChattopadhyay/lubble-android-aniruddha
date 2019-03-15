package in.lubble.app.referrals;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import androidx.appcompat.widget.Toolbar;
import in.lubble.app.BaseActivity;
import in.lubble.app.R;
import in.lubble.app.utils.FragUtils;

public class ReferralActivity extends BaseActivity {
    private static final String TAG = "ReferralActivity";


    public static void open(Context context) {
        context.startActivity(new Intent(context, ReferralActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_referral);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set up the ViewPager with the sections adapter.
        FrameLayout container = findViewById(R.id.container);
        getSupportActionBar().setTitle("Invites");

        FragUtils.replaceFrag(getSupportFragmentManager(), ReferralsFragment.newInstance(), R.id.container);
    }

}
