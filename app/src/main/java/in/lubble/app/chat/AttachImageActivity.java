package in.lubble.app.chat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.ArrayList;

import in.lubble.app.BaseActivity;
import in.lubble.app.BuildConfig;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.UploadFileService;
import in.lubble.app.UploadMultipleFileService;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.chat.horizontalImageRecyclerView.MultiImageAttachmentAdapter;
import in.lubble.app.chat.horizontalImageRecyclerView.MyDividerItemDecoration;
import in.lubble.app.chat.horizontalImageRecyclerView.RecyclerTouchListener;
import in.lubble.app.utils.TouchImageView;

import static in.lubble.app.UploadFileService.EXTRA_FILE_URI;

public class AttachImageActivity extends BaseActivity {

    private static final String EXTRA_IMG_PATH = BuildConfig.APPLICATION_ID + "_NEW_IMG_PATH";
    private static final String EXTRA_MULTI_IMG_PATH = BuildConfig.APPLICATION_ID + "_NEW_MULTI_IMG_PATH";
    private static final String EXTRA_GROUP_ID = BuildConfig.APPLICATION_ID + "_NEW_IMG_GROUP_ID";
    private static final String EXTRA_CAPTION = BuildConfig.APPLICATION_ID + "_CAPTION";
    private static final String EXTRA_IS_DM = BuildConfig.APPLICATION_ID + "_IS_DM";
    private static final String EXTRA_AUTHOR_ID = BuildConfig.APPLICATION_ID + "_AUTHOR_ID";
    private static final String EXTRA_IS_AUTHOR_SELLER = BuildConfig.APPLICATION_ID + "_IS_AUTHOR_SELLER";

    private TouchImageView touchImageView;
    private EditText captionEt;
    private ImageView sendIcon;
    private RecyclerView recyclerView;
    private MultiImageAttachmentAdapter mAdapter;
    private ArrayList<Uri> imageUriList = null;

    public static void open(Context context, Uri imgUri, String groupId, @Nullable String caption, boolean isDm, boolean isAuthorSeller, String authorId) {
        context.startActivity(getIntent(context, imgUri, groupId, caption, isDm, isAuthorSeller, authorId));
    }
    public static void open(Context context, ArrayList<Uri> imgUri, String groupId, @Nullable String caption, boolean isDm, boolean isAuthorSeller, String authorId) {
        context.startActivity(getIntent(context, imgUri, groupId, caption, isDm, isAuthorSeller, authorId));
    }
    public static Intent getIntent(Context context, Uri imgUri, String groupId, @Nullable String caption, boolean isDm, boolean isAuthorSeller, String authorId) {
        Intent intent = new Intent(context, AttachImageActivity.class);
        intent.putExtra(EXTRA_IMG_PATH, imgUri);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        intent.putExtra(EXTRA_CAPTION, caption);
        intent.putExtra(EXTRA_IS_DM, isDm);
        intent.putExtra(EXTRA_AUTHOR_ID, authorId);
        intent.putExtra(EXTRA_IS_AUTHOR_SELLER, isAuthorSeller);
        return intent;
    }

    public static Intent getIntent(Context context, ArrayList<Uri> imgUri, String groupId, @Nullable String caption, boolean isDm, boolean isAuthorSeller, String authorId) {
        Intent intent = new Intent(context, AttachImageActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        intent.putExtra(EXTRA_CAPTION, caption);
        intent.putExtra(EXTRA_IS_DM, isDm);
        intent.putExtra(EXTRA_AUTHOR_ID, authorId);
        intent.putExtra(EXTRA_IS_AUTHOR_SELLER, isAuthorSeller);
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_MULTI_IMG_PATH,(Serializable)imgUri);
        intent.putExtra("BUNDLE",args);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attach_image);

        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("");

        touchImageView = findViewById(R.id.tiv_new_img);
        captionEt = findViewById(R.id.et_img_caption);
        sendIcon = findViewById(R.id.iv_send_btn);
        final Uri imgUri = getIntent().getParcelableExtra(EXTRA_IMG_PATH);
        final String chatId = getIntent().getStringExtra(EXTRA_GROUP_ID);
        final String caption = getIntent().getStringExtra(EXTRA_CAPTION);

        Bundle args = getIntent().getBundleExtra("BUNDLE");
        if(args!=null)
            imageUriList = (ArrayList<Uri>) args.getSerializable(EXTRA_MULTI_IMG_PATH);

