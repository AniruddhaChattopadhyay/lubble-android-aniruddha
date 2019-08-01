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
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.models.ProfileData;
import in.lubble.app.profile.ProfileActivity;
import in.lubble.app.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeaderboardFrag extends Fragment implements OnListFragmentInteractionListener {

    private OnListFragmentInteractionListener mListener;
    private LeaderboardAdapter adapter;
    private static final String TAG = "LeaderboardFrag";

    private TextView subtitleTv;

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

        RecyclerView recyclerView = view.findViewById(R.id.rv_leaderboard);
        recyclerView.setNestedScrollingEnabled(true);
        progressbar = view.findViewById(R.id.progressbar_leaderboard);
        subtitleTv = view.findViewById(R.id.tv_subtitle);

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

        subtitleTv.setText(String.format("in %s", LubbleSharedPrefs.getInstance().getLubbleId()));

        fetchAllLubbleUsers();

        return view;
    }

    private void fetchAllLubbleUsers() {
        progressbar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference("users").orderByChild("likes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                adapter.clear();
                List<ProfileData> profileDataList = new ArrayList<>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if (child.hasChild("lubbles/" + LubbleSharedPrefs.getInstance().requireLubbleId())) {
                        final ProfileData profileData = child.getValue(ProfileData.class);
                        if (profileData != null && profileData.getInfo() != null &&
                                (profileData.getInfo().getBadge() == null || !profileData.getInfo().getBadge().equalsIgnoreCase("admin"))) {
                            profileData.setId(child.getKey());
                            profileDataList.add(profileData);
                        }
                    }
                }
                if (isAdded()) {
                    progressbar.setVisibility(View.GONE);
                    Collections.reverse(profileDataList);
                    setTop3(profileDataList.subList(0, 3));
                    adapter.addList(profileDataList.subList(3, 10));
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
        firstPointsTv.setText(String.valueOf(firstUser.getLikes()));
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
        secondPointsTv.setText(String.valueOf(secondUser.getLikes()));
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
        thirdPointsTv.setText(String.valueOf(thirdUser.getLikes()));
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
