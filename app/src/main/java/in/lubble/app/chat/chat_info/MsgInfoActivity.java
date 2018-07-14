package in.lubble.app.chat.chat_info;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
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
    private static final String ARG_SHOW_READ_RECEIPTS = "ARG_SHOW_READ_RECEIPTS";

    private LinearLayout noLikesContainer;
    private LinearLayout noReadsContainer;
    private TextView readByHeaderTv;
    private RecyclerView readRecyclerView;
    private RecyclerView lubbRecyclerView;
    private String groupId;
    private String chatId;
    private MsgReceiptAdapter readAdapter;
    private MsgReceiptAdapter lubbAdapter;
    @Nullable
    private ValueEventListener readListener;
    @Nullable
    private ValueEventListener lubbListener;
    private ChatData chatData;
    private boolean showReadReceipts;

    @NonNull
    public static Intent getIntent(Context context, String groupId, String chatId, boolean showReadReceipts) {
        final Intent intent = new Intent(context, MsgInfoActivity.class);
        intent.putExtra(ARG_GROUP_ID, groupId);
        intent.putExtra(ARG_CHAT_ID, chatId);
        intent.putExtra(ARG_SHOW_READ_RECEIPTS, showReadReceipts);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_info);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.msg_info));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        readByHeaderTv = findViewById(R.id.tv_read_by_header);
        noLikesContainer = findViewById(R.id.linear_layout_no_likes);
        noReadsContainer = findViewById(R.id.linear_layout_no_reads);
        readRecyclerView = findViewById(R.id.rv_msg_info);
        lubbRecyclerView = findViewById(R.id.rv_liked_msg_info);

        readRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        lubbRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        readAdapter = new MsgReceiptAdapter(GlideApp.with(this));
        lubbAdapter = new MsgReceiptAdapter(GlideApp.with(this));
        readRecyclerView.setAdapter(readAdapter);
        lubbRecyclerView.setAdapter(lubbAdapter);

        groupId = getIntent().getStringExtra(ARG_GROUP_ID);
        chatId = getIntent().getStringExtra(ARG_CHAT_ID);
        showReadReceipts = getIntent().getBooleanExtra(ARG_SHOW_READ_RECEIPTS, false);

    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchLubbReceipts();
        if (showReadReceipts) {
            readByHeaderTv.setVisibility(View.VISIBLE);
            readRecyclerView.setVisibility(View.VISIBLE);
            fetchReadReceipts();
        } else {
            readByHeaderTv.setVisibility(View.GONE);
            readRecyclerView.setVisibility(View.GONE);
        }
    }

    private void fetchReadReceipts() {
        readListener = RealtimeDbHelper.getMessagesRef().child(groupId).child(chatId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatData = dataSnapshot.getValue(ChatData.class);
                if (chatData != null) {
                    final HashMap<String, Long> readReceiptsMap = chatData.getReadReceipts();
                    if (readReceiptsMap.size() > 1) {
                        noReadsContainer.setVisibility(View.GONE);
                        for (String uid : readReceiptsMap.keySet()) {
                            if (!uid.equalsIgnoreCase(FirebaseAuth.getInstance().getUid())) {
                                fetchAndAddProfileInfoToReadReceipts(uid);
                            }
                        }
                    } else {
                        noReadsContainer.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchAndAddProfileInfoToReadReceipts(String uid) {
        getUserInfoRef(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                if (profileInfo != null) {
                    profileInfo.setId(dataSnapshot.getRef().getParent().getKey()); // this works. Don't touch.
                    final MsgInfoData msgInfoData = new MsgInfoData();
                    msgInfoData.setProfileInfo(profileInfo);
                    msgInfoData.setTimestamp(chatData.getReadReceipts().get(profileInfo.getId()));
                    readAdapter.addData(msgInfoData);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void fetchLubbReceipts() {
        lubbListener = RealtimeDbHelper.getMessagesRef().child(groupId).child(chatId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatData = dataSnapshot.getValue(ChatData.class);
                if (chatData != null) {
                    final HashMap<String, Long> lubbReceiptsMap = chatData.getLubbReceipts();
                    if (lubbReceiptsMap.size() > 0) {
                        noLikesContainer.setVisibility(View.GONE);
                        for (String uid : lubbReceiptsMap.keySet()) {
                            fetchAndAddProfileInfoToLubbReceipts(uid);
                        }
                    } else {
                        noLikesContainer.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchAndAddProfileInfoToLubbReceipts(String uid) {
        getUserInfoRef(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                if (profileInfo != null) {
                    profileInfo.setId(dataSnapshot.getRef().getParent().getKey()); // this works. Don't touch.
                    final MsgInfoData msgInfoData = new MsgInfoData();
                    msgInfoData.setProfileInfo(profileInfo);
                    msgInfoData.setTimestamp(chatData.getReadReceipts().get(profileInfo.getId()));
                    lubbAdapter.addData(msgInfoData);
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
        if (lubbListener != null) {
            RealtimeDbHelper.getMessagesRef().child(groupId).child(chatId).removeEventListener(lubbListener);
        }
        if (readListener != null) {
            RealtimeDbHelper.getMessagesRef().child(groupId).child(chatId).removeEventListener(readListener);
        }
    }
}
