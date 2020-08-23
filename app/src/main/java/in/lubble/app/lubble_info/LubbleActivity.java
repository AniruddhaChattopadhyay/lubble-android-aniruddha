package in.lubble.app.lubble_info;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import in.lubble.app.BaseActivity;
import in.lubble.app.Constants;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;

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

        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(LubbleSharedPrefs.getInstance().getLubbleName());

        progressBar = findViewById(R.id.progressBar_lubbleInfo);
        wikiWebView = findViewById(R.id.webview_wiki);

        String wikiUrl = FirebaseRemoteConfig.getInstance().getString(Constants.WIKI_URL);

        wikiWebView.getSettings().setJavaScriptEnabled(true);
        wikiWebView.loadUrl(wikiUrl);
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
}
