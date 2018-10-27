package in.lubble.app.lubble_info;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import in.lubble.app.BaseActivity;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.announcements.announcementHistory.AnnouncementsActivity;
import in.lubble.app.events.EventsActivity;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.GroupData;

public class LubbleActivity extends BaseActivity {

    private static final String TAG = "LubbleActivity";

    private ImageView lubbleIv;
    private TextView lubbleInfoTv;
    private TextView noticeBoardTv;
    private TextView eventsTv;
    private ProgressBar progressBar;

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
        progressBar = findViewById(R.id.progressBar_lubbleInfo);
        lubbleInfoTv = findViewById(R.id.tv_lubble_info);
        noticeBoardTv = findViewById(R.id.tv_notice_board);
        eventsTv = findViewById(R.id.tv_events);

        lubbleInfoTv.setText(R.string.about_svr);

        RealtimeDbHelper.getLubbleGroupsRef().child(LubbleSharedPrefs.getInstance().getDefaultGroupId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final GroupData groupData = dataSnapshot.getValue(GroupData.class);
                        if (groupData != null) {
                            GlideApp.with(LubbleActivity.this)
                                    .load(groupData.getProfilePic())
                                    .placeholder(R.drawable.city)
                                    .error(R.drawable.city)
                                    .listener(new RequestListener<Drawable>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            progressBar.setVisibility(View.GONE);
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                            progressBar.setVisibility(View.GONE);
                                            return false;
                                        }
                                    })
                                    .into(lubbleIv);
                            LubbleSharedPrefs.getInstance().setDefaultGroupId(dataSnapshot.child("defaultGroup").getValue(String.class));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        noticeBoardTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnnouncementsActivity.open(LubbleActivity.this);
            }
        });

        eventsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventsActivity.open(LubbleActivity.this);
            }
        });
    }
}
