package in.lubble.app.chat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import in.lubble.app.BuildConfig;
import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.UploadFileService;
import in.lubble.app.utils.TouchImageView;

import static in.lubble.app.UploadFileService.EXTRA_FILE_URI;
import static in.lubble.app.utils.UserUtils.getLubbleId;

public class AttachImageActivity extends AppCompatActivity {

    private static final String EXTRA_IMG_PATH = BuildConfig.APPLICATION_ID + "_NEW_IMG_PATH";
    private static final String EXTRA_GROUP_ID = BuildConfig.APPLICATION_ID + "_NEW_IMG_GROUP_ID";

    private TouchImageView touchImageView;
    private EditText captionEt;
    private ImageView sendIcon;

    public static void open(Context context, Uri imgUri, String groupId) {
        Intent intent = new Intent(context, AttachImageActivity.class);
        intent.putExtra(EXTRA_IMG_PATH, imgUri);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
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
        final String groupId = getIntent().getStringExtra(EXTRA_GROUP_ID);

        GlideApp.with(this).load(imgUri).fitCenter().into(touchImageView);

        sendIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(AttachImageActivity.this, UploadFileService.class)
                        .putExtra(UploadFileService.EXTRA_BUCKET, UploadFileService.BUCKET_CONVO)
                        .putExtra(UploadFileService.EXTRA_FILE_NAME, imgUri.getLastPathSegment())
                        .putExtra(EXTRA_FILE_URI, imgUri)
                        .putExtra(UploadFileService.EXTRA_UPLOAD_PATH, "lubbles/" + getLubbleId() + "/groups/" + groupId)
                        .putExtra(UploadFileService.EXTRA_CAPTION, captionEt.getText().toString())
                        .putExtra(UploadFileService.EXTRA_GROUP_ID, groupId)
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
