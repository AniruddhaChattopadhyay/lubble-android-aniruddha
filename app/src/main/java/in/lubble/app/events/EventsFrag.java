package in.lubble.app.events;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.events.new_event.NewEventActivity;
import in.lubble.app.models.EventData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static in.lubble.app.firebase.RealtimeDbHelper.getLubbleGroupsRef;

public class EventsFrag extends Fragment {
    private static final String TAG = "EventsFrag";
    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private LinearLayout emptyEventContainer;
    private EventsAdapter adapter;
    private ChildEventListener childEventListener;
    private ProgressBar progressBar;
    private Endpoints endpoints;
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
        emptyEventContainer = view.findViewById(R.id.container_empty_events);

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

        LubbleSharedPrefs.getInstance().setEventSet(null);
        adapter.clear();
        return view;
    }
    public void getEvents(){
        Log.d(TAG, "inside get events on on event frag");
        String lubble_id = LubbleSharedPrefs.getInstance().getLubbleId();
        Call<List<EventData>> call = endpoints.getEvents("ayush_django_backend_token","ayush_django_backend",LubbleSharedPrefs.getInstance().getLubbleId());
        call.enqueue(new Callback<List<EventData>>() {
            @Override
            public void onResponse(Call<List<EventData>> call, Response<List<EventData>> response) {
                if (!response.isSuccessful()) {
                    Log.e(TAG,response.code()+"");
                    return;
                }
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                List<EventData> data = response.body();
                adapter.clear();
                for (EventData eventData:data) {
                    if (eventData != null) {
                        eventData.setId(eventData.getEvent_id());
                        adapter.addEvent(eventData);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<EventData>> call, Throwable t) {
               Log.e(TAG,"failed to get response from django");
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        emptyEventContainer.setVisibility(View.GONE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getResources().getString(R.string.fetch_test_event))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        //endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints = retrofit.create(Endpoints.class);
        getEvents();




//        childEventListener = getEventsRef().orderByChild("startTimestamp").addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                if (progressBar != null) {
//                    progressBar.setVisibility(View.GONE);
//                }
//                final EventData eventData = dataSnapshot.getValue(EventData.class);
//                if (eventData != null) {
//                    eventData.setId(dataSnapshot.getKey());
//                    adapter.addEvent(eventData);
//                }
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//        getEventsRef().orderByChild("startTimestamp").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.getValue() == null) {
//                    // zero events
//                    if (progressBar != null) {
//                        progressBar.setVisibility(View.GONE);
//                    }
//                    recyclerView.setVisibility(View.GONE);
//                    emptyEventContainer.setVisibility(View.VISIBLE);
//                }
//
//                if (getActivity() != null && getActivity() instanceof MainActivity) {
//                    ((MainActivity) getActivity()).showEventsBadge(0);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
    }

    @Override
    public void onPause() {
        super.onPause();
        //getLubbleGroupsRef().removeEventListener(childEventListener);
    }
}
