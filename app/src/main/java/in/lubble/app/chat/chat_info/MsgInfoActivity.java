package in.lubble.app.chat.chat_info;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.ChatData;
import in.lubble.app.models.MsgInfoData;
import in.lubble.app.models.ProfileInfo;

import static in.lubble.app.firebase.RealtimeDbHelper.getUserInfoRef;

public class MsgInfoActivity extends AppCompatActivity {

    private static final String TAG = "MsgInfoActivity";
    private static final String ARG_GROUP_ID = "ARG_GROUP_ID";
    private static final String ARG_CHAT_ID = "ARG_CHAT_ID";

    private RecyclerView recyclerView;
    private String groupId;
    private String chatId;
    private MsgReadAdapter adapter;
    private ValueEventListener listener;
    private ChatData chatData;

    @NonNull
    public static Intent getIntent(Context context, String groupId, String chatId) {
        final Intent intent = new Intent(context, MsgInfoActivity.class);
        intent.putExtra(ARG_GROUP_ID, groupId);
        intent.putExtra(ARG_CHAT_ID, chatId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_info);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Message Info");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.rv_msg_info);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MsgReadAdapter(GlideApp.with(this));
        recyclerView.setAdapter(adapter);

        groupId = getIntent().getStringExtra(ARG_GROUP_ID);
        chatId = getIntent().getStringExtra(ARG_CHAT_ID);

    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchReadReceipts();
    }

    private void fetchReadReceipts() {
        listener = RealtimeDbHelper.getMessagesRef().child(groupId).child(chatId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatData = dataSnapshot.getValue(ChatData.class);
                if (chatData != null) {
                    final HashMap<String, Long> readReceiptsMap = chatData.getReadReceipts();
                    for (String uid : readReceiptsMap.keySet()) {
                        fetchProfileInfo(uid);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchProfileInfo(String uid) {
        getUserInfoRef(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                if (profileInfo != null) {
                    profileInfo.setId(dataSnapshot.getRef().getParent().getKey()); // this works. Don't touch.
                    final MsgInfoData msgInfoData = new MsgInfoData();
                    msgInfoData.setProfileInfo(profileInfo);
                    msgInfoData.setTimestamp(chatData.getReadReceipts().get(profileInfo.getId()));
                    adapter.addData(msgInfoData);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        RealtimeDbHelper.getMessagesRef().child(groupId).child(chatId).removeEventListener(listener);
    }
}
