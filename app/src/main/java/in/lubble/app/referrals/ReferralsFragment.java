package in.lubble.app.referrals;

import android.content.Intent;
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

import static in.lubble.app.utils.StringUtils.isValidString;

public class ReferralsFragment extends Fragment {

    private static final String TAG = "ReferralsFragment";

    private ProgressBar leaderboardProgressBar;
    private LinearLayout fbContainer;
    private ReferralLeaderboardAdapter adapter;
    private String sharingUrl;

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
        RecyclerView rv = view.findViewById(R.id.rv_leaderboard);
        rv.setNestedScrollingEnabled(false);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReferralLeaderboardAdapter(GlideApp.with(getContext()), getContext());
        rv.setAdapter(adapter);

        generateBranchUrl();
        fetchReferralLeaderboard();
        return view;
    }

    private BranchUniversalObject branchUniversalObject;

    private void generateBranchUrl() {

        branchUniversalObject = new BranchUniversalObject()
                .setCanonicalIdentifier("lbl/referralCode/" + FirebaseAuth.getInstance().getUid())
                .setTitle("Test Title")
                .setContentDescription("Test Description")
                .setContentImageUrl("https://via.placeholder.com/300x300")
                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setContentMetadata(new ContentMetadata().addCustomMetadata("key1", "value1"));

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
                    /*if (sharingUrlProgressDialog != null) {
                        sharingUrlProgressDialog.dismiss();
                        Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_LONG).show();
                    }*/
                    initClickHandlers(linkProperties);
                } else {
                    Log.i("BRANCH SDK", "got my Branch link to share: " + url);
                }
            }
        });
    }

    private void initClickHandlers(final LinkProperties linkProperties) {
        fbContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(sharingUrl)) {
                    if (isValidString(sharingUrl)) {
                        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Test Subject");
                        String message = "test msg ";
                        sharingIntent.putExtra(Intent.EXTRA_TEXT, message + sharingUrl);
                        startActivity(Intent.createChooser(sharingIntent, "Share with:"));
                        Analytics.triggerEvent(AnalyticsEvents.REFERRAL_MORE_SHARE, getContext());
                    } else {
                        /// TODO: 15/9/18
                    }
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
