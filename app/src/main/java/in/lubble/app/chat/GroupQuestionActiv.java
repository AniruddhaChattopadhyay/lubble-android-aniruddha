package in.lubble.app.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.emoji.widget.EmojiTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.ChatData;
import in.lubble.app.models.GroupData;
import in.lubble.app.utils.ChatUtils;
import in.lubble.app.utils.NotifUtils;
import in.lubble.app.utils.UiUtils;

import static in.lubble.app.firebase.RealtimeDbHelper.getLubbleGroupsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getMessagesRef;
import static in.lubble.app.utils.NotifUtils.deleteUnreadMsgsForGroupId;

public class GroupQuestionActiv extends AppCompatActivity {

    private static final String ARG_GROUP_ID = "GroupQuestionActiv_ARG_GROUP_ID";

    private ImageView dpIv;
    private EmojiTextView questionTv;
    private EditText answerEt;
    private ImageView sendBtnIv;
    private String groupId;
    private String replyMsgId;

    public static Intent getIntent(Context context, String groupId) {
        final Intent intent = new Intent(context, GroupQuestionActiv.class);
        intent.putExtra(ARG_GROUP_ID, groupId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_question);

        dpIv = findViewById(R.id.iv_dp);
        questionTv = findViewById(R.id.tv_question);
        answerEt = findViewById(R.id.et_answer);
        sendBtnIv = findViewById(R.id.iv_send_btn);

        groupId = getIntent().getStringExtra(ARG_GROUP_ID);

        getLubbleGroupsRef().child(groupId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final GroupData groupData = dataSnapshot.getValue(GroupData.class);
                        questionTv.setText(groupData.getQuestion());
                        replyMsgId = groupData.getQuestionChatId();
                        GlideApp.with(GroupQuestionActiv.this)
                                .load(groupData.getProfilePic())
                                .placeholder(R.drawable.circle)
                                .error(R.drawable.ic_group_24dp)
                                .into(dpIv);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        sendBtnIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(answerEt.getText().toString())) {
                    sendAnswer();
                    setResult(RESULT_OK);
                    finish();
                    overridePendingTransition(R.anim.none, R.anim.slide_to_bottom);
                }
            }
        });

    }

    private void sendAnswer() {
        UiUtils.hideKeyboard(this);

        final ChatData chatData = ChatUtils.createReplyChatdata(answerEt.getText().toString(), replyMsgId);
        getMessagesRef().child(groupId).push().setValue(chatData);

        // reset user's group unread counter
        RealtimeDbHelper.getUserGroupsRef().child(groupId).child("unreadCount").setValue(0);

        deleteUnreadMsgsForGroupId(groupId, this);
        NotifUtils.sendNotifAnalyticEvent(AnalyticsEvents.NOTIF_REPLIED, groupId, this);
    }

}
