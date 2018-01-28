package in.lubble.app.groups;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import in.lubble.app.R;
import in.lubble.app.chat.ChatActivity;
import in.lubble.app.models.GroupData;

import static in.lubble.app.Constants.DEFAULT_LUBBLE;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewGroupFragment extends Fragment {

    private static final String TAG = "NewGroupFragment";

    private EditText groupName;
    private EditText groupDesc;
    private DatabaseReference userGroupRef;
    private ProgressDialog progressDialog;

    public NewGroupFragment() {
        // Required empty public constructor
    }

    public static NewGroupFragment newInstance() {
        return new NewGroupFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userGroupRef = FirebaseDatabase.getInstance().getReference("users/" + FirebaseAuth.getInstance().getUid()
                + "/lubbles/" + DEFAULT_LUBBLE + "/groups");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_new_group, container, false);

        groupName = view.findViewById(R.id.et_group_title);
        groupDesc = view.findViewById(R.id.et_group_desc);
        Button createBtn = view.findViewById(R.id.btn_create_group);

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog = new ProgressDialog(getContext());
                progressDialog.setTitle("Creating New Group");
                progressDialog.setMessage(getString(R.string.all_please_wait));
                progressDialog.setCancelable(false);
                progressDialog.show();

                final GroupData groupData = new GroupData();
                groupData.setTitle(groupName.getText().toString());
                groupData.setDescription(groupDesc.getText().toString());

                DatabaseReference pushRef = userGroupRef.push();
                pushRef.setValue(groupData);
                confirmGroupDone(pushRef.getKey());
            }
        });

        return view;
    }

    private void confirmGroupDone(String pushId) {
        userGroupRef.orderByKey().equalTo(pushId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                progressDialog.dismiss();
                startActivity(new Intent(getContext(), ChatActivity.class));
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(getContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
