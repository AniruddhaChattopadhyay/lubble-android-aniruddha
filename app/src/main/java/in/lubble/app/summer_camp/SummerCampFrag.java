package in.lubble.app.summer_camp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;

import static in.lubble.app.firebase.RealtimeDbHelper.getLubbleGroupsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getLubbleRef;

public class SummerCampFrag extends Fragment {

    private static final String TAG = "SummerCampFrag";

    private RecyclerView summerCampRecyclerView;
    private LinearLayout campOverContainer;
    private ImageView campOverIv;
    private TextView campOverTitleTv;
    private TextView campOverDescTv;
    private FloatingActionButton fab;
    //private SummerCampAdapter adapter;
    private ChildEventListener childEventListener;
    private ValueEventListener campCheckListener;
    private ProgressBar progressBar;

    public SummerCampFrag() {
        // Required empty public constructor
    }

    public static SummerCampFrag newInstance() {
        return new SummerCampFrag();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_events, container, false);

        progressBar = view.findViewById(R.id.progressBar_events);
        summerCampRecyclerView = view.findViewById(R.id.rv_events);
        /*campOverContainer = view.findViewById(R.id.container_camp_over);
        campOverIv = view.findViewById(R.id.iv_camp_over);
        campOverTitleTv = view.findViewById(R.id.tv_camp_over_title);
        fab = view.findViewById(R.id.fab_new_event);
        campOverDescTv = view.findViewById(R.id.tv_camp_over_desc);*/

        summerCampRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //adapter = new SummerCampAdapter(getContext());
        //summerCampRecyclerView.setAdapter(adapter);
        Analytics.triggerScreenEvent(getContext(), this.getClass());

        /*fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewCampActivity.open(getContext());
            }
        });*/

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        progressBar.setVisibility(View.VISIBLE);

        campCheckListener = getLubbleRef().child("summerCampCheck").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();
                if (map != null) {
                    final Boolean isEnabled = (Boolean) map.get("isEnabled");
                    if (!isEnabled) {
                        campOverTitleTv.setText((String) map.get("title"));
                        campOverDescTv.setText((String) map.get("desc"));
                        summerCampRecyclerView.setVisibility(View.GONE);
                        campOverContainer.setVisibility(View.VISIBLE);
                    } else {
                        summerCampRecyclerView.setVisibility(View.VISIBLE);
                        campOverContainer.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*adapter.clear();
        childEventListener = getLubbleGroupsRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                final GroupData publicGroup = dataSnapshot.getValue(GroupData.class);
                adapter.addGroup(publicGroup);
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
        });*/
    }

    @Override
    public void onPause() {
        super.onPause();
        getLubbleGroupsRef().removeEventListener(childEventListener);
        getLubbleRef().child("summerCampCheck").removeEventListener(campCheckListener);

    }
}
