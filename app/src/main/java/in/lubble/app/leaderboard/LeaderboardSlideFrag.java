package in.lubble.app.leaderboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.models.ProfileData;
import in.lubble.app.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeaderboardSlideFrag extends Fragment {
    private static final String ARG_SLIDER_DATA = "ARG_SLIDER_DATA";

    private ImageView firstIv;
    private TextView firstNameTv;

    private ImageView secondIv;
    private TextView secondNameTv;

    private ImageView thirdIv;
    private TextView thirdNameTv;

    public LeaderboardSlideFrag() {
        // Required empty public constructor
    }

    public static LeaderboardSlideFrag newInstance() {
        LeaderboardSlideFrag fragment = new LeaderboardSlideFrag();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_leaderboard_slide, container, false);

        firstIv = view.findViewById(R.id.iv_first);
        firstNameTv = view.findViewById(R.id.tv_name_first);

        secondIv = view.findViewById(R.id.iv_second);
        secondNameTv = view.findViewById(R.id.tv_name_second);

        thirdIv = view.findViewById(R.id.iv_third);
        thirdNameTv = view.findViewById(R.id.tv_name_third);


        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LeaderboardActivity.open(requireContext());
            }
        });

        fetchAllLubbleUsers();

        return view;
    }

    private void fetchAllLubbleUsers() {
        FirebaseDatabase.getInstance().getReference("users").orderByChild("likes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
                    Collections.reverse(profileDataList);
                    setTop3(profileDataList.subList(0, 3));
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
        GlideApp.with(requireContext()).load(firstUser.getInfo().getThumbnail()).circleCrop()
                .placeholder(R.drawable.ic_account_circle_black_no_padding)
                .error(R.drawable.ic_account_circle_black_no_padding)
                .into(firstIv);

        final ProfileData secondUser = top3List.get(1);
        secondNameTv.setText(StringUtils.getTitleCase(secondUser.getInfo().getName()));
        GlideApp.with(requireContext()).load(secondUser.getInfo().getThumbnail()).circleCrop()
                .placeholder(R.drawable.ic_account_circle_black_no_padding)
                .error(R.drawable.ic_account_circle_black_no_padding)
                .into(secondIv);

        final ProfileData thirdUser = top3List.get(2);
        thirdNameTv.setText(StringUtils.getTitleCase(thirdUser.getInfo().getName()));
        GlideApp.with(requireContext()).load(thirdUser.getInfo().getThumbnail()).circleCrop()
                .placeholder(R.drawable.ic_account_circle_black_no_padding)
                .error(R.drawable.ic_account_circle_black_no_padding)
                .into(thirdIv);
    }

}
