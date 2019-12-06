package in.lubble.app.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

import in.lubble.app.R;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.ChatData;

public class DmIntroBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "DmIntroBottomSheet";

    private TextInputLayout introMsgTil;
    private MaterialButton inviteBtn;
    private String receiverUid;

    public static DmIntroBottomSheet newInstance(String receiverUid) {

        Bundle args = new Bundle();
        args.putString("receiverUid", receiverUid);
        DmIntroBottomSheet fragment = new DmIntroBottomSheet();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.frag_dm_intro_bottom_sheet, container, false);

        introMsgTil = view.findViewById(R.id.til_intro_msg);
        inviteBtn = view.findViewById(R.id.btn_invite);

        if (getArguments() != null) {
            receiverUid = getArguments().getString("receiverUid");
        } else {
            dismiss();
        }

        inviteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(introMsgTil.getEditText().getText().toString())) {
                    createNewDm();
                }
            }
        });

        return view;
    }

    private void createNewDm() {

        String authorId = FirebaseAuth.getInstance().getUid();

        final ChatData chatData = new ChatData();
        chatData.setAuthorUid(authorId);
        chatData.setAuthorIsSeller(false);
        chatData.setMessage(introMsgTil.getEditText().getText().toString());
        chatData.setCreatedTimestamp(System.currentTimeMillis());
        chatData.setServerTimestamp(ServerValue.TIMESTAMP);
        chatData.setIsDm(true);

        final DatabaseReference pushRef = RealtimeDbHelper.getCreateDmRef().push();

        final HashMap<String, Object> userMap = new HashMap<>();
        final HashMap<Object, Object> map2 = new HashMap<>();
        map2.put("joinedTimestamp", System.currentTimeMillis());
        map2.put("isSeller", false);
        map2.put("otherUser", authorId);
        userMap.put(receiverUid, map2);

        HashMap<String, Object> authorMap = new HashMap<>();
        authorMap.put("otherUser", receiverUid);
        authorMap.put("joinedTimestamp", System.currentTimeMillis());
        authorMap.put("isSeller", false);
        authorMap.put("author", true);
        userMap.put(authorId, authorMap);

        final HashMap<String, Object> map = new HashMap<>();
        map.put("members", userMap);
        map.put("message", chatData);
        pushRef.setValue(map);
        String dmId = pushRef.getKey();

        dismiss();
    }

}
