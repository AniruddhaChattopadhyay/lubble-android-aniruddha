package in.lubble.app.announcements.announcementHistory;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import in.lubble.app.R;
import in.lubble.app.models.AnnouncementData;

import static in.lubble.app.Constants.DEFAULT_LUBBLE;

public class AnnouncementsFrag extends Fragment {

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
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        final AnnouncementsAdapter adapter = new AnnouncementsAdapter();
        recyclerView.setAdapter(adapter);

        FirebaseDatabase.getInstance().getReference("messages/lubbles/" + DEFAULT_LUBBLE + "/announcements")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            adapter.addAnnouncement(child.getValue(AnnouncementData.class));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        return view;
    }

}
