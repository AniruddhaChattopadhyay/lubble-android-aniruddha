package in.lubble.app.events;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.chat.ChatActivity;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.EventData;
import in.lubble.app.models.UserGroupData;
import in.lubble.app.utils.DateTimeUtils;
import in.lubble.app.utils.StringUtils;

import static in.lubble.app.chat.ChatActivity.EXTRA_GROUP_ID;
import static in.lubble.app.chat.ChatActivity.EXTRA_IS_JOINING;
import static in.lubble.app.firebase.RealtimeDbHelper.getCreateOrJoinGroupRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getEventsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserGroupsRef;
import static in.lubble.app.utils.UiUtils.dpToPx;

public class EventInfoActivity extends AppCompatActivity {

    private static final String KEY_EVENT_ID = "KEY_EVENT_ID";

    private String eventId;
    private DatabaseReference eventRef;
    private ValueEventListener eventInfoListener;
    private ProgressBar progressBar;
    private EventData eventData;
    private ImageView groupHeaderIv;
    private TextView monthTv;
    private TextView dateTv;
    private TextView organizerTv;
    private ImageView goingIcon;
    private TextView eventNameTv;
    private TextView goingHintTv;
    private ImageView maybeIcon;
    private TextView maybeHintTv;
    private TextView finalMarkedStatus;
    private LinearLayout goingContainer;
    private LinearLayout maybeContainer;
    private LinearLayout shareContainer;
    private ImageView statsIcon;
    private TextView statsTv;
    private TextView timeTv;
    private TextView addressTv;
    private TextView linkedGroupTv;
    private ImageView linkedGroupOpenIcon;
    private LinearLayout mapContainer;
    private ImageView ticketIv;
    private TextView ticketCountTv;
    private LinearLayout luckyDrawHint;
    private TextView descTv;
    private ProgressDialog progressDialog;
    private boolean isLinkedGroupJoined;
    private DatabaseReference checkGroupJoinedRef;
    private ValueEventListener checkGroupJoinedListener;
    private DatabaseReference isGroupJoinedRef;
    private ValueEventListener isGroupJoinedListener;
    private DatabaseReference groupTitleRef;
    private ValueEventListener groupTitleListener;
    private long oldResponse = EventData.NO;

