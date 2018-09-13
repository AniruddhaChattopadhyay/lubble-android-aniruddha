package in.lubble.app.referrals;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReferralsFragment extends Fragment {

    private static final String TAG = "ReferralsFragment";

    private ProgressBar leaderboardProgressBar;
    private ReferralLeaderboardAdapter adapter;

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
        RecyclerView rv = view.findViewById(R.id.rv_leaderboard);
        rv.setNestedScrollingEnabled(false);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReferralLeaderboardAdapter(GlideApp.with(getContext()), getContext());
        rv.setAdapter(adapter);

        fetchReferralLeaderboard();

        return view;
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
