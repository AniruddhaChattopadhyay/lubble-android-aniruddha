package in.lubble.app.referrals;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleApp;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.ProfileData;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;

import java.util.List;

import static in.lubble.app.firebase.RealtimeDbHelper.getThisUserRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserRef;
import static in.lubble.app.utils.ReferralUtils.generateBranchUrl;
import static in.lubble.app.utils.ReferralUtils.getReferralIntent;

public class ReferralsFragment extends Fragment {

    private static final String TAG = "ReferralsFragment";

    private ImageView referralHeaderIv;
    private TextView myCoinsTv;
    private LinearLayout copyLinkContainer;
    private LinearLayout fbContainer;
    private LinearLayout whatsappContainer;
    private LinearLayout moreContainer;
    private Button bottomInviteBtn;
    private ReferralLeaderboardAdapter adapter;
    private String sharingUrl;
    private ProgressDialog sharingProgressDialog;
    private ValueEventListener referralHdrImgListener;
    private ValueEventListener thisUserListener;

    public ReferralsFragment() {
        // Required empty public constructor
    }

    public static ReferralsFragment newInstance() {
        return new ReferralsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_referrals, container, false);

        referralHeaderIv = view.findViewById(R.id.iv_refer_header);
        copyLinkContainer = view.findViewById(R.id.container_copy_link);
        fbContainer = view.findViewById(R.id.container_fb);
        whatsappContainer = view.findViewById(R.id.container_whatsapp);
        moreContainer = view.findViewById(R.id.container_more);
        bottomInviteBtn = view.findViewById(R.id.btn_bottom_invite);
        RecyclerView rv = view.findViewById(R.id.rv_leaderboard);
        myCoinsTv = view.findViewById(R.id.tv_my_coins);
        rv.setNestedScrollingEnabled(false);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReferralLeaderboardAdapter(GlideApp.with(getContext()), getContext());
        rv.setAdapter(adapter);

