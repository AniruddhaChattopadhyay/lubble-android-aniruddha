package in.lubble.app;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
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

public class GoingStatsActivity extends BaseActivity {

    String eventId;
    RecyclerView recyclerView;


    GoingStatsAdapter goingStatsAdapter;
    List<GoingStatsModel> goingStatsModelList;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_going_stats);


        Intent intent = getIntent();
        eventId = intent.getStringExtra("KEY_EVENT_ID");

        recyclerView = findViewById(R.id.goingStatsRecyclerView);
        goingStatsModelList = new ArrayList<>();
        goingStatsAdapter = new GoingStatsAdapter(getApplicationContext(), goingStatsModelList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(goingStatsAdapter);

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

        FirebaseDatabase.getInstance().getReference().child("lubbles/DEV/events").child(eventId).child("title").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                toolbar.setTitle(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseDatabase.getInstance().getReference().child("lubbles/DEV/events").child(eventId).child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                goingStatsModelList.clear();
                for (final DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    FirebaseDatabase.getInstance().getReference().child("users").child(dataSnapshot1.getKey()).child("info").addValueEventListener(new ValueEventListener() {
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
                                    return o2.getStats().compareTo(o1.getStats());
                                }
                            });
                            goingStatsAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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

