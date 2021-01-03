package in.lubble.app.chat.stories;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

import in.lubble.app.R;

public class StoryRedirectLink extends AppCompatActivity {
    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_redirect_link);
        Intent intent = getIntent();
        String link = intent.getStringExtra("link");
        webView = findViewById(R.id.story_link_redirect_view);
        webView.loadUrl(link);
    }
}