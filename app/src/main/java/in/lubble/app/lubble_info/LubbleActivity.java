package in.lubble.app.lubble_info;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import in.lubble.app.BaseActivity;
import in.lubble.app.Constants;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.receivers.ShareSheetReceiver;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;

import static in.lubble.app.utils.ReferralUtils.generateBranchUrl;
import static in.lubble.app.utils.ReferralUtils.getReferralIntent;

public class LubbleActivity extends BaseActivity {

    private static final String TAG = "LubbleActivity";

    private ProgressBar progressBar;
    private WebView wikiWebView;

    public static void open(Context context) {
        context.startActivity(new Intent(context, LubbleActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lubble);

        Analytics.triggerScreenEvent(this, this.getClass());

        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(LubbleSharedPrefs.getInstance().getLubbleName());

        progressBar = findViewById(R.id.progressBar_lubbleInfo);
        wikiWebView = findViewById(R.id.webview_wiki);

        String wikiUrl = FirebaseRemoteConfig.getInstance().getString(Constants.WIKI_URL);
        Uri uri = Uri.parse(wikiUrl);
        if ("neighbourhoods".equalsIgnoreCase(uri.getLastPathSegment())) {
            // couldnt resolve apt nhood name from firebase remoteConfig, use redirects in WP instead
            // remoteConfig will fail for new users whose uid & lubble ID are just set in analytics but not yet synced with remoteConfig
            uri = uri.buildUpon().appendQueryParameter("lubble_id", LubbleSharedPrefs.getInstance().getLubbleId()).build();
        }

        wikiWebView.getSettings().setJavaScriptEnabled(true);
        wikiWebView.loadUrl(uri.toString());
        progressBar.setVisibility(View.VISIBLE);

        wikiWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(progress);
                if (progress == 100) {
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case R.id.action_wiki_share:

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, FirebaseRemoteConfig.getInstance().getString(Constants.WIKI_URL));

                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this, 21,
                        new Intent(this, ShareSheetReceiver.class),
                        PendingIntent.FLAG_UPDATE_CURRENT);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    startActivity(Intent.createChooser(intent, getString(R.string.refer_share_title), pendingIntent.getIntentSender()));
                } else {
                    startActivity(Intent.createChooser(intent, getString(R.string.refer_share_title)));
                }
                Analytics.triggerEvent(AnalyticsEvents.REFERRAL_WIKI_SHARE, this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.lubble_info_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

}
