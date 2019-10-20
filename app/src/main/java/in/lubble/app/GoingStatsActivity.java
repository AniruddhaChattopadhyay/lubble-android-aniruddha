package in.lubble.app;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import in.lubble.app.events.EventInfoActivity;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.ProfileInfo;
import in.lubble.app.profile.ProfileActivity;

public class GoingStatsActivity extends BaseActivity {

    private String eventId;


    private GoingStatsAdapter goingStatsAdapter;
    private List<GoingStatsModel> goingStatsModelList;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_going_stats);


        final Intent intent = getIntent();
        eventId = intent.getStringExtra("KEY_EVENT_ID");

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
                    ProfileActivity.open(GoingStatsActivity.this, goingStatsModel.getUid());
                } else {
                    Toast.makeText(GoingStatsActivity.this, "User doesn't exist", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));


        toolbar = findViewById(R.id.goingStatsToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GoingStatsActivity.this, EventInfoActivity.class);
                intent.putExtra("KEY_EVENT_ID", eventId);
                startActivity(intent);
                GoingStatsActivity.this.finish();
            }
        });

        RealtimeDbHelper.getEventsRef().child(eventId).child("title").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                toolbar.setTitle(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(GoingStatsActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        RealtimeDbHelper.getEventsRef().child(eventId).child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                goingStatsModelList.clear();
                for (final DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    RealtimeDbHelper.getUserRef(dataSnapshot1.getKey()).child("info").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            //Log.i("Tejas1", dataSnapshot1.child("response").getValue(Integer.class) + " " + dataSnapshot.toString());

                            if (dataSnapshot.child("thumbnail").exists())
                                goingStatsModelList.add(new GoingStatsModel(dataSnapshot1.getKey(), dataSnapshot.child("name").getValue(String.class), dataSnapshot.child("thumbnail").getValue(String.class), String.valueOf(dataSnapshot1.child("response").getValue(Integer.class))));
                            else
                                goingStatsModelList.add(new GoingStatsModel(dataSnapshot1.getKey(), dataSnapshot.child("name").getValue(String.class), "android.resource://in.lubble.app/drawable/ic_person", String.valueOf(dataSnapshot1.child("response").getValue(Integer.class))));
                            Collections.sort(goingStatsModelList, new Comparator<GoingStatsModel>() {
                                @Override
                                public int compare(GoingStatsModel o1, GoingStatsModel o2) {
                                    return o1.getStats().compareTo(o2.getStats());
                                }
                            });
                            goingStatsAdapter.notifyDataSetChanged();
                            RealtimeDbHelper.getUserRef(dataSnapshot1.getKey()).child("info").removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    });
                }
                RealtimeDbHelper.getEventsRef().child(eventId).child("members").removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(GoingStatsActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, EventInfoActivity.class);
        intent.putExtra("KEY_EVENT_ID", eventId);
        startActivity(intent);
        this.finish();
    }
}

