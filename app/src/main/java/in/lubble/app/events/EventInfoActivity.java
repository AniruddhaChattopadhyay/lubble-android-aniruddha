package in.lubble.app.events;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.emoji.widget.EmojiTextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.crashlytics.android.Crashlytics;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import in.lubble.app.BaseActivity;
import in.lubble.app.EventAttendeesActivity;
import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.chat.ChatActivity;
import in.lubble.app.chat.ShareActiv;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.EventData;
import in.lubble.app.models.ProfileInfo;
import in.lubble.app.models.UserGroupData;
import in.lubble.app.utils.DateTimeUtils;
import in.lubble.app.utils.StringUtils;

import static in.lubble.app.chat.ChatActivity.EXTRA_GROUP_ID;
import static in.lubble.app.chat.ChatActivity.EXTRA_IS_JOINING;
import static in.lubble.app.firebase.RealtimeDbHelper.getCreateOrJoinGroupRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getEventsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserGroupsRef;
import static in.lubble.app.utils.AppNotifUtils.TRACK_NOTIF_ID;
import static in.lubble.app.utils.UiUtils.dpToPx;

public class EventInfoActivity extends BaseActivity {

    public static final String KEY_EVENT_ID = "KEY_EVENT_ID";

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
    private ImageView goingPersonOne, goingPersonTwo, goingPersonThree;
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
    private EmojiTextView descTv;
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
        timeTv = findViewById(R.id.tv_phone);
        addressTv = findViewById(R.id.tv_address);
        linkedGroupTv = findViewById(R.id.tv_linked_group);
        linkedGroupOpenIcon = findViewById(R.id.iv_open_group);
        mapContainer = findViewById(R.id.map_container);
        ticketIv = findViewById(R.id.iv_ticket);
        ticketCountTv = findViewById(R.id.tv_ticket_count);
        luckyDrawHint = findViewById(R.id.lucky_draw_hint);
        descTv = findViewById(R.id.tv_desc);
        goingPersonOne = findViewById(R.id.iv_stats_one);
        goingPersonTwo = findViewById(R.id.iv_stats_two);
        goingPersonThree = findViewById(R.id.iv_stats_three);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.joining_group);
        progressDialog.setMessage(getString(R.string.all_please_wait));

        eventId = getIntent().getStringExtra(KEY_EVENT_ID);
        // Log.i("TejasEIA",eventId);

        eventRef = getEventsRef().child(eventId);
        Analytics.triggerScreenEvent(this, this.getClass());

        goingContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eventData != null && !checkEventAdmin()) {
                    if (oldResponse == EventData.GOING) {
                        new AlertDialog.Builder(EventInfoActivity.this)
                                .setTitle(R.string.event_not_going_confirm_title)
                                .setMessage(R.string.event_not_going_confirm_subtitle)
                                .setPositiveButton(R.string.event_not_going, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        changeStatus(EventData.NO);
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton(R.string.all_cancel, new DialogInterface.OnClickListener() {
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
        try {
            Intent intent = this.getIntent();
            if (intent != null && intent.getExtras() != null && intent.getExtras().containsKey(TRACK_NOTIF_ID)
                    && (intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
                int notifId = Integer.parseInt(this.getIntent().getExtras().getString(TRACK_NOTIF_ID));

                if (notifId > 0) {
                    final Bundle bundle = new Bundle();
                    bundle.putString("notifKey", String.valueOf(notifId));
                    bundle.putString("eventId", eventId);
                    Analytics.triggerEvent(AnalyticsEvents.NOTIF_OPENED, bundle, this);
                }
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }

        shareContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareActiv.open(EventInfoActivity.this, eventId, ShareActiv.ShareType.EVENT);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchEventInfo();
        statsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventInfoActivity.this, EventAttendeesActivity.class);
                intent.putExtra("KEY_EVENT_DATA", eventData);
                startActivity(intent);
            }
        });
    }

    private boolean checkEventAdmin() {
        final HashMap<String, Object> memberMap = (HashMap<String, Object>) eventData.getMembers().get(FirebaseAuth.getInstance().getUid());
        boolean isAdmin;
        isAdmin = memberMap != null && memberMap.get("isAdmin") != null && ((boolean) memberMap.get("isAdmin"));
        if (isAdmin) {
            new AlertDialog.Builder(EventInfoActivity.this)
                    .setTitle(R.string.event_host_leave_title)
                    .setMessage(R.string.event_host_leave_subtitle)
                    .setPositiveButton(R.string.all_ok, new DialogInterface.OnClickListener() {
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
                            } else {
                                luckyDrawHint.setVisibility(View.GONE);
                            }
                            ticketIv.setVisibility(View.GONE);
                            ticketCountTv.setVisibility(View.GONE);
                        }
                    } else {
                        if (System.currentTimeMillis() < DateTimeUtils.FAMILY_FUN_NIGHT_END_TIME) {
                            luckyDrawHint.setVisibility(View.VISIBLE);
                        } else {
                            luckyDrawHint.setVisibility(View.GONE);
                        }
                        ticketIv.setVisibility(View.GONE);
                        ticketCountTv.setVisibility(View.GONE);
                    }
                    final String month = DateTimeUtils.getTimeFromLong(eventData.getStartTimestamp(), "MMM");
                    final String monthFull = DateTimeUtils.getTimeFromLong(eventData.getStartTimestamp(), "MMMM");
                    final String date = DateTimeUtils.getTimeFromLong(eventData.getStartTimestamp(), "dd");
                    final String dayOfWeek = DateTimeUtils.getTimeFromLong(eventData.getStartTimestamp(), "EEE");
                    final String startTime = DateTimeUtils.getTimeFromLong(eventData.getStartTimestamp(), "h:mm a");
                    String endTime = getString(R.string.event_time_onwards);
                    if (eventData.getEndTimestamp() > 0) {
                        endTime = "- " + DateTimeUtils.getTimeFromLong(eventData.getEndTimestamp(), "h:mm a");
                    }
                    monthTv.setText(month);
                    dateTv.setText(date);
                    timeTv.setText(String.format(getString(R.string.event_time_text), dayOfWeek, date, monthFull, startTime, endTime));

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
                    if (goingCount > 0) {
                        statsIcon.setVisibility(View.VISIBLE);
                        statsTv.setVisibility(View.VISIBLE);
                        statsTv.setText(String.format(getString(R.string.event_going_count), goingCount));
                    }
                    if (maybeCount > 0) {
                        statsIcon.setVisibility(View.VISIBLE);
                        statsTv.setVisibility(View.VISIBLE);
                        final CharSequence prefixText = statsTv.getText();
                        String suffixText = "";
                        if (StringUtils.isValidString(prefixText.toString())) {
                            suffixText = " · ";
                        }
                        suffixText += String.format(getString(R.string.event_maybe_suffix), maybeCount);
                        statsTv.setText(String.format(getString(R.string.event_maybe_count), prefixText, suffixText));
                    }
                    fetchIsLinkedGroupJoined(eventData.getGid());
                    fetchMemberInfo(eventData);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void fetchMemberInfo(EventData eventData) {
        goingPersonOne.setImageDrawable(null);
        goingPersonOne.setVisibility(View.GONE);
        goingPersonTwo.setImageDrawable(null);
        goingPersonTwo.setVisibility(View.GONE);
        goingPersonThree.setImageDrawable(null);
        goingPersonThree.setVisibility(View.GONE);
        for (Map.Entry<String, Object> entry : eventData.getMembers().entrySet()) {
            HashMap<String, Object> memberInfoMap = (HashMap<String, Object>) entry.getValue();
            if ((long) memberInfoMap.get("response") == EventData.GOING) {
                RealtimeDbHelper.getUserInfoRef(entry.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                        if (profileInfo != null && !TextUtils.isEmpty(profileInfo.getThumbnail())) {
                            ImageView emptyImageView = getEmptyImageView();
                            if (emptyImageView != null) {
                                GlideApp.with(EventInfoActivity.this)
                                        .load(profileInfo.getThumbnail())
                                        .placeholder(R.drawable.ic_account_circle_black_no_padding)
                                        .circleCrop()
                                        .into(emptyImageView);
                                emptyImageView.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        }
    }

    @Nullable
    private ImageView getEmptyImageView() {
        if (goingPersonOne.getDrawable() == null) {
            return goingPersonOne;
        } else if (goingPersonTwo.getDrawable() == null) {
            return goingPersonTwo;
        } else if (goingPersonThree.getDrawable() == null) {
            return goingPersonThree;
        }
        return null;
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
                        linkedGroupTv.setText(String.format(getString(R.string.event_linked_group_hint), title));
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