    public static void open(Context context, String eventId) {
        Intent intent = new Intent(context, EventInfoActivity.class);
        intent.putExtra(KEY_EVENT_ID, eventId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("");

        progressBar = findViewById(R.id.progressBar_groupInfo);
        groupHeaderIv = findViewById(R.id.iv_group_image);

        monthTv = findViewById(R.id.tv_month);
        dateTv = findViewById(R.id.tv_date);
        organizerTv = findViewById(R.id.tv_organizer_name);
        eventNameTv = findViewById(R.id.tv_event_name);
        goingIcon = findViewById(R.id.iv_going);
        goingHintTv = findViewById(R.id.tv_going_hint);
        maybeIcon = findViewById(R.id.iv_maybe);
        maybeHintTv = findViewById(R.id.tv_maybe_hint);
        finalMarkedStatus = findViewById(R.id.tv_final_marked_status);
        goingContainer = findViewById(R.id.going_container);
        maybeContainer = findViewById(R.id.maybe_container);
        shareContainer = findViewById(R.id.share_container);
        statsIcon = findViewById(R.id.iv_stats);
        statsTv = findViewById(R.id.tv_stats);
        timeTv = findViewById(R.id.tv_time);
        addressTv = findViewById(R.id.tv_address);
        linkedGroupTv = findViewById(R.id.tv_linked_group);
        linkedGroupOpenIcon = findViewById(R.id.iv_open_group);
        mapContainer = findViewById(R.id.map_container);
        ticketIv = findViewById(R.id.iv_ticket);
        ticketCountTv = findViewById(R.id.tv_ticket_count);
        luckyDrawHint = findViewById(R.id.lucky_draw_hint);
        descTv = findViewById(R.id.tv_desc);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Joining Group");
        progressDialog.setMessage("Please Wait...");

        eventId = getIntent().getStringExtra(KEY_EVENT_ID);

        eventRef = getEventsRef().child(eventId);

        goingContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eventData != null && !checkEventAdmin()) {
                    if (oldResponse == EventData.GOING) {
                        new AlertDialog.Builder(EventInfoActivity.this)
                                .setTitle("Not Going?")
                                .setMessage("Are you sure you will not attend the event?")
                                .setPositiveButton("Not Going", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        changeStatus(EventData.NO);
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    } else {
                        changeStatus(EventData.GOING);
                    }
                }
            }
        });

        maybeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eventData != null && !checkEventAdmin()) {
                    final int newResponse = oldResponse == EventData.MAYBE ? EventData.NO : EventData.MAYBE;
                    changeStatus(newResponse);
                }
            }
        });

        linkedGroupOpenIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eventData != null) {
                    final Intent intent = new Intent(EventInfoActivity.this, ChatActivity.class);
                    intent.putExtra(EXTRA_GROUP_ID, eventData.getGid());
                    intent.putExtra(EXTRA_IS_JOINING, false);
                    startActivity(intent);
                }
            }
        });

        mapContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eventData != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + eventData.getLati() +
                            "," + eventData.getLongi() + "?q=" + eventData.getLati() + "," + eventData.getLongi() + "(" + eventData.getTitle() + ")"));
                    startActivity(intent);
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchEventInfo();
    }

    private boolean checkEventAdmin() {
        final HashMap<String, Object> memberMap = (HashMap<String, Object>) eventData.getMembers().get(FirebaseAuth.getInstance().getUid());
        boolean isAdmin;
        isAdmin = memberMap != null && memberMap.get("isAdmin") != null && ((boolean) memberMap.get("isAdmin"));
        if (isAdmin) {
            new AlertDialog.Builder(EventInfoActivity.this)
                    .setTitle("Hosts cannot leave event")
                    .setMessage("You are hosting this event & cannot change your own status.")
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
        return isAdmin;
    }

    private void changeStatus(int newResponse) {
        progressDialog.show();
        final DatabaseReference eventMemberRef = getEventsRef()
                .child(eventId)
                .child("members")
                .child(FirebaseAuth.getInstance().getUid());

        final HashMap<String, Object> map = new HashMap<>();
        map.put("response", newResponse);
        if (newResponse == EventData.NO) {
            map.put("guests", 0);
        }
        map.put("timestamp", System.currentTimeMillis());

        if (newResponse != EventData.NO) {
            getCreateOrJoinGroupRef().child(eventData.getGid()).setValue(true);
        }
        eventMemberRef.updateChildren(map);

        checkGroupJoined(eventData.getGid(), newResponse);
    }

    private void checkGroupJoined(@NonNull final String groupId, final int status) {
        checkGroupJoinedRef = getUserGroupsRef().child(groupId);
        checkGroupJoinedListener = checkGroupJoinedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
                toggleGoingButton(status == EventData.GOING);
                toggleMaybeButton(status == EventData.MAYBE);
                if (status != EventData.NO) {
                    EventGroupJoinedActivity.open(EventInfoActivity.this, status, eventId, groupId, isLinkedGroupJoined);
                }
                oldResponse = status;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void toggleGoingButton(boolean isGoing) {
        if (isGoing) {
            goingIcon.setImageResource(R.drawable.ic_check_circle_black_24dp);
            goingIcon.setColorFilter(ContextCompat.getColor(this, R.color.dark_green), android.graphics.PorterDuff.Mode.SRC_IN);
            goingHintTv.setTextColor((ContextCompat.getColor(this, R.color.dark_green)));
        } else {
            goingIcon.setImageResource(R.drawable.ic_check_circle_outline);
            goingIcon.setColorFilter(ContextCompat.getColor(this, R.color.dark_gray), android.graphics.PorterDuff.Mode.SRC_IN);
            goingHintTv.setTextColor((ContextCompat.getColor(this, R.color.black)));
        }
    }

    private void toggleMaybeButton(boolean isMaybe) {
        if (isMaybe) {
            maybeIcon.setImageResource(R.drawable.ic_help_black_24dp);
            maybeIcon.setColorFilter(ContextCompat.getColor(this, R.color.dk_colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);
            maybeHintTv.setTextColor((ContextCompat.getColor(this, R.color.dk_colorAccent)));
        } else {
            maybeIcon.setImageResource(R.drawable.ic_help_outline_black_24dp);
            maybeIcon.setColorFilter(ContextCompat.getColor(this, R.color.dark_gray), android.graphics.PorterDuff.Mode.SRC_IN);
            maybeHintTv.setTextColor((ContextCompat.getColor(this, R.color.black)));
        }
    }

    private void fetchEventInfo() {

        eventInfoListener = eventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                eventData = dataSnapshot.getValue(EventData.class);
                if (eventData != null) {
                    setTitleWhenCollapsed();
                    GlideApp.with(EventInfoActivity.this)
                            .load(eventData.getProfilePic())
                            .error(R.drawable.ic_star_party)
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    progressBar.setVisibility(View.GONE);
                                    groupHeaderIv.setBackgroundColor(ContextCompat.getColor(EventInfoActivity.this, R.color.dark_teal));
                                    groupHeaderIv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                    groupHeaderIv.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    progressBar.setVisibility(View.GONE);
                                    groupHeaderIv.setBackgroundColor(ContextCompat.getColor(EventInfoActivity.this, R.color.black));
                                    groupHeaderIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                    groupHeaderIv.setPadding(0, 0, 0, 0);
                                    return false;
                                }
                            })
                            .into(groupHeaderIv);
                    organizerTv.setText(eventData.getOrganizer());
                    eventNameTv.setText(eventData.getTitle());
                    addressTv.setText(eventData.getAddress());
                    descTv.setText(eventData.getDesc());

                    if (System.currentTimeMillis() < eventData.getStartTimestamp()) {
                        finalMarkedStatus.setVisibility(View.GONE);
                    } else {
                        finalMarkedStatus.setVisibility(View.VISIBLE);
                        goingContainer.setVisibility(View.GONE);
                        maybeContainer.setVisibility(View.GONE);
                    }

                    final HashMap<String, Object> memberMap = (HashMap<String, Object>) eventData.getMembers().get(FirebaseAuth.getInstance().getUid());
                    if (memberMap != null) {
                        oldResponse = (long) memberMap.get("response");
                        toggleGoingButton(oldResponse == EventData.GOING);
                        toggleMaybeButton(oldResponse == EventData.MAYBE);

                        setFinalMarkedResponse(oldResponse);
                        if (memberMap.get("tickets") != null && ((long) memberMap.get("tickets")) > 0) {
                            ticketIv.setVisibility(View.VISIBLE);
                            ticketCountTv.setVisibility(View.VISIBLE);
                            luckyDrawHint.setVisibility(View.GONE);
                            ticketCountTv.setText("Lucky Draw Tickets: " + ((long) memberMap.get("tickets")));
                        } else {
                            if (System.currentTimeMillis() < DateTimeUtils.FAMILY_FUN_NIGHT_END_TIME) {
                                luckyDrawHint.setVisibility(View.VISIBLE);
                            }
                            ticketIv.setVisibility(View.GONE);
                            ticketCountTv.setVisibility(View.GONE);
                        }

                    }
                    final String month = DateTimeUtils.getTimeFromLong(eventData.getStartTimestamp(), "MMM");
                    final String monthFull = DateTimeUtils.getTimeFromLong(eventData.getStartTimestamp(), "MMMM");
                    final String date = DateTimeUtils.getTimeFromLong(eventData.getStartTimestamp(), "dd");
                    final String dayOfWeek = DateTimeUtils.getTimeFromLong(eventData.getStartTimestamp(), "EEE");
                    final String startTime = DateTimeUtils.getTimeFromLong(eventData.getStartTimestamp(), "h:mm a");
                    String endTime = "onwards";
                    if (eventData.getEndTimestamp() > 0) {
                        endTime = "- " + DateTimeUtils.getTimeFromLong(eventData.getEndTimestamp(), "h:mm a");
                    }
                    monthTv.setText(month);
                    dateTv.setText(date);
                    timeTv.setText(dayOfWeek + ", " + date + " " + monthFull + " from " + startTime + " " + endTime);

                    fetchLinkedGroupInfo(eventData.getGid());

                    int goingCount = 0;
                    int maybeCount = 0;
                    for (Map.Entry<String, Object> entry : eventData.getMembers().entrySet()) {
                        final Integer responseInt = (int) ((long) ((HashMap<String, Object>) entry.getValue()).get("response"));
                        if (responseInt == EventData.GOING) {
                            goingCount++;
                        } else if (responseInt == EventData.MAYBE) {
                            maybeCount++;
                        }
                    }
                    if (goingCount > 5) {
                        statsIcon.setVisibility(View.VISIBLE);
                        statsTv.setVisibility(View.VISIBLE);
                        statsTv.setText(goingCount + " going");
                    }
                    if (maybeCount > 3) {
                        statsIcon.setVisibility(View.VISIBLE);
                        statsTv.setVisibility(View.VISIBLE);
                        final CharSequence prefixText = statsTv.getText();
                        String suffixText = "";
                        if (StringUtils.isValidString(prefixText.toString())) {
                            suffixText = " · ";
                        }
                        suffixText += maybeCount + " maybe";
                        statsTv.setText(prefixText + suffixText);
                    }
                    fetchIsLinkedGroupJoined(eventData.getGid());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setFinalMarkedResponse(long oldResponse) {
        switch ((int) oldResponse) {
            case EventData.GOING:
                finalMarkedStatus.setText("Marked as GOING");
                finalMarkedStatus.setTextColor(ContextCompat.getColor(this, R.color.dark_green));
                break;
            case EventData.MAYBE:
                finalMarkedStatus.setText("Marked as MAYBE");
                finalMarkedStatus.setTextColor(ContextCompat.getColor(this, R.color.dk_blue));
                break;
            default:
                finalMarkedStatus.setText("Not Responded");
                finalMarkedStatus.setTextColor(ContextCompat.getColor(this, R.color.black));
                break;
        }
    }

    private void fetchIsLinkedGroupJoined(@NonNull String linkedGroupId) {
        isGroupJoinedRef = getUserGroupsRef().child(linkedGroupId);
        isGroupJoinedListener = isGroupJoinedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final UserGroupData userGroupData = dataSnapshot.getValue(UserGroupData.class);
                if (userGroupData != null) {
                    isLinkedGroupJoined = userGroupData.isJoined();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void fetchLinkedGroupInfo(String gid) {
        groupTitleRef = RealtimeDbHelper.getLubbleGroupsRef().child(gid).child("title");
        groupTitleListener = groupTitleRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    final String title = dataSnapshot.getValue(String.class);
                    if (StringUtils.isValidString(title)) {
                        linkedGroupTv.setText("Linked Group: " + title);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setTitleWhenCollapsed() {
        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        AppBarLayout appBarLayout = findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle(eventData.getTitle());
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbarLayout.setTitle(" ");// careful there should a space between double quote otherwise it wont work
                    isShow = false;
                }
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
        if (eventInfoListener != null) {
            eventRef.removeEventListener(eventInfoListener);
        }
        if (checkGroupJoinedRef != null && checkGroupJoinedListener != null) {
            checkGroupJoinedRef.removeEventListener(checkGroupJoinedListener);
        }
        if (isGroupJoinedRef != null && isGroupJoinedListener != null) {
            isGroupJoinedRef.removeEventListener(isGroupJoinedListener);
        }
        if (groupTitleRef != null && groupTitleListener != null) {
            groupTitleRef.removeEventListener(groupTitleListener);
        }
    }

}
