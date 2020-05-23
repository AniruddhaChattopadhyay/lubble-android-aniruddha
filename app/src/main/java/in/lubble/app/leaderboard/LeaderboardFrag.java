package in.lubble.app.leaderboard;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.models.ProfileData;
import in.lubble.app.profile.ProfileActivity;
import in.lubble.app.utils.DateTimeUtils;
import in.lubble.app.utils.StringUtils;
import in.lubble.app.utils.mapUtils.MathUtil;

public class LeaderboardFrag extends Fragment implements OnListFragmentInteractionListener {

    private OnListFragmentInteractionListener mListener;
    private LeaderboardAdapter adapter;
    private static final String TAG = "LeaderboardFrag";

    private TextView subtitleTv, explainTv, titleTv;
    private ImageView logoIv;

    private RelativeLayout firstContainer;
    private ImageView firstIv;
    private TextView firstNameTv;
    private TextView firstPointsTv;

    private RelativeLayout secondContainer;
    private ImageView secondIv;
    private TextView secondNameTv;
    private TextView secondPointsTv;

    private RelativeLayout thirdContainer;
    private ImageView thirdIv;
    private TextView thirdNameTv;
    private TextView thirdPointsTv;
    private ProgressBar progressbar;

    public LeaderboardFrag() {
    }

    public static LeaderboardFrag newInstance() {
        LeaderboardFrag fragment = new LeaderboardFrag();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_leaderboard, container, false);

        Context context = view.getContext();
        Analytics.triggerScreenEvent(getContext(), this.getClass());

        RecyclerView recyclerView = view.findViewById(R.id.rv_leaderboard);
        recyclerView.setNestedScrollingEnabled(true);
        progressbar = view.findViewById(R.id.progressbar_leaderboard);
        titleTv = view.findViewById(R.id.tv_title);
        subtitleTv = view.findViewById(R.id.tv_subtitle);
        explainTv = view.findViewById(R.id.tv_explain);
        logoIv = view.findViewById(R.id.iv_logo);

        ImageView crossIv = view.findViewById(R.id.iv_cross);

        crossIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.none, R.anim.slide_to_bottom);
            }
        });

        firstContainer = view.findViewById(R.id.container_first);
        firstIv = view.findViewById(R.id.iv_first);
        firstNameTv = view.findViewById(R.id.tv_name_first);
        firstPointsTv = view.findViewById(R.id.tv_likes_first);

        secondContainer = view.findViewById(R.id.container_second);
        secondIv = view.findViewById(R.id.iv_second);
        secondNameTv = view.findViewById(R.id.tv_name_second);
        secondPointsTv = view.findViewById(R.id.tv_likes_second);

        thirdContainer = view.findViewById(R.id.container_third);
        thirdIv = view.findViewById(R.id.iv_third);
        thirdNameTv = view.findViewById(R.id.tv_name_third);
        thirdPointsTv = view.findViewById(R.id.tv_likes_third);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        adapter = new LeaderboardAdapter(GlideApp.with(requireContext()), mListener, requireContext());
        recyclerView.setAdapter(adapter);

        titleTv.setText("Most Liked in " + DateTimeUtils.getCurrMonth());
        subtitleTv.setText(String.format("in %s", LubbleSharedPrefs.getInstance().getLubbleName()));

        fetchAllLubbleUsers();

        return view;
    }

    private void fetchAllLubbleUsers() {
        progressbar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference("users").orderByChild("lubbles/" + LubbleSharedPrefs.getInstance().requireLubbleId()).startAt("")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        adapter.clear();
                        List<ProfileData> profileDataList = new ArrayList<>();

                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            if (!(child.getValue() instanceof Boolean)) {
                                final ProfileData profileData = child.getValue(ProfileData.class);
                                if (profileData != null && profileData.getInfo() != null &&
                                        (profileData.getInfo().getBadge() == null || !profileData.getInfo().getBadge().equalsIgnoreCase("admin"))) {
                                    profileData.setId(child.getKey());
                                    profileDataList.add(profileData);
                                }
                            }
                        }
                        Collections.sort(profileDataList, new Comparator<ProfileData>() {
                            @Override
                            public int compare(ProfileData o1, ProfileData o2) {
                                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                                return MathUtil.compareDesc(o1.getMonthly_likes(), o2.getMonthly_likes());
                            }
                        });
                        if (isAdded()) {
                            progressbar.setVisibility(View.GONE);
                            explainTv.setVisibility(View.VISIBLE);
                            logoIv.setVisibility(View.VISIBLE);
                            setTop3(profileDataList.subList(0, 3));
                            int limit = Math.min(10, profileDataList.size());
                            adapter.addList(profileDataList.subList(3, limit));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void setTop3(List<ProfileData> top3List) {
        final ProfileData firstUser = top3List.get(0);
        firstNameTv.setText(StringUtils.getTitleCase(firstUser.getInfo().getName()));
        firstPointsTv.setText(String.valueOf(firstUser.getMonthly_likes()));
        GlideApp.with(requireContext()).load(firstUser.getInfo().getThumbnail()).circleCrop()
                .placeholder(R.drawable.ic_account_circle_black_no_padding)
                .error(R.drawable.ic_account_circle_black_no_padding)
                .into(firstIv);
        firstContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileActivity.open(requireContext(), firstUser.getId());
            }
        });

        final ProfileData secondUser = top3List.get(1);
        secondNameTv.setText(StringUtils.getTitleCase(secondUser.getInfo().getName()));
        secondPointsTv.setText(String.valueOf(secondUser.getMonthly_likes()));
        GlideApp.with(requireContext()).load(secondUser.getInfo().getThumbnail()).circleCrop()
                .placeholder(R.drawable.ic_account_circle_black_no_padding)
                .error(R.drawable.ic_account_circle_black_no_padding)
                .into(secondIv);
        secondContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileActivity.open(requireContext(), secondUser.getId());
            }
        });

        final ProfileData thirdUser = top3List.get(2);
        thirdNameTv.setText(StringUtils.getTitleCase(thirdUser.getInfo().getName()));
        thirdPointsTv.setText(String.valueOf(thirdUser.getMonthly_likes()));
        GlideApp.with(requireContext()).load(thirdUser.getInfo().getThumbnail()).circleCrop()
                .placeholder(R.drawable.ic_account_circle_black_no_padding)
                .error(R.drawable.ic_account_circle_black_no_padding)
                .into(thirdIv);
        thirdContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileActivity.open(requireContext(), thirdUser.getId());
            }
        });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = this;
    }

    @Override
    public void onListFragmentInteraction(String uid) {
        ProfileActivity.open(requireContext(), uid);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}
