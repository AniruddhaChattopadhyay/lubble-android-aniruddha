package in.lubble.app.leaderboard;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import in.lubble.app.BaseActivity;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.utils.FragUtils;

public class LeaderboardActivity extends BaseActivity {

    private static final String TAG = "LeaderboardActivity";

    private ImageView crossIv;

    public static void open(Context context) {
        Intent intent = new Intent(context, LeaderboardActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        Analytics.triggerScreenEvent(this, this.getClass());

        FragUtils.replaceFrag(getSupportFragmentManager(), LeaderboardFrag.newInstance(), R.id.frag_container);
        crossIv = findViewById(R.id.iv_cross);

        crossIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.none, R.anim.slide_to_bottom);
            }
        });
    }

}