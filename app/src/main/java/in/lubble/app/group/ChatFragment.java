package in.lubble.app.group;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import in.lubble.app.R;
import in.lubble.app.models.ChatData;

public class ChatFragment extends Fragment implements View.OnClickListener {

    private RecyclerView chatRecyclerView;
    private EditText newMessageEt;
    private Button sendBtn;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference messagesReference;

    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseDatabase = FirebaseDatabase.getInstance();
        messagesReference = firebaseDatabase.getReference("messages/lubbles/0/groups/0");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_chat, container, false);

        chatRecyclerView = view.findViewById(R.id.rv_chat);
        newMessageEt = view.findViewById(R.id.et_new_message);
        sendBtn = view.findViewById(R.id.btn_send_message);

        setupTogglingOfSendBtn();
        sendBtn.setOnClickListener(this);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerView.setAdapter(new ChatAdapter(getContext(), new ArrayList<ChatData>()));

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send_message:

                final ChatData chatData = new ChatData();
                chatData.setAuthorName(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                chatData.setMessage(newMessageEt.getText().toString());

                messagesReference.push().setValue(chatData);

                newMessageEt.setText("");
                break;
        }
    }

    private void setupTogglingOfSendBtn() {
        newMessageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                sendBtn.setEnabled(editable.length() > 0);
            }
        });
    }
}
