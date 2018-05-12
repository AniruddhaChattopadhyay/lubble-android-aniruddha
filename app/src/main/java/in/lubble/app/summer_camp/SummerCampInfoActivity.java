package in.lubble.app.summer_camp;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import in.lubble.app.GlideApp;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.models.GroupData;

import static in.lubble.app.firebase.RealtimeDbHelper.getCreateOrJoinGroupRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getLubbleGroupsRef;
import static in.lubble.app.groups.GroupListFragment.EXTRA_GROUP_ID_HIGHLIGHT;

public class SummerCampInfoActivity extends AppCompatActivity {

    private static final String KEY_GROUP_ID = "KEY_GROUP_ID";

    private String groupId;
    private DatabaseReference groupReference;
    private ValueEventListener groupInfoListener;
    private ProgressBar progressBar;
    private GroupData groupData;
    private ImageView groupHeaderIv;
    private Button joinBtn;

    public static void open(Context context, String groupId) {
        Intent intent = new Intent(context, SummerCampInfoActivity.class);
        intent.putExtra(KEY_GROUP_ID, groupId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summer_camp_info);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("");

        progressBar = findViewById(R.id.progressBar_groupInfo);
        groupHeaderIv = findViewById(R.id.iv_group_image);
        joinBtn = findViewById(R.id.btn_join);

        groupId = getIntent().getStringExtra(KEY_GROUP_ID);

        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCreateOrJoinGroupRef().child(groupId).setValue(true);
                final Intent intent = new Intent(SummerCampInfoActivity.this, MainActivity.class);
                intent.putExtra(EXTRA_GROUP_ID_HIGHLIGHT, groupId);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finishAffinity();
            }
        });

        groupReference = getLubbleGroupsRef().child(groupId);
        fetchGroupInfo();
    }

    private void fetchGroupInfo() {

        groupInfoListener = groupReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                groupData = dataSnapshot.getValue(GroupData.class);
                if (groupData != null) {
                    setTitle(groupData.getTitle());
                    GlideApp.with(SummerCampInfoActivity.this)
                            .load(groupData.getProfilePic())
                            .placeholder(R.drawable.ic_wb_sunny_black_24dp)
                            .error(R.drawable.ic_wb_sunny_black_24dp)
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
                            .into(groupHeaderIv);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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

    @Override
    public void onPause() {
        super.onPause();
        groupReference.removeEventListener(groupInfoListener);
    }

}
