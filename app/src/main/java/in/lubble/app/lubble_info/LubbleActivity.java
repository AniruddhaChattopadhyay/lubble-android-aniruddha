package in.lubble.app.lubble_info;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import in.lubble.app.R;
import in.lubble.app.announcements.announcementHistory.AnnouncementsActivity;
import in.lubble.app.domestic_directory.DomesticDirectoryActivity;

public class LubbleActivity extends AppCompatActivity {

    private static final String TAG = "LubbleActivity";

    private ImageView lubbleIv;
    private TextView lubbleInfoTv;
    private TextView noticeBoardTv;
    private TextView domesticDirectoryTv;

    public static void open(Context context) {
        context.startActivity(new Intent(context, LubbleActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lubble);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Saraswati Vihar");
        lubbleIv = findViewById(R.id.iv_lubble_image);
        lubbleInfoTv = findViewById(R.id.tv_lubble_info);
        noticeBoardTv = findViewById(R.id.tv_notice_board);
        domesticDirectoryTv = findViewById(R.id.tv_domestic_directory);

        lubbleInfoTv.setText(R.string.about_svr);

        noticeBoardTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnnouncementsActivity.open(LubbleActivity.this);
            }
        });

        domesticDirectoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DomesticDirectoryActivity.open(LubbleActivity.this);
            }
        });
    }
}
