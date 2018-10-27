package in.lubble.app.chat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.widget.Toolbar;
import in.lubble.app.*;
import in.lubble.app.utils.TouchImageView;

import static in.lubble.app.UploadFileService.EXTRA_FILE_URI;
import static in.lubble.app.utils.UserUtils.getLubbleId;

public class AttachImageActivity extends BaseActivity {

    private static final String EXTRA_IMG_PATH = BuildConfig.APPLICATION_ID + "_NEW_IMG_PATH";
    private static final String EXTRA_GROUP_ID = BuildConfig.APPLICATION_ID + "_NEW_IMG_GROUP_ID";
    private static final String EXTRA_IS_DM = BuildConfig.APPLICATION_ID + "_IS_DM";
    private static final String EXTRA_AUTHOR_ID = BuildConfig.APPLICATION_ID + "_AUTHOR_ID";
    private static final String EXTRA_IS_AUTHOR_SELLER = BuildConfig.APPLICATION_ID + "_IS_AUTHOR_SELLER";

    private TouchImageView touchImageView;
    private EditText captionEt;
    private ImageView sendIcon;

    public static void open(Context context, Uri imgUri, String groupId, boolean isDm, boolean isAuthorSeller, String authorId) {
        Intent intent = new Intent(context, AttachImageActivity.class);
        intent.putExtra(EXTRA_IMG_PATH, imgUri);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        intent.putExtra(EXTRA_IS_DM, isDm);
        intent.putExtra(EXTRA_AUTHOR_ID, authorId);
        intent.putExtra(EXTRA_IS_AUTHOR_SELLER, isAuthorSeller);
        context.startActivity(intent);
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

        GlideApp.with(this).load(imgUri).fitCenter().into(touchImageView);

        sendIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean isDm = getIntent().getBooleanExtra(EXTRA_IS_DM, false);
                String uploadPath = "lubbles/" + getLubbleId() + "/groups/" + chatId;
                if (isDm) {
                    uploadPath = "dms/" + chatId;
                }

                startService(new Intent(AttachImageActivity.this, UploadFileService.class)
                        .putExtra(UploadFileService.EXTRA_BUCKET, UploadFileService.BUCKET_CONVO)
                        .putExtra(UploadFileService.EXTRA_FILE_NAME, imgUri.getLastPathSegment())
                        .putExtra(EXTRA_FILE_URI, imgUri)
                        .putExtra(UploadFileService.EXTRA_UPLOAD_PATH, uploadPath)
                        .putExtra(UploadFileService.EXTRA_CAPTION, captionEt.getText().toString())
                        .putExtra(UploadFileService.EXTRA_CHAT_ID, chatId)
                        .putExtra(UploadFileService.EXTRA_IS_DM, isDm)
                        .putExtra(UploadFileService.EXTRA_AUTHOR_ID, getIntent().getStringExtra(EXTRA_AUTHOR_ID))
                        .putExtra(UploadFileService.EXTRA_IS_AUTHOR_SELLER, getIntent().getBooleanExtra(EXTRA_IS_AUTHOR_SELLER, false))
                        .setAction(UploadFileService.ACTION_UPLOAD));
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
