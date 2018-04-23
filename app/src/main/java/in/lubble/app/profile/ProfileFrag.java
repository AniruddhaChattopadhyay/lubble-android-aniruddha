package in.lubble.app.profile;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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

import in.lubble.app.BuildConfig;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.ProfileData;
import in.lubble.app.utils.FragUtils;
import in.lubble.app.utils.FullScreenImageActivity;
import in.lubble.app.utils.UserUtils;

import static in.lubble.app.firebase.RealtimeDbHelper.getUserRef;
import static in.lubble.app.utils.StringUtils.isValidString;

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
    private ProgressBar progressBar;
    private CardView referralCard;
    private DatabaseReference userRef;
    private ValueEventListener valueEventListener;
    @Nullable
    private ProfileData profileData;

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
        progressBar = rootView.findViewById(R.id.progressBar_profile);
        TextView versionTv = rootView.findViewById(R.id.tv_version_name);
        RelativeLayout feedbackView = rootView.findViewById(R.id.feedback_container);

        profilePicIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String profilePicUrl = profileData.getProfilePic();
                if (isValidString(profilePicUrl)) {
                    String uploadPath = null;
                    if (userId.equalsIgnoreCase(FirebaseAuth.getInstance().getUid())) {
                        uploadPath = "user_profile/" + FirebaseAuth.getInstance().getUid();
                    }
                    FullScreenImageActivity.open(getActivity(), getContext(), profilePicUrl, profilePicIv, uploadPath, R.drawable.ic_account_circle_black_no_padding);
                }
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
                Analytics.triggerLogoutEvent(getContext());
                UserUtils.logout(getActivity());
            }
        });

        versionTv.setVisibility(FirebaseAuth.getInstance().getUid().equalsIgnoreCase(userId) ? View.VISIBLE : View.GONE);
        feedbackView.setVisibility(FirebaseAuth.getInstance().getUid().equalsIgnoreCase(userId) ? View.VISIBLE : View.GONE);

        versionTv.setText(BuildConfig.VERSION_NAME);
        feedbackView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ayush@mittalsoft.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Lubble Feedback");
                intent.putExtra(Intent.EXTRA_TEXT, "Thanks for taking out time to write to us!\n\n" +
                        "We're here to listen & improve Lubble for you, please write your feedback below:\n\n\n\n");
                Intent mailer = Intent.createChooser(intent, null);
                startActivity(mailer);
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        userRef = getUserRef(userId);
        fetchProfileFeed();
        fetchDevMenu();
    }

    private void fetchProfileFeed() {
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                profileData = dataSnapshot.getValue(ProfileData.class);
                assert profileData != null;
                userName.setText(profileData.getInfo().getName());
                locality.setText(profileData.getLocality());
                if (isValidString(profileData.getBio())) {
                    userBio.setText(profileData.getBio());
                } else if (userId.equalsIgnoreCase(FirebaseAuth.getInstance().getUid())) {
                    userBio.setText("Edit Profile to add a bio");
                } else {
                    userBio.setText("This user has no bio :(");
                }
                if (userId.equalsIgnoreCase(FirebaseAuth.getInstance().getUid())) {
                    editProfileTV.setVisibility(View.VISIBLE);
                    referralCard.setVisibility(View.VISIBLE);
                    logoutTv.setVisibility(View.VISIBLE);
                } else {
                    editProfileTV.setVisibility(View.GONE);
                    referralCard.setVisibility(View.GONE);
                    logoutTv.setVisibility(View.GONE);
                }
                GlideApp.with(getContext())
                        .load(profileData.getProfilePic())
                        .error(R.drawable.ic_account_circle_black_no_padding)
                        .placeholder(R.drawable.circle)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                progressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
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

    private void fetchDevMenu() {
        RealtimeDbHelper.getDevRef().child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null && dataSnapshot.getValue(Boolean.class)) {
                    toggleDevMenu(true);
                } else {
                    toggleDevMenu(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void toggleDevMenu(boolean isDev) {
        Spinner envoSpinner = rootView.findViewById(R.id.spinner_envo);
        envoSpinner.setVisibility(isDev ? View.VISIBLE : View.GONE);
        String[] envos = new String[3];
        envos[0] = "Select Environment...";
        envos[1] = "DEV";
        envos[2] = "STAGING";
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, envos);

        envoSpinner.setAdapter(adapter);
        envoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1) {
                    LubbleSharedPrefs.getInstance().setLubbleId("DEV");
                    UserUtils.logout(getActivity());
                } else if (position == 2) {
                    LubbleSharedPrefs.getInstance().setLubbleId("STAGING");
                    UserUtils.logout(getActivity());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