        sharingProgressDialog = new ProgressDialog(getContext());
        generateBranchUrl(getContext(), linkCreateListener);
        fetchReferralLeaderboard();
        initClickHandlers();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        referralHdrImgListener = RealtimeDbHelper.getLubbleRef().child("referralHdrImg").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GlideApp.with(getContext())
                        .load(dataSnapshot.getValue(String.class))
                        .placeholder(R.drawable.referral_info)
                        .error(R.drawable.referral_info)
                        .into(referralHeaderIv);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        thisUserListener = getThisUserRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final ProfileData profileData = dataSnapshot.getValue(ProfileData.class);
                if (profileData != null) {
                    myCoinsTv.setText(String.valueOf(profileData.getCoins()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    final Branch.BranchLinkCreateListener linkCreateListener = new Branch.BranchLinkCreateListener() {
        @Override
        public void onLinkCreate(String url, BranchError error) {
            if (url != null) {
                Log.d(TAG, "got my Branch link to share: " + url);
                sharingUrl = url;
                if (sharingProgressDialog != null && sharingProgressDialog.isShowing()) {
                    sharingProgressDialog.dismiss();
                }
            } else {
                Log.e(TAG, "Branch onLinkCreate: " + error.getMessage());
                Crashlytics.logException(new IllegalStateException(error.getMessage()));
                if (isAdded() && isVisible()) {
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private void initClickHandlers() {
        copyLinkContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent referralIntent = getReferralIntent(getContext(), sharingUrl, sharingProgressDialog, linkCreateListener);
                if (referralIntent != null) {
                    ClipboardManager clipboard = (ClipboardManager) LubbleApp.getAppContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("lubble_share_link", sharingUrl);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(requireContext(), "LINK COPIED", Toast.LENGTH_SHORT).show();
                    Analytics.triggerEvent(AnalyticsEvents.REFERRAL_COPY_LINK, getContext());
                } else {
                    Toast.makeText(requireContext(), "RETRY", Toast.LENGTH_SHORT).show();
                }
            }
        });
        fbContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent referralIntent = getReferralIntent(getContext(), sharingUrl, sharingProgressDialog, linkCreateListener);
                if (referralIntent != null) {
                    boolean facebookAppFound = false;
                    List<ResolveInfo> matches = getContext().getPackageManager().queryIntentActivities(referralIntent, 0);
                    for (ResolveInfo info : matches) {
                        if (info.activityInfo.packageName.toLowerCase().startsWith("com.facebook.katana")) {
                            referralIntent.setPackage(info.activityInfo.packageName);
                            facebookAppFound = true;
                            break;
                        }
                    }
                    if (!facebookAppFound) {
                        String sharerUrl = "https://www.facebook.com/sharer/sharer.php?u=" + sharingUrl;
                        referralIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharerUrl));
                    }
                    startActivity(referralIntent);
                    Analytics.triggerEvent(AnalyticsEvents.REFERRAL_FB_SHARE, getContext());
                }
            }
        });
        whatsappContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent referralIntent = getReferralIntent(getContext(), sharingUrl, sharingProgressDialog, linkCreateListener);
                if (referralIntent != null) {
                    PackageManager pm = getContext().getPackageManager();
                    try {
                        PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
                        //Check if package exists or not. If not then code
                        //in catch block will be called
                        referralIntent.setPackage("com.whatsapp");

                        startActivity(Intent.createChooser(referralIntent, getString(R.string.refer_share_title)));

                        Analytics.triggerEvent(AnalyticsEvents.REFERRAL_WA_SHARE, getContext());

                    } catch (PackageManager.NameNotFoundException e) {
                        Toast.makeText(getContext(), "You don't have WhatsApp! Please share it with another app", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        moreContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent referralIntent = getReferralIntent(getContext(), sharingUrl, sharingProgressDialog, linkCreateListener);
                if (referralIntent != null) {
                    startActivity(Intent.createChooser(referralIntent, getString(R.string.refer_share_title)));
                    Analytics.triggerEvent(AnalyticsEvents.REFERRAL_MORE_SHARE, getContext());
                }
            }
        });
        bottomInviteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent referralIntent = getReferralIntent(getContext(), sharingUrl, sharingProgressDialog, linkCreateListener);
                if (referralIntent != null) {
                    startActivity(Intent.createChooser(referralIntent, getString(R.string.refer_share_title)));
                    Analytics.triggerEvent(AnalyticsEvents.REFERRAL_LEADERBOARD_BOTTOM_SHARE, getContext());
                }
            }
        });
    }

    private void fetchReferralLeaderboard() {
        fetchAllLubbleUsers();
    }

    private void fetchAllLubbleUsers() {
        RealtimeDbHelper.getLubbleMembersRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                adapter.clear();
                // get list of all lubble users
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    fetchLubbleMembersProfile(child.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchLubbleMembersProfile(String uid) {
        getUserRef(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ProfileData profileData = dataSnapshot.getValue(ProfileData.class);
                if (profileData != null && profileData.getInfo() != null) {
                    profileData.setId(dataSnapshot.getKey());
                    addPersonToAdapter(profileData, 0);
                }
            }

            private void addPersonToAdapter(ProfileData profileData1, int rank) {
                final LeaderboardPersonData leaderboardPersonData = new LeaderboardPersonData();
                leaderboardPersonData.setUid(profileData1.getId());
                leaderboardPersonData.setName(profileData1.getInfo().getName());
                leaderboardPersonData.setPoints((int) profileData1.getCoins());
                leaderboardPersonData.setThumbnail(profileData1.getInfo().getThumbnail());
                leaderboardPersonData.setCurrentUserRank(rank);
                adapter.addPerson(leaderboardPersonData);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (referralHdrImgListener != null) {
            RealtimeDbHelper.getLubbleRef().child("referralHdrImg").removeEventListener(referralHdrImgListener);
        }
        if (thisUserListener != null) {
            getThisUserRef().removeEventListener(thisUserListener);
        }
    }
}
