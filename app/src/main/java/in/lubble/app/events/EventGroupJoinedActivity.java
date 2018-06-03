package in.lubble.app.events;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.chat.ChatActivity;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.EventData;
import in.lubble.app.models.GroupData;

import static in.lubble.app.chat.ChatActivity.EXTRA_GROUP_ID;
import static in.lubble.app.chat.ChatActivity.EXTRA_IS_JOINING;

public class EventGroupJoinedActivity extends AppCompatActivity {

    private static final String TAG = "EventGroupJoinedActiv";
    private static final String STATUS = "STATUS";
    private static final String GROUP_ID = "GROUP_ID";
    private static final String IS_JOINED = "IS_JOINED";

    private RelativeLayout rootLayout;
    private ImageView cancelIcon;
    private TextView titleTv;
    private TextView subtitleTv;
    private ImageView groupIcon;
    private TextView groupNameTv;
    private Button openGroupBtn;

    private int status = 1;
    private String groupId;
    private boolean isJoined;
    private ValueEventListener listener;

    public static void open(Context context, int status, @NonNull String groupId, boolean isJoined) {
        final Intent intent = new Intent(context, EventGroupJoinedActivity.class);
        intent.putExtra(STATUS, status);
        intent.putExtra(GROUP_ID, groupId);
        intent.putExtra(IS_JOINED, isJoined);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_group_joined);

        rootLayout = findViewById(R.id.root);
        cancelIcon = findViewById(R.id.iv_cancel);
        titleTv = findViewById(R.id.tv_title);
        subtitleTv = findViewById(R.id.tv_subtitle);
        groupIcon = findViewById(R.id.iv_group);
        groupNameTv = findViewById(R.id.tv_group_name);
        openGroupBtn = findViewById(R.id.btn_open_group);

        status = getIntent().getIntExtra(STATUS, 1);
        groupId = getIntent().getStringExtra(GROUP_ID);
        isJoined = getIntent().getBooleanExtra(IS_JOINED, false);

        changeLayoutFor(status);
        Analytics.triggerScreenEvent(this, getClass());

        fetchLinkedGroupInfo(groupId);

        openGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Analytics.triggerEvent(AnalyticsEvents.EVENT_JOINED_OPEN_GROUP, EventGroupJoinedActivity.this);
                final Intent intent = new Intent(EventGroupJoinedActivity.this, ChatActivity.class);
                intent.putExtra(EXTRA_GROUP_ID, groupId);
                intent.putExtra(EXTRA_IS_JOINING, false);
                startActivity(intent);
                finish();
            }
        });

        cancelIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void changeLayoutFor(int status) {
        final String isJoinedStr = isJoined ? getString(R.string.already_member) : getString(R.string.now_member);
        if (status == EventData.GOING) {
            rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_green));
            titleTv.setText(R.string.event_going_confirm_title);
            subtitleTv.setText(String.format(getString(R.string.event_going_confirm_subtitle), isJoinedStr));
        } else if (status == EventData.MAYBE) {
            rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.dk_colorAccent));
            titleTv.setText(R.string.event_maybe_confirm_title);
            subtitleTv.setText(String.format(getString(R.string.event_maybe_confirm_subtitle), isJoinedStr));
        }
    }


    private void fetchLinkedGroupInfo(String gid) {
        listener = RealtimeDbHelper.getLubbleGroupsRef().child(gid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    final GroupData groupData = dataSnapshot.getValue(GroupData.class);
                    if (groupData != null) {

                        GlideApp.with(EventGroupJoinedActivity.this)
                                .load(groupData.getThumbnail())
                                .placeholder(R.drawable.ic_circle_group_24dp)
                                .error(R.drawable.ic_circle_group_24dp)
                                .circleCrop()
                                .into(groupIcon);

                        groupNameTv.setText(groupData.getTitle());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (listener != null) {
            RealtimeDbHelper.getLubbleGroupsRef().child(groupId).removeEventListener(listener);
        }
    }
}
