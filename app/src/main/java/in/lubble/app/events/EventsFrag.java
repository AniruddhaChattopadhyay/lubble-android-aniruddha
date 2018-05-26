package in.lubble.app.events;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.events.new_event.NewEventActivity;
import in.lubble.app.models.EventData;

import static in.lubble.app.firebase.RealtimeDbHelper.getEventsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getLubbleGroupsRef;

public class EventsFrag extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private EventsAdapter adapter;
    private ChildEventListener childEventListener;
    private ProgressBar progressBar;

    public EventsFrag() {
        // Required empty public constructor
    }

    public static EventsFrag newInstance() {
        return new EventsFrag();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_events, container, false);

        progressBar = view.findViewById(R.id.progressBar_events);
        recyclerView = view.findViewById(R.id.rv_events);
        fab = view.findViewById(R.id.fab_new_event);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventsAdapter(getContext());
        recyclerView.setAdapter(adapter);
        Analytics.triggerScreenEvent(getContext(), this.getClass());

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewEventActivity.open(getContext());
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        progressBar.setVisibility(View.VISIBLE);

        adapter.clear();
        childEventListener = getEventsRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                final EventData eventData = dataSnapshot.getValue(EventData.class);
                if (eventData != null) {
                    eventData.setId(dataSnapshot.getKey());
                    adapter.addEvent(eventData);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        getLubbleGroupsRef().removeEventListener(childEventListener);
    }
}
