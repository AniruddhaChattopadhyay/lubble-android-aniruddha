package in.lubble.app.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.firebase.RealtimeDbHelper;


public class PinnedMessageBottomSheet extends BottomSheetDialogFragment {
    private TextView pinnedMessageContent;
    private RelativeLayout pinnedMessageContainer;
    private final String groupId;

    public PinnedMessageBottomSheet(String gid) {
        this.groupId = gid;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = new Bundle();
        bundle.putString("group_id", groupId);
        Analytics.triggerScreenEvent(getContext(), this.getClass());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.pinned_message_bottom_sheet_layout, container, false);
        pinnedMessageContent = rootview.findViewById(R.id.pinned_message_content);
        pinnedMessageContainer = rootview.findViewById(R.id.pinned_message_container);
        MaterialButton dismissBtn = rootview.findViewById(R.id.btn_pin_msg_ok);

        RealtimeDbHelper.getLubbleGroupsRef().child(groupId).child("pinned_message").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String message = snapshot.getValue(String.class);
                    if (!TextUtils.isEmpty(message)) {
                        pinnedMessageContainer.setVisibility(View.VISIBLE);
                        pinnedMessageContent.setMaxLines(Integer.MAX_VALUE);
                        pinnedMessageContent.setText(message.replaceAll("\\\\n", "\n"));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return rootview;
    }
}
