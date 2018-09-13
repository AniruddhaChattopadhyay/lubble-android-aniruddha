package in.lubble.app.referrals;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.lubble.app.GlideApp;
import in.lubble.app.R;

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
        adapter = new ReferralHistoryAdapter(GlideApp.with(getContext()));
        rv.setAdapter(adapter);

        return view;
    }

}
