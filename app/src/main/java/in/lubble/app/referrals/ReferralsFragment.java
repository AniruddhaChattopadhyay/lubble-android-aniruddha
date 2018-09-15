package in.lubble.app.referrals;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.util.ContentMetadata;
import io.branch.referral.util.LinkProperties;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReferralsFragment extends Fragment {

    private static final String TAG = "ReferralsFragment";

    private ProgressBar leaderboardProgressBar;
    private LinearLayout fbContainer;
    private LinearLayout whatsappContainer;
    private LinearLayout moreContainer;
    private ReferralLeaderboardAdapter adapter;
    private String sharingUrl;
    private ProgressDialog sharingProgressDialog;
    private Intent sharingIntent = new Intent(Intent.ACTION_SEND);

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

        leaderboardProgressBar = view.findViewById(R.id.progressbar_leaderboard);
        fbContainer = view.findViewById(R.id.container_fb);
        whatsappContainer = view.findViewById(R.id.container_whatsapp);
        moreContainer = view.findViewById(R.id.container_more);
        RecyclerView rv = view.findViewById(R.id.rv_leaderboard);
        rv.setNestedScrollingEnabled(false);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReferralLeaderboardAdapter(GlideApp.with(getContext()), getContext());
        rv.setAdapter(adapter);

        generateBranchUrl();
        fetchReferralLeaderboard();
        initClickHandlers();

        return view;
    }

    private BranchUniversalObject branchUniversalObject;

    private void generateBranchUrl() {

        branchUniversalObject = new BranchUniversalObject()
                .setCanonicalIdentifier("lbl/referralCode/" + FirebaseAuth.getInstance().getUid())
                .setTitle("Join your neighbours on Lubble")
                .setContentDescription("Know what's happening in your neighbourhood, buy or sell items around you")
                .setContentImageUrl("https://i.imgur.com/JFsrCOs.png")
                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setContentMetadata(new ContentMetadata().addCustomMetadata("referrer_uid", FirebaseAuth.getInstance().getUid()));

        final LinkProperties linkProperties = new LinkProperties()
                .setChannel("Android")
                .setFeature("Referral")
                .addControlParameter("$desktop_url", "https://lubble.in")
                .addControlParameter("$ios_url", "https://lubble.in");

        branchUniversalObject.generateShortUrl(getContext(), linkProperties, new Branch.BranchLinkCreateListener() {
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
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isInviteLinkGeneratedAndIntentReady() {
        if (TextUtils.isEmpty(sharingUrl)) {
            sharingProgressDialog = new ProgressDialog(getContext());
            sharingProgressDialog.setTitle("Generating Invite Link");
            sharingProgressDialog.setMessage(getString(R.string.all_please_wait));
            sharingProgressDialog.show();
            generateBranchUrl();
            return false;
        } else {
            sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Join me & your neighbours on Lubble");
            String message = "Hey,\n\nI would love to invite you to Lubble, a private social network just for you & your neighbours " +
                    "living together in the same society.\n\nJoin Now: " + sharingUrl + "\n\nJoin the Lubble app to" +
                    "\n- Connect & interact with your neighbours" +
                    "\n- Buy & Sell items around you" +
                    "\n- Get nearby recommendations for plumbers & such services" +
                    "\n and get to know the lastest happenings around you!\n\n";
            sharingIntent.putExtra(Intent.EXTRA_TEXT, message + "Check it out: " + sharingUrl);
        }
        return true;
    }

    private void initClickHandlers() {
        fbContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInviteLinkGeneratedAndIntentReady()) {
                    boolean facebookAppFound = false;
                    List<ResolveInfo> matches = getContext().getPackageManager().queryIntentActivities(sharingIntent, 0);
                    for (ResolveInfo info : matches) {
                        if (info.activityInfo.packageName.toLowerCase().startsWith("com.facebook.katana")) {
                            sharingIntent.setPackage(info.activityInfo.packageName);
                            facebookAppFound = true;
                            break;
                        }
                    }
                    if (!facebookAppFound) {
                        String sharerUrl = "https://www.facebook.com/sharer/sharer.php?u=" + sharingUrl;
                        sharingIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharerUrl));
                    }
                    startActivity(sharingIntent);
                    Analytics.triggerEvent(AnalyticsEvents.REFERRAL_FB_SHARE, getContext());
                }
            }
        });
        whatsappContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInviteLinkGeneratedAndIntentReady()) {
                    PackageManager pm = getContext().getPackageManager();
                    try {
                        PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
                        //Check if package exists or not. If not then code
                        //in catch block will be called
                        sharingIntent.setPackage("com.whatsapp");

                        startActivity(Intent.createChooser(sharingIntent, getString(R.string.refer_share_title)));

                        Analytics.triggerEvent(AnalyticsEvents.REFERRAL_WA_SHARE, getContext());

                    } catch (PackageManager.NameNotFoundException e) {
                        Toast.makeText(getContext(), "You don't have WhatsApp whaaaaaaaaat!?!!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        moreContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInviteLinkGeneratedAndIntentReady()) {
                    startActivity(Intent.createChooser(sharingIntent, getString(R.string.refer_share_title)));
                    Analytics.triggerEvent(AnalyticsEvents.REFERRAL_MORE_SHARE, getContext());
                }
            }
        });
    }


    private void fetchReferralLeaderboard() {
        leaderboardProgressBar.setVisibility(View.VISIBLE);
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.fetchReferralLeaderboard().enqueue(new Callback<ReferralLeaderboardData>() {
            @Override
            public void onResponse(Call<ReferralLeaderboardData> call, Response<ReferralLeaderboardData> response) {
                leaderboardProgressBar.setVisibility(View.GONE);
                final ReferralLeaderboardData referralLeaderboardData = response.body();
                if (response.isSuccessful() && referralLeaderboardData != null && isAdded() && isVisible()) {

                    for (LeaderboardPersonData referralPersonData : referralLeaderboardData.getLeaderboardData()) {
                        if (referralPersonData.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                            adapter.addPerson(referralLeaderboardData.getCurrentUser());
                        } else {
                            adapter.addPerson(referralPersonData);
                        }
                    }

                    if (referralLeaderboardData.getCurrentUser().getCurrentUserRank() > 10) {
                        adapter.addPerson(referralLeaderboardData.getCurrentUser());
                    }

                } else if (isAdded() && isVisible()) {
                    Crashlytics.log("referral leaderboard bad response");
                    Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ReferralLeaderboardData> call, Throwable t) {
                if (isAdded() && isVisible()) {
                    Log.e(TAG, "onFailure: ");
                    leaderboardProgressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), R.string.check_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}
