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

public class ReferralsFragment extends Fragment {

    private static final String TAG = "ReferralsFragment";

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

        RecyclerView rv = view.findViewById(R.id.rv_leaderboard);
        rv.setNestedScrollingEnabled(false);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReferralLeaderboardAdapter(GlideApp.with(getContext()), getContext());
        rv.setAdapter(adapter);

        getdummylist();

        return view;
    }

    private void getdummylist() {
        for (int i = 0; i < 10; i++) {
            final ReferralLeaderboardData data = new ReferralLeaderboardData();
            data.setName("Bruce Wayne");
            data.setPoints(999);
            data.setThumbnail("https://imgur.com/I80W1Q0.png");

            adapter.addPerson(data);
        }
    }

}
