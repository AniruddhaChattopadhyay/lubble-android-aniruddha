package in.lubble.app.announcements.announcementHistory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.announcements.NewAnnouncementActivity;
import in.lubble.app.models.AnnouncementData;
import in.lubble.app.utils.AppNotifUtils;

import static in.lubble.app.firebase.RealtimeDbHelper.getAnnouncementsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserLubbleRef;

public class AnnouncementsFrag extends Fragment {

    private AnnouncementsAdapter announcementsAdapter;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private LinearLayout emptyNoticesContainer;

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

        progressBar = view.findViewById(R.id.progressBar_notices);
        recyclerView = view.findViewById(R.id.rv_announcements);
        emptyNoticesContainer = view.findViewById(R.id.container_empty_notices);
        final FloatingActionButton fab = view.findViewById(R.id.fab_new_announcement);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        announcementsAdapter = new AnnouncementsAdapter();
        recyclerView.setAdapter(announcementsAdapter);

        Analytics.triggerScreenEvent(getContext(), this.getClass());

        toggleAnnouncementBtn(fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewAnnouncementActivity.newInstance(getContext());
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getAnnouncementsRef().addValueEventListener(announcementEventListener);
    }

    final ValueEventListener announcementEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (progressBar.getVisibility() == View.VISIBLE) {
                progressBar.setVisibility(View.GONE);
            }
            emptyNoticesContainer.setVisibility(View.GONE);
            announcementsAdapter.clear();
            for (DataSnapshot child : dataSnapshot.getChildren()) {
                announcementsAdapter.addAnnouncement(child.getValue(AnnouncementData.class));
                AppNotifUtils.deleteAppNotif(getContext(), child.getKey());
            }
            if (dataSnapshot.getValue() == null) {
                // zero notices
                emptyNoticesContainer.setVisibility(View.VISIBLE);
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
