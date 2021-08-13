package in.lubble.app.leaderboard;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

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

        // change status bar color
        Window window = getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.dk_colorAccent));

    }

}