        captionEt.setText(caption);


        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_multi_img);

        mAdapter = new MultiImageAttachmentAdapter(this,imageUriList);

        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);

        recyclerView.setLayoutManager(mLayoutManager);

        // adding inbuilt divider line
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.HORIZONTAL,10));

        // adding custom divider line with padding 16dp
        // recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.HORIZONTAL, 16));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(mAdapter);
        GlideApp.with(getApplicationContext()).load(imageUriList.get(0)).fitCenter().into(touchImageView);

        // row click listener
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Uri uri = imageUriList.get(position);
                GlideApp.with(getApplicationContext()).load(uri).fitCenter().into(touchImageView);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
//        if(imageUriList!=null)
//            GlideApp.with(this).load(imageUriList.get(0)).fitCenter().into(touchImageView);
//        else
//            GlideApp.with(this).load(imgUri).fitCenter().into(touchImageView);

        sendIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean isDm = getIntent().getBooleanExtra(EXTRA_IS_DM, false);
                String uploadPath = "lubbles/" + LubbleSharedPrefs.getInstance().requireLubbleId() + "/groups/" + chatId;
                if (isDm) {
                    uploadPath = "dms/" + chatId;
                }
                Intent intent;
                if(imgUri!=null){
                    intent = new Intent(AttachImageActivity.this, UploadFileService.class)
                            .putExtra(UploadFileService.EXTRA_BUCKET, UploadFileService.BUCKET_CONVO)
                            .putExtra(UploadFileService.EXTRA_UPLOAD_PATH, uploadPath)
                            .putExtra(UploadFileService.EXTRA_CAPTION, captionEt.getText().toString())
                            .putExtra(UploadFileService.EXTRA_CHAT_ID, chatId)
                            .putExtra(UploadFileService.EXTRA_IS_DM, isDm)
                            .putExtra(UploadFileService.EXTRA_AUTHOR_ID, getIntent().getStringExtra(EXTRA_AUTHOR_ID))
                            .putExtra(UploadFileService.EXTRA_IS_AUTHOR_SELLER, getIntent().getBooleanExtra(EXTRA_IS_AUTHOR_SELLER, false))
                            .putExtra(UploadFileService.EXTRA_FILE_NAME, imgUri.getLastPathSegment()).putExtra(EXTRA_FILE_URI, imgUri)
                            .setAction(UploadFileService.ACTION_UPLOAD);
                }
                else {
                    ArrayList<String> fileNameList = new ArrayList<>();
                    for(int i=0;i<imageUriList.size();i++){
                        fileNameList.add(imageUriList.get(i).getLastPathSegment());
                    }
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(UploadMultipleFileService.EXTRA_MULTI_FILE_URI,(Serializable)imageUriList);
                    bundle.putSerializable(UploadMultipleFileService.EXTRA_MULTI_FILE_NAME,(Serializable)fileNameList);
                    intent = new Intent(AttachImageActivity.this, UploadMultipleFileService.class)
                            .putExtra(UploadMultipleFileService.EXTRA_BUCKET, UploadMultipleFileService.BUCKET_CONVO)
                            .putExtra(UploadMultipleFileService.EXTRA_UPLOAD_PATH, uploadPath)
                            .putExtra(UploadMultipleFileService.EXTRA_CAPTION, captionEt.getText().toString())
                            .putExtra(UploadMultipleFileService.EXTRA_CHAT_ID, chatId)
                            .putExtra(UploadMultipleFileService.EXTRA_IS_DM, isDm)
                            .putExtra(UploadMultipleFileService.EXTRA_AUTHOR_ID, getIntent().getStringExtra(EXTRA_AUTHOR_ID))
                            .putExtra(UploadMultipleFileService.EXTRA_IS_AUTHOR_SELLER, getIntent().getBooleanExtra(EXTRA_IS_AUTHOR_SELLER, false))
                            .putExtra("BUNDLE",bundle)
                            .setAction(UploadMultipleFileService.ACTION_UPLOAD);
                }
                startService(intent);
                final Bundle bundle = new Bundle();
                bundle.putString("group_id", chatId);
                Analytics.triggerEvent(AnalyticsEvents.SEND_GROUP_CHAT, bundle, AttachImageActivity.this);
                setResult(RESULT_OK);
                finish();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
