package in.lubble.app;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import in.lubble.app.analytics.Analytics;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.EventData;
import in.lubble.app.models.EventMemberData;
import in.lubble.app.models.ProfileInfo;
import in.lubble.app.profile.ProfileActivity;
import in.lubble.app.utils.mapUtils.MathUtil;

public class EventAttendeesActivity extends BaseActivity {

    private EventData eventData;
    private GoingStatsAdapter goingStatsAdapter;
    private List<GoingStatsModel> goingStatsModelList;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_going_stats);

        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Attendees");

        eventData = (EventData) getIntent().getSerializableExtra("KEY_EVENT_DATA");
        Analytics.triggerScreenEvent(this, this.getClass());

        RecyclerView recyclerView = findViewById(R.id.goingStatsRecyclerView);
        goingStatsModelList = new ArrayList<>();
        goingStatsAdapter = new GoingStatsAdapter(this, goingStatsModelList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(goingStatsAdapter);
        recyclerView.addOnItemTouchListener(new GoingStatsRecyclerViewClickListener(this, recyclerView, new GoingStatsRecyclerViewClickListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                GoingStatsModel goingStatsModel = goingStatsModelList.get(position);
                //Toast.makeText(getApplicationContext(),goingStatsModel.getUid(),Toast.LENGTH_LONG).show();
                if (!TextUtils.isEmpty(goingStatsModel.getName())) {
                    ProfileActivity.open(EventAttendeesActivity.this, goingStatsModel.getUid());
                } else {
                    Toast.makeText(EventAttendeesActivity.this, "User doesn't exist", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

    }

    @Override
    protected void onStart() {
        super.onStart();

        goingStatsModelList.clear();

        for (final EventMemberData eventMemberData: eventData.getMembers()) {
            String userId = eventMemberData.getUid();
            if(eventMemberData.getResponse()== EventData.NO )
                continue;
            RealtimeDbHelper.getUserInfoRef(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    final ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                    if (profileInfo != null) {
                        profileInfo.setId(dataSnapshot.getRef().getParent().getKey()); // this works. Don't touch.

                        final GoingStatsModel goingStatsModel =
                                new GoingStatsModel(
                                        profileInfo.getId(),
                                        profileInfo.getName(),
                                        profileInfo.getThumbnail(),
                                        ( eventMemberData.getResponse())
                                );
                        goingStatsModelList.add(goingStatsModel);
                        Collections.sort(goingStatsModelList, new Comparator<GoingStatsModel>() {
                            @Override
                            public int compare(GoingStatsModel o1, GoingStatsModel o2) {
                                return MathUtil.compare(o1.getStatus(), o2.getStatus());
                            }
                        });
                        goingStatsAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Respond to the action bar's Up/Home button
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

