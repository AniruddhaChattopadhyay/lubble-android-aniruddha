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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
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
import static in.lubble.app.firebase.RealtimeDbHelper.getEventsRef;

public class EventGroupJoinedActivity extends AppCompatActivity {

    private static final String TAG = "EventGroupJoinedActiv";
    private static final String STATUS = "STATUS";
    private static final String EVENT_ID = "EVENT_ID";
    private static final String GROUP_ID = "GROUP_ID";
    private static final String IS_JOINED = "IS_JOINED";

    private RelativeLayout rootLayout;
    private ImageView cancelIcon;
    private TextView titleTv;
    private TextView subtitleTv;
    private ImageView groupIcon;
    private TextView groupNameTv;
    private LinearLayout openGroupBtn;
    private TextView finalGuestCountTv;
    private Button confirmGuestsBtn;
    private ElegantNumberButton guestCounter;
    private LinearLayout ticketShareLayout;

    private int status = 1;
    private String eventId;
    private String groupId;
    private boolean isJoined;
    private ValueEventListener listener;

    public static void open(Context context, int status, String eventId, @NonNull String groupId, boolean isJoined) {
        final Intent intent = new Intent(context, EventGroupJoinedActivity.class);
        intent.putExtra(STATUS, status);
        intent.putExtra(EVENT_ID, eventId);
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
        openGroupBtn = findViewById(R.id.btn_open_group);
        finalGuestCountTv = findViewById(R.id.tv_final_guest_count);
        guestCounter = findViewById(R.id.guest_counter);
        confirmGuestsBtn = findViewById(R.id.btn_confirm_guests);
        groupNameTv = findViewById(R.id.tv_group_name);
        ticketShareLayout = findViewById(R.id.ticket_share_layout);

        status = getIntent().getIntExtra(STATUS, 1);
        eventId = getIntent().getStringExtra(EVENT_ID);
        groupId = getIntent().getStringExtra(GROUP_ID);
        isJoined = getIntent().getBooleanExtra(IS_JOINED, false);

        changeLayoutFor(status);
        Analytics.triggerScreenEvent(this, getClass());

        fetchLinkedGroupInfo(groupId);

        guestCounter.setOnClickListener(new ElegantNumberButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (confirmGuestsBtn.getVisibility() != View.VISIBLE) {
                    confirmGuestsBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        confirmGuestsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String guestCount = guestCounter.getNumber();
                final DatabaseReference eventMemberRef = getEventsRef()
                        .child(eventId)
                        .child("members")
                        .child(FirebaseAuth.getInstance().getUid())
                        .child("guests");
                eventMemberRef.setValue(Integer.parseInt(guestCount));
                guestCounter.setVisibility(View.GONE);
                confirmGuestsBtn.setVisibility(View.GONE);
                finalGuestCountTv.setVisibility(View.VISIBLE);
                finalGuestCountTv.setText(" + " + guestCount + " guests");
            }
        });

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
        final String isJoinedStr = isJoined ? "already" : "now";
        if (status == EventData.GOING) {
            rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_green));
            titleTv.setText("See you at the event!");
            subtitleTv.setText("You are " + isJoinedStr + " a member of the event's group.\nChat with neighbours who are also going.");
        } else if (status == EventData.MAYBE) {
            rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.dk_colorAccent));
            titleTv.setText("Hope to see you there!");
            subtitleTv.setText("You are " + isJoinedStr + " a member of the event's group.\nChat with neighbours who are also interested.");
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
