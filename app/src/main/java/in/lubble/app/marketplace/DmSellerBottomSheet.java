package in.lubble.app.marketplace;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.ChatData;
import in.lubble.app.models.ProfileInfo;

import static in.lubble.app.firebase.RealtimeDbHelper.getUserInfoRef;

public class DmSellerBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "DmSellerBottomSheet";

    private TextInputLayout introMsgTil;
    private MaterialButton inviteBtn;
    private String receiverUid, receiverName, receiverProfilePic, authorProfilePic;

    public static DmSellerBottomSheet newInstance(String receiverUid, String receiverName, String receiverProfilePic) {

        Bundle args = new Bundle();
        args.putString("receiverUid", receiverUid);
        args.putString("receiverName", receiverName);
        args.putString("receiverProfilePic", receiverProfilePic);
        DmSellerBottomSheet fragment = new DmSellerBottomSheet();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle);
        Analytics.triggerScreenEvent(getContext(), this.getClass());
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
            receiverName = getArguments().getString("receiverName");
            receiverProfilePic = getArguments().getString("receiverProfilePic");
        } else {
            dismiss();
        }

        // get updated author DP
        getUserInfoRef(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                if (profileInfo != null) {
                    authorProfilePic = profileInfo.getThumbnail();
                } else {
                    authorProfilePic = String.valueOf(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        inviteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(introMsgTil.getEditText().getText().toString())) {
                    if (isHighQualityDm(introMsgTil.getEditText().getText().toString())) {
                        createNewDm();
                    } else {
                        introMsgTil.setError("Please send meaningful invitations. Try to explain why you want to message them. Spamming members may get you banned from Lubble.");
                    }
                }
            }
        });

        return view;
    }

    private boolean isHighQualityDm(String dmText) {
        String filteredText = dmText.trim().toLowerCase();
        filteredText = filteredText.replaceAll("hi+", "");
        filteredText = filteredText.replaceAll("hello+", "");
        filteredText = filteredText.replaceAll("yo+", "");
        filteredText = filteredText.replaceAll("hey+", "");
        filteredText = filteredText.replaceAll("hola+", "");
        filteredText = filteredText.replaceAll(receiverName, "");
        return filteredText.length() >= 10;
    }

    private void createNewDm() {

        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        String authorId = firebaseAuth.getUid();
        String authorName = firebaseAuth.getCurrentUser().getDisplayName();

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
        map2.put("isSeller", false);
        map2.put("otherUser", authorId);
        map2.put("name", receiverName);
        map2.put("profilePic", receiverProfilePic);
        userMap.put(receiverUid, map2);

        HashMap<String, Object> authorMap = new HashMap<>();
        authorMap.put("otherUser", receiverUid);
        authorMap.put("joinedTimestamp", System.currentTimeMillis());
        authorMap.put("isSeller", false);
        authorMap.put("author", true);
        authorMap.put("name", authorName);
        authorMap.put("profilePic", authorProfilePic);
        userMap.put(authorId, authorMap);

        final HashMap<String, Object> map = new HashMap<>();
        map.put("members", userMap);
        map.put("message", chatData);
        pushRef.setValue(map);
        String dmId = pushRef.getKey();

        //todo Analytics.triggerEvent(NEW_DM_SENT, getContext());
        dismiss();
    }

}
