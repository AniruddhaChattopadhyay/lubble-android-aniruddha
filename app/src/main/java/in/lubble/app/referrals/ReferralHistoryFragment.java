package in.lubble.app.referrals;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReferralHistoryFragment extends Fragment {

    private static final String TAG = "ReferralsFragment";

    private ReferralHistoryAdapter adapter;
    private TextView totalPointsTv;
    private TextView totalPointsHintTv;
    private ProgressBar progressBar;
    private RecyclerView rv;
    private LinearLayout noHistoryContainer;

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
        noHistoryContainer = view.findViewById(R.id.container_no_history);
        rv.setNestedScrollingEnabled(false);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReferralHistoryAdapter(GlideApp.with(getContext()), getContext());
        rv.setAdapter(adapter);

        fetchReferralHistory();

        return view;
    }

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
                        for (ReferralPersonData referralPersonData : referralHistoryData.getReferralPersonData()) {
                            adapter.addReferral(referralPersonData);
                        }
                    } else {
                        rv.setVisibility(View.GONE);
                        noHistoryContainer.setVisibility(View.VISIBLE);
                    }

                } else if (isAdded() && isVisible()) {
                    Crashlytics.log("referral history bad response");
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

}
