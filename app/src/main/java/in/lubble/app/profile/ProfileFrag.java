package in.lubble.app.profile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import in.lubble.app.R;
import in.lubble.app.UserSharedPrefs;
import in.lubble.app.models.ProfileData;
import in.lubble.app.utils.FragUtils;
import in.lubble.app.utils.UiUtils;

public class ProfileFrag extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "ProfileFrag";
    private static final String ARG_USER_ID = "arg_user_id";

    private View rootView;
    private SwipeRefreshLayout swipeLayout;
    private String userId;
    private ImageView coverPicIv;
    private ImageView profilePicIv;
    private TextView userName;
    private TextView locality;
    private TextView userBio;
    private TextView editProfileTV;
    private RecyclerView recyclerView;

    public ProfileFrag() {
        // Required empty public constructor
    }

    public static ProfileFrag newInstance(String profileId) {
        ProfileFrag fragment = new ProfileFrag();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, profileId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        swipeLayout = rootView.findViewById(R.id.swipeLayout_profile_feed);
        coverPicIv = rootView.findViewById(R.id.iv_cover);
        profilePicIv = rootView.findViewById(R.id.iv_profilePic);
        userName = rootView.findViewById(R.id.tv_name);
        locality = rootView.findViewById(R.id.tv_locality);
        userBio = rootView.findViewById(R.id.tv_bio);
        editProfileTV = rootView.findViewById(R.id.tv_editProfile);
        recyclerView = rootView.findViewById(R.id.rv_profile_feed);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setProgressViewOffset(false, 0, UiUtils.dpToPx(48));

        if (userId.equalsIgnoreCase(UserSharedPrefs.getInstance().getUserId())) {
            // todo fetch cached profile from DB
            /*ProfileData profileData = DbSingleton.getInstance().readProfileData(userId);
            updateProfileData(profileData);
            editProfileTV.setVisibility(View.VISIBLE);*/
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setNestedScrollingEnabled(false);
        fetchProfileFeed();

        profilePicIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //// TODO: 21/11/17 open pic full screen with edit option if its user's profile
            }
        });

        editProfileTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragUtils.addFrag(getFragmentManager(), R.id.frameLayout_fragContainer, EditProfileFrag.newInstance());
            }
        });

        return rootView;
    }

    private void fetchProfileFeed() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ProfileData profileData = dataSnapshot.getValue(ProfileData.class);
                userName.setText(profileData.getName());
                locality.setText(profileData.getLocality());
                userBio.setText(profileData.getBio());
                if (userId.equalsIgnoreCase(FirebaseAuth.getInstance().getUid())) {
                    editProfileTV.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        /*swipeLayout.setRefreshing(true);
        NetworkClient apiClient = createService();
        Call<ProfileData> profileCall = apiClient.getProfile(userId);
        profileCall.enqueue(new Callback<ProfileData>() {
            @Override
            public void onResponse(Call<ProfileData> call, Response<ProfileData> response) {
                swipeLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    ProfileData profileData = response.body();
                    DbSingleton.getInstance().createProfileData(profileData);
                    updateProfileData(profileData);

                    List<PostData> postDataList = profileData.getPostDataList();
                    if (postDataList.isEmpty()) {
                        //// TODO: 17/11/17 show empty layout
                        return;
                    }
                    FeedAdapter feedAdapter = new FeedAdapter(getContext(), profileData);
                    feedAdapter.addAll(postDataList);
                    recyclerView.setAdapter(feedAdapter);
                } else {
                    Toast.makeText(getContext(), "Whoops!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProfileData> call, Throwable t) {
                swipeLayout.setRefreshing(false);
                d(TAG, "onFailure: ");
                Snackbar.make(rootView, "Check connection", Snackbar.LENGTH_SHORT).show();
            }
        });*/
    }

    private void updateProfileData(ProfileData profileData) {
        /*GlideApp.with(getContext()).load(BASE_MEDIA_URL + profileData.getCoverPic())
                .placeholder(R.drawable.cover_pic)
                .into(coverPicIv);
        GlideApp.with(getContext()).load(BASE_MEDIA_URL + profileData.getDp())
                .placeholder(R.drawable.ic_account_circle_black_no_padding)
                .into(profilePicIv);
        userName.setText(profileData.getName());
        locality.setText(profileData.getLocality());
        String bio;
        if (StringUtils.isValidString(profileData.getBio())) {
            bio = profileData.getBio();
        } else {
            bio = getString(R.string.bio_default);
        }
        userBio.setText(bio);*/
    }

    @Override
    public void onRefresh() {
        fetchProfileFeed();
    }
}
