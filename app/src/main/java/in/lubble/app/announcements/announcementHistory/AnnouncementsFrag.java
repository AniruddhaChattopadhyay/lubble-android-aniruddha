package in.lubble.app.announcements.announcementHistory;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import in.lubble.app.R;
import in.lubble.app.announcements.NewAnnouncementActivity;
import in.lubble.app.models.AnnouncementData;

import static in.lubble.app.firebase.RealtimeDbHelper.getAnnouncementsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserLubbleRef;

public class AnnouncementsFrag extends Fragment {

    private AnnouncementsAdapter announcementsAdapter;

    public static AnnouncementsFrag newInstance() {
        return new AnnouncementsFrag();
    }

    public AnnouncementsFrag() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_announcements, container, false);

        final RecyclerView recyclerView = view.findViewById(R.id.rv_announcements);
        final FloatingActionButton fab = view.findViewById(R.id.fab_new_announcement);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        announcementsAdapter = new AnnouncementsAdapter();
        recyclerView.setAdapter(announcementsAdapter);

        getAnnouncementsRef().addValueEventListener(announcementEventListener);

        toggleAnnouncementBtn(fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewAnnouncementActivity.newInstance(getContext());
            }
        });

        return view;
    }

    final ValueEventListener announcementEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            for (DataSnapshot child : dataSnapshot.getChildren()) {
                announcementsAdapter.addAnnouncement(child.getValue(AnnouncementData.class));
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void toggleAnnouncementBtn(final FloatingActionButton newAnnouncementBtn) {
        // single listener as user won't have admin role updated frequently
        getUserLubbleRef().child("isAdmin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null && dataSnapshot.getValue(Boolean.class)) {
                    newAnnouncementBtn.setVisibility(View.VISIBLE);
                } else {
                    newAnnouncementBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "ERROR: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        getAnnouncementsRef().removeEventListener(announcementEventListener);
    }
}
