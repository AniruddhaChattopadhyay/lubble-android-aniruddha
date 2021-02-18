package in.lubble.app.utils;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.io.Serializable;
import java.util.ArrayList;

import in.lubble.app.BuildConfig;
import in.lubble.app.R;
import in.lubble.app.chat.horizontalImageRecyclerView.MyDividerItemDecoration;
import in.lubble.app.chat.horizontalImageRecyclerView.RecyclerTouchListener;
import in.lubble.app.chat.multi_image_chat_gridview.MultiImageGridViewAdapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class FullScreenMultiImageActivity extends AppCompatActivity {

    private static final String EXTRA_IMG_PATH = BuildConfig.APPLICATION_ID + "_EXTRA_IMG_PATH";
    private static final String EXTRA_UPLOAD_PATH = BuildConfig.APPLICATION_ID + "_EXTRA_UPLOAD_PATH";
    private static final String EXTRA_ERROR_PIC = BuildConfig.APPLICATION_ID + "_EXTRA_ERROR_PIC";
    private static final String EXTRA_MULTI_IMG_PATH = "EXTRA_MULTI_IMG_PATH";
    private RecyclerView recyclerView;

    public static void open(Activity activity, Context context, ArrayList<String> imgPath, @Nullable String uploadPath, @DrawableRes int errorPic) {
        Intent intent = new Intent(context, FullScreenMultiImageActivity.class);
        intent.putExtra(EXTRA_IMG_PATH, imgPath);
        intent.putExtra(EXTRA_UPLOAD_PATH, uploadPath);
        intent.putExtra(EXTRA_ERROR_PIC, errorPic);
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_MULTI_IMG_PATH,imgPath);
        intent.putExtra("BUNDLE",bundle);
        bundle = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle();
        }
        context.startActivity(intent,bundle);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_multi_image);
        recyclerView = findViewById(R.id.multi_img_recylerview_fullscreen);
        Bundle args = getIntent().getBundleExtra("BUNDLE");
        final ArrayList<String> multiImageList = (ArrayList<String>) args.getSerializable(EXTRA_MULTI_IMG_PATH);
        MultiImageGridViewAdapter mAdapter = new MultiImageGridViewAdapter(this,multiImageList,false);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL,10));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, this.recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                //TODO

            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }
}