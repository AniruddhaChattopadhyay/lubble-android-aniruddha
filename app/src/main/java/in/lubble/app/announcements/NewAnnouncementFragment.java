package in.lubble.app.announcements;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import in.lubble.app.R;
import in.lubble.app.models.AnnouncementData;

import static in.lubble.app.firebase.RealtimeDbHelper.getAnnouncementsRef;
import static in.lubble.app.utils.StringUtils.isValidString;

public class NewAnnouncementFragment extends Fragment {

    private EditText titleEt;
    private EditText msgEt;

    public NewAnnouncementFragment() {
        // Required empty public constructor
    }

    public static NewAnnouncementFragment newInstance() {
        return new NewAnnouncementFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_new_announcement, container, false);

        titleEt = view.findViewById(R.id.et_title);
        msgEt = view.findViewById(R.id.et_message);
        Button sendBtn = view.findViewById(R.id.btn_send);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidString(titleEt.getText().toString()) && isValidString(msgEt.getText().toString())) {
                    final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    final AnnouncementData announcementData = new AnnouncementData();
                    announcementData.setAuthorUid(currentUser.getUid());
                    announcementData.setAuthorName(currentUser.getDisplayName());
                    announcementData.setCreatedTimestamp(System.currentTimeMillis());
                    announcementData.setTitle(titleEt.getText().toString());
                    announcementData.setMessage(msgEt.getText().toString());
                    pushAnnouncement(announcementData);
                } else {
                    Toast.makeText(getContext(), R.string.all_fill_details, Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private void pushAnnouncement(AnnouncementData announcementData) {
        getAnnouncementsRef().push().setValue(announcementData);
        getActivity().finish();
    }

}
