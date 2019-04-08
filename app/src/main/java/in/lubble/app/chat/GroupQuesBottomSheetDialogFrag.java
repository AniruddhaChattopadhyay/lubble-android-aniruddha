package in.lubble.app.chat;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.emoji.widget.EmojiTextView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.ChatData;
import in.lubble.app.models.GroupData;
import in.lubble.app.utils.ChatUtils;
import in.lubble.app.utils.UiUtils;

import static android.app.Activity.RESULT_OK;
import static in.lubble.app.firebase.RealtimeDbHelper.getLubbleGroupsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getMessagesRef;
import static in.lubble.app.utils.NotifUtils.deleteUnreadMsgsForGroupId;

public class GroupQuesBottomSheetDialogFrag extends BottomSheetDialogFragment {

    private static final String ARG_GROUP_ID = "GroupQuestionActiv_ARG_GROUP_ID";

    private ImageView dpIv;
    private EmojiTextView questionTv;
    private EditText answerEt;
    private ImageView sendBtnIv;
    private String groupId;
    private String replyMsgId;

    public static GroupQuesBottomSheetDialogFrag newInstance(String groupId) {
        Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID, groupId);
        GroupQuesBottomSheetDialogFrag fragment = new GroupQuesBottomSheetDialogFrag();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        //super.setupDialog(dialog, style);
        View view = View.inflate(getContext(), R.layout.frag_group_question, null);
        dialog.setContentView(view);

        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();

        int height = displayMetrics.heightPixels;

        int maxHeight = (int) (height * 0.88);

        final BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from(((View) view.getParent()));
        bottomSheetBehavior.setPeekHeight(maxHeight);
        view.requestLayout();

        dpIv = view.findViewById(R.id.iv_dp);
        questionTv = view.findViewById(R.id.tv_question);
        answerEt = view.findViewById(R.id.et_answer);
        sendBtnIv = view.findViewById(R.id.iv_send_btn);

        groupId = getArguments().getString(ARG_GROUP_ID);

        getLubbleGroupsRef().child(groupId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final GroupData groupData = dataSnapshot.getValue(GroupData.class);
                        questionTv.setText(groupData.getQuestion());
                        GlideApp.with(GroupQuesBottomSheetDialogFrag.this)
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
                    getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_OK, null);
                    dismiss();
                }
            }
        });

        answerEt.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getContext() != null) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(answerEt, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        }, 1000);
        answerEt.requestFocus();
        final Bundle bundle = new Bundle();
        bundle.putString("group_id", groupId);
        Analytics.triggerEvent(AnalyticsEvents.GROUP_QUES_SHOWN, bundle, getContext());
    }

    private void sendAnswer() {
        UiUtils.hideKeyboard(requireContext());

        final ChatData chatData = ChatUtils.createReplyChatdata(answerEt.getText().toString(), replyMsgId);
        getMessagesRef().child(groupId).push().setValue(chatData);

        // reset user's group unread counter
        RealtimeDbHelper.getUserGroupsRef().child(groupId).child("unreadCount").setValue(0);

        deleteUnreadMsgsForGroupId(groupId, requireContext());
        final Bundle bundle = new Bundle();
        bundle.putString("group_id", groupId);
        Analytics.triggerEvent(AnalyticsEvents.GROUP_QUES_ANSWERED, bundle, getContext());
    }

}
