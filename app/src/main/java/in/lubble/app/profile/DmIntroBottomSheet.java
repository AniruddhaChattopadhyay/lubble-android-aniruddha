package in.lubble.app.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import in.lubble.app.LubbleApp;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.ChatData;
import in.lubble.app.models.ProfileInfo;

import static in.lubble.app.analytics.AnalyticsEvents.NEW_DM_SENT;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserInfoRef;

public class DmIntroBottomSheet extends BottomSheetDialogFragment {

    private static final String TAG = "DmIntroBottomSheet";

    private TextInputLayout introMsgTil;
    private MaterialButton inviteBtn;
    private TextView hintTv;
    private String receiverUid, receiverName, receiverProfilePic, authorProfilePic, sellerPhoneNumber;
    private boolean isSeller;

    public static DmIntroBottomSheet newInstance(String receiverUid, String receiverName, String receiverProfilePic, @Nullable String sellerPhoneNumber) {

        Bundle args = new Bundle();
        args.putString("receiverUid", receiverUid);
        args.putString("receiverName", receiverName);
        args.putString("receiverProfilePic", receiverProfilePic);
        args.putString("sellerPhoneNumber", sellerPhoneNumber);
        DmIntroBottomSheet fragment = new DmIntroBottomSheet();
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

        hintTv = view.findViewById(R.id.tv_hint);
        introMsgTil = view.findViewById(R.id.til_intro_msg);
        inviteBtn = view.findViewById(R.id.btn_invite);

        if (getArguments() != null) {
            receiverUid = getArguments().getString("receiverUid");
            receiverName = getArguments().getString("receiverName");
            receiverProfilePic = getArguments().getString("receiverProfilePic");
            sellerPhoneNumber = getArguments().getString("sellerPhoneNumber");
            isSeller = !TextUtils.isEmpty(sellerPhoneNumber);

            if(isSeller){
                hintTv.setText("You can send only one message before they accept your invitation to chat privately. Write your requirements here.");
                introMsgTil.setHint("Your query/requirements");
                inviteBtn.setText("Send Message");
            } else {
                hintTv.setText("You can send only one message before they accept your invitation to chat privately. Be meaningful &amp; respectful.");
                introMsgTil.setHint("Intro message");
                inviteBtn.setText("INVITE TO CHAT");
            }

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

        setCancelable(false);
        inviteBtn.setEnabled(false);
        inviteBtn.setText("Sending...");

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
        map2.put("isSeller", isSeller);
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

        if (isSeller) {
            final HashMap<String, Object> createSellerDmMap = new HashMap<>();
            createSellerDmMap.put("sellerId", receiverUid);
            createSellerDmMap.put("sellerName", receiverName);
            createSellerDmMap.put("sellerPhone", sellerPhoneNumber);
            createSellerDmMap.put("message", introMsgTil.getEditText().getText().toString());
            createSellerDmMap.put("authorUid", authorId);
            createSellerDmMap.put("authorName", authorName);
            createSellerDmMap.put("timestamp", System.currentTimeMillis());

            Map<String, Object> childUpdates = new HashMap<>();
            String key = FirebaseDatabase.getInstance().getReference("create_seller_dm").push().getKey();
            childUpdates.put(pushRef.toString().substring(pushRef.getRoot().toString().length()), map);
            childUpdates.put("create_seller_dm/" + key, createSellerDmMap);
            FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(LubbleApp.getAppContext(), "Message sent", Toast.LENGTH_SHORT).show();
                        dismiss();
                    } else {
                        Toast.makeText(LubbleApp.getAppContext(), "Something went wrong. Plz try again or contact support", Toast.LENGTH_SHORT).show();
                        setCancelable(true);
                        inviteBtn.setEnabled(true);
                        inviteBtn.setText("Send Message");
                    }
                }
            });
        } else {
            pushRef.setValue(map);
        }

        String dmId = pushRef.getKey();

        Analytics.triggerEvent(NEW_DM_SENT, getContext());
    }

}
