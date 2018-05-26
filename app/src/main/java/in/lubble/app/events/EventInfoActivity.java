package in.lubble.app.events;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.EventData;
import in.lubble.app.utils.DateTimeUtils;
import in.lubble.app.utils.StringUtils;

import static in.lubble.app.firebase.RealtimeDbHelper.getEventsRef;

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
    private TextView eventNameTv;
    private LinearLayout goingContainer;
    private LinearLayout maybeContainer;
    private LinearLayout shareContainer;
    private ImageView statsIcon;
    private TextView statsTv;
    private TextView timeTv;
    private TextView addressTv;
    private TextView linkedGroupTv;
    private TextView descTv;

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
        goingContainer = findViewById(R.id.going_container);
        maybeContainer = findViewById(R.id.maybe_container);
        shareContainer = findViewById(R.id.share_container);
        statsIcon = findViewById(R.id.iv_stats);
        statsTv = findViewById(R.id.tv_stats);
        timeTv = findViewById(R.id.tv_time);
        addressTv = findViewById(R.id.tv_address);
        linkedGroupTv = findViewById(R.id.tv_linked_group);
        descTv = findViewById(R.id.tv_desc);

        eventId = getIntent().getStringExtra(KEY_EVENT_ID);

        eventRef = getEventsRef().child(eventId);
        fetchGroupInfo();
    }

    private void fetchGroupInfo() {

        eventInfoListener = eventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                eventData = dataSnapshot.getValue(EventData.class);
                if (eventData != null) {
                    //setTitle(eventData.getTitle());
                    GlideApp.with(EventInfoActivity.this)
                            .load(eventData.getProfilePic())
                            .placeholder(R.drawable.ic_star_party)
                            .error(R.drawable.ic_star_party)
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
                    organizerTv.setText(eventData.getOrganizer());
                    eventNameTv.setText(eventData.getTitle());
                    addressTv.setText(eventData.getAddress());
                    descTv.setText(eventData.getDesc());

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
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void fetchLinkedGroupInfo(String gid) {
        RealtimeDbHelper.getLubbleGroupsRef().child(gid).child("title").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    final String title = dataSnapshot.getValue(String.class);
                    if (StringUtils.isValidString(title)) {
                        linkedGroupTv.setText(title);
                    }
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
        eventRef.removeEventListener(eventInfoListener);
    }

}
