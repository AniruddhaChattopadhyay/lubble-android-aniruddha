package in.lubble.app.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import in.lubble.app.BuildConfig;
import in.lubble.app.GlideApp;
import in.lubble.app.R;

public class FullScreenImageActivity extends AppCompatActivity {

    private static final String EXTRA_IMG_PATH = BuildConfig.APPLICATION_ID + "_EXTRA_IMG_PATH";

    public static void open(Activity activity, Context context, String imgPath, ImageView chatIv) {
        Intent intent = new Intent(context, FullScreenImageActivity.class);
        intent.putExtra(EXTRA_IMG_PATH, imgPath);
        Bundle bundle = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, chatIv, chatIv.getTransitionName()).toBundle();
        }
        context.startActivity(intent, bundle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        TouchImageView touchImageView = findViewById(R.id.tiv_fullscreen);

        if (getIntent() != null) {
            String imgPath = getIntent().getStringExtra(EXTRA_IMG_PATH);
            GlideApp.with(this)
                    .load(imgPath)
                    .fitCenter()
                    .into(touchImageView);
        }
    }

}
