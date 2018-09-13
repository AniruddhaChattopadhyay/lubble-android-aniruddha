package in.lubble.app.referrals;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;

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

        RecyclerView rv = view.findViewById(R.id.rv_referral_history);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReferralHistoryAdapter(GlideApp.with(getContext()), getContext());
        rv.setAdapter(adapter);

        fetchReferralHistory();

        return view;
    }

    private void fetchReferralHistory() {
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.fetchReferralHistory().enqueue(new Callback<ArrayList<ReferralPersonData>>() {
            @Override
            public void onResponse(Call<ArrayList<ReferralPersonData>> call, Response<ArrayList<ReferralPersonData>> response) {
                /// TODO: 13/9/18  progressBar.setVisibility(View.GONE);
                final ArrayList<ReferralPersonData> referralPersonList = response.body();
                if (response.isSuccessful() && referralPersonList != null && isAdded() && isVisible()) {

                    for (ReferralPersonData referralPersonData : referralPersonList) {
                        adapter.addReferral(referralPersonData);
                    }

                } else if (isAdded() && isVisible()) {
                    Crashlytics.log("referral history bad response");
                    Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<ReferralPersonData>> call, Throwable t) {
                if (isAdded() && isVisible()) {
                    Log.e(TAG, "onFailure: ");
                    /// TODO: 13/9/18  progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), R.string.check_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
