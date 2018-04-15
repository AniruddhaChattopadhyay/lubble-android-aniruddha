package in.lubble.app.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.models.ProfileData;
import in.lubble.app.utils.FragUtils;
import in.lubble.app.utils.UserUtils;

import static in.lubble.app.firebase.RealtimeDbHelper.getUserRef;

public class ProfileFrag extends Fragment {
    private static final String TAG = "ProfileFrag";
    private static final String ARG_USER_ID = "arg_user_id";

    private View rootView;
    private String userId;
    private ImageView coverPicIv;
    private ImageView profilePicIv;
    private TextView userName;
    private TextView locality;
    private TextView userBio;
    private TextView editProfileTV;
    private Button inviteBtn;
    private TextView logoutTv;
    private CardView referralCard;
    private DatabaseReference userRef;
    private ValueEventListener valueEventListener;

    public ProfileFrag() {
        // Required empty public constructor
    }

    public static ProfileFrag newInstance(String profileId) {
        ProfileFrag fragment = new ProfileFrag();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, profileId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        coverPicIv = rootView.findViewById(R.id.iv_cover);
        profilePicIv = rootView.findViewById(R.id.iv_profilePic);
        userName = rootView.findViewById(R.id.tv_name);
        locality = rootView.findViewById(R.id.tv_locality);
        userBio = rootView.findViewById(R.id.tv_bio);
        editProfileTV = rootView.findViewById(R.id.tv_editProfile);
        referralCard = rootView.findViewById(R.id.card_referral);
        inviteBtn = rootView.findViewById(R.id.btn_invite);
        logoutTv = rootView.findViewById(R.id.tv_logout);

        profilePicIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //// TODO: 21/11/17 open pic full screen with edit option if its user's profile
            }
        });

        editProfileTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragUtils.addFrag(getFragmentManager(), R.id.frameLayout_fragContainer, EditProfileFrag.newInstance());
            }
        });

        inviteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onInviteClicked();
            }
        });

        logoutTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserUtils.logout(getActivity());
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        userRef = getUserRef(userId);
        fetchProfileFeed();
    }

    private void fetchProfileFeed() {
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ProfileData profileData = dataSnapshot.getValue(ProfileData.class);
                userName.setText(profileData.getInfo().getName());
                locality.setText(profileData.getLocality());
                userBio.setText(profileData.getBio());
                if (userId.equalsIgnoreCase(FirebaseAuth.getInstance().getUid())) {
                    editProfileTV.setVisibility(View.VISIBLE);
                    referralCard.setVisibility(View.VISIBLE);
                }
                GlideApp.with(getContext())
                        .load(profileData.getProfilePic())
                        .error(R.drawable.ic_account_circle_black_no_padding)
                        .placeholder(R.drawable.ic_account_circle_black_no_padding)
                        .circleCrop()
                        .into(profilePicIv);
                GlideApp.with(getContext())
                        .load(profileData.getCoverPic())
                        .into(coverPicIv);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        userRef.addValueEventListener(valueEventListener);
    }


    private void onInviteClicked() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        String link = "https://lubble.in/?invitedby=" + uid;
        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(link))
                .setSocialMetaTagParameters(new DynamicLink.SocialMetaTagParameters.Builder()
                        .setTitle("Join Saraswati Vihar Lubble")
                        .setDescription("Click to open Lubble App!")
                        .setImageUrl(Uri.parse("https://place-hold.it/300x200"))
                        .build()
                )
                .setDynamicLinkDomain("bx5at.app.goo.gl")
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder()
                                .setMinimumVersion(1)
                                .build())
                .buildShortDynamicLink()
                .addOnSuccessListener(new OnSuccessListener<ShortDynamicLink>() {
                    @Override
                    public void onSuccess(ShortDynamicLink shortDynamicLink) {
                        Uri mInvitationUrl = shortDynamicLink.getShortLink();
                        sendInvite(mInvitationUrl);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: " + e.toString());
                    }
                });
    }

    private void sendInvite(Uri mInvitationUrl) {
        String referrerName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        String subject = String.format("%s wants you to join Lubble!", referrerName);
        String invitationLink = mInvitationUrl.toString();
        String msg = "Join me on Lubble! Use my referrer link: "
                + invitationLink;
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, subject);
            i.putExtra(Intent.EXTRA_TEXT, msg);
            startActivity(Intent.createChooser(i, "choose one"));
        } catch (Exception e) {
            Log.e(TAG, "sendInvite: " + e.toString());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        userRef.removeEventListener(valueEventListener);
    }

}
