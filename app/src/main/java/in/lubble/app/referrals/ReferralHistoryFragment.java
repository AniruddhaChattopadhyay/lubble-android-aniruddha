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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.List;

import in.lubble.app.GlideApp;
import in.lubble.app.LubbleApp;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static in.lubble.app.utils.ReferralUtils.generateBranchUrl;
import static in.lubble.app.utils.ReferralUtils.getReferralIntent;

public class ReferralHistoryFragment extends Fragment {

    private static final String TAG = "ReferralsFragment";

    private ReferralHistoryAdapter adapter;
    private TextView totalPointsTv;
    private TextView totalPointsHintTv;
    private ProgressBar progressBar;
    private LinearLayout fbContainer;
    private LinearLayout copyLinkContainer;
    private LinearLayout whatsappContainer;
    private LinearLayout moreContainer;
    private RecyclerView rv;
    private TextView noReferralsTv;
    private LinearLayout noHistoryContainer;
    private String sharingUrl;
    private ProgressDialog sharingProgressDialog;

    public ReferralHistoryFragment() {
        // Required empty public constructor
    }

    public static ReferralHistoryFragment newInstance() {
        return new ReferralHistoryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_referral_history, container, false);

        progressBar = view.findViewById(R.id.progress_bar_history);
        totalPointsTv = view.findViewById(R.id.tv_total_points);
        totalPointsHintTv = view.findViewById(R.id.tv_total_points_hint);
        rv = view.findViewById(R.id.rv_referral_history);
        noReferralsTv = view.findViewById(R.id.tv_no_referrals);
        noHistoryContainer = view.findViewById(R.id.container_no_history);
        copyLinkContainer = view.findViewById(R.id.container_copy_link);
        fbContainer = view.findViewById(R.id.container_fb);
        whatsappContainer = view.findViewById(R.id.container_whatsapp);
        moreContainer = view.findViewById(R.id.container_more);
        rv.setNestedScrollingEnabled(false);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReferralHistoryAdapter(GlideApp.with(getContext()), getContext());
        rv.setAdapter(adapter);

        fetchReferralHistory();
        sharingProgressDialog = new ProgressDialog(getContext());
        generateBranchUrl(getContext(), linkCreateListener);
        initClickHandlers();

        return view;
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
                FirebaseCrashlytics.getInstance().recordException(new IllegalStateException(error.getMessage()));
                if (isAdded() && isVisible()) {
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private void fetchReferralHistory() {
        progressBar.setVisibility(View.VISIBLE);
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.fetchReferralHistory().enqueue(new Callback<ReferralHistoryData>() {
            @Override
            public void onResponse(Call<ReferralHistoryData> call, Response<ReferralHistoryData> response) {
                progressBar.setVisibility(View.GONE);
                final ReferralHistoryData referralHistoryData = response.body();
                if (response.isSuccessful() && referralHistoryData != null && isAdded() && isVisible()) {
                    totalPointsTv.setText(String.valueOf(referralHistoryData.getTotalPoints()));
                    totalPointsHintTv.setText("Total Points");

                    if (referralHistoryData.getReferralPersonData() != null && !referralHistoryData.getReferralPersonData().isEmpty()) {
                        rv.setVisibility(View.VISIBLE);
                        noReferralsTv.setVisibility(View.GONE);
                        for (ReferralPersonData referralPersonData : referralHistoryData.getReferralPersonData()) {
                            adapter.addReferral(referralPersonData);
                        }
                    } else {
                        rv.setVisibility(View.GONE);
                        noReferralsTv.setVisibility(View.VISIBLE);
                    }

                } else if (isAdded() && isVisible()) {
                    FirebaseCrashlytics.getInstance().log("referral history bad response");
                    Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ReferralHistoryData> call, Throwable t) {
                if (isAdded() && isVisible()) {
                    Log.e(TAG, "onFailure: ");
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), R.string.check_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

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
                Intent referralIntent = getReferralIntent(getContext(), sharingUrl, sharingProgressDialog, linkCreateListener);
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
                        Toast.makeText(getContext(), "You don't have WhaaaaatsApp! Please share with another app", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        moreContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent referralIntent = getReferralIntent(getContext(), sharingUrl, sharingProgressDialog, linkCreateListener);
                if (referralIntent != null) {
                    startActivity(Intent.createChooser(referralIntent, getString(R.string.refer_share_title)));
                    Analytics.triggerEvent(AnalyticsEvents.REFERRAL_MORE_SHARE, getContext());
                }
            }
        });
    }

}
