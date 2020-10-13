package in.lubble.app.explore;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.models.GroupData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static in.lubble.app.firebase.RealtimeDbHelper.getLubbleGroupsRef;

public class ExploreFrag extends Fragment implements ExploreGroupAdapter.OnListFragmentInteractionListener {

    private static final String TAG = "ExploreFrag";
    private ExploreGroupAdapter.OnListFragmentInteractionListener mListener;
    private RecyclerView recyclerView;
    private ProgressBar progressbar;
    private TextView joinedAllTv;
    private TextView titleTv;
    private TextView descTv;
    private ExploreGroupAdapter exploreGroupAdapter;
    private boolean isOnboarding;
    private EditText searchEt;
    private Query publicGroupsQuery;

    public ExploreFrag() {
    }

    public static ExploreFrag newInstance() {
        return new ExploreFrag();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Analytics.triggerScreenEvent(getContext(), this.getClass());
        isOnboarding = getActivity() instanceof ExploreGroupAdapter.OnListFragmentInteractionListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);
        Context context = view.getContext();
        joinedAllTv = view.findViewById(R.id.tv_joined_all);
        titleTv = view.findViewById(R.id.tv_title);
        descTv = view.findViewById(R.id.tv_desc);
        recyclerView = view.findViewById(R.id.rv_interest_groups);
        progressbar = view.findViewById(R.id.progressbar);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        searchEt = view.findViewById(R.id.et_search);
        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                exploreGroupAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Log.d(TAG,s+"*******************");
            }
        });
        joinedAllTv.setVisibility(View.GONE);
        if (isOnboarding) {
            titleTv.setVisibility(View.GONE);
            descTv.setVisibility(View.GONE);
        } else {
            titleTv.setVisibility(View.VISIBLE);
            descTv.setVisibility(View.VISIBLE);
        }

        exploreGroupAdapter = new ExploreGroupAdapter(new ArrayList<ExploreGroupData>(), mListener, GlideApp.with(requireContext()), isOnboarding);
        recyclerView.setAdapter(exploreGroupAdapter);
        recyclerView.setItemAnimator(null);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchExploreGroups();
        addGroupsListener();
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).toggleSearchInToolbar(false);
        }
    }

    private void addGroupsListener() {
        publicGroupsQuery = getLubbleGroupsRef().orderByChild("members/" + FirebaseAuth.getInstance().getUid()).endAt(null);
        publicGroupsQuery.addValueEventListener(publicGroupListener);

    }

    private ValueEventListener publicGroupListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            for (DataSnapshot snapshotChild : dataSnapshot.getChildren()) {
                final GroupData groupData = snapshotChild.getValue(GroupData.class);
                if (groupData != null && !groupData.getIsPrivate() && groupData.getId() != null && !TextUtils.isEmpty(groupData.getTitle())
                        && groupData.getMembers().size() > 0) {
                    // non-joined public groups with non-zero members
                    ExploreGroupData exploreGroupData = new ExploreGroupData();
                    exploreGroupData.setFirebaseGroupId(groupData.getId());
                    exploreGroupData.setTitle(groupData.getTitle());
                    exploreGroupData.setPhotoUrl(groupData.getProfilePic());
                    exploreGroupData.setMemberCount(groupData.getMembers().size());
                    exploreGroupData.setLastMessageTimestamp(groupData.getLastMessageTimestamp());
                    exploreGroupAdapter.updateGroup(exploreGroupData);
                }
            }
            publicGroupsQuery.removeEventListener(publicGroupListener);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            FirebaseCrashlytics.getInstance().recordException(databaseError.toException());
        }
    };

    private void fetchExploreGroups() {
        progressbar.setVisibility(View.VISIBLE);
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.fetchExploreGroups(LubbleSharedPrefs.getInstance().requireLubbleId()).enqueue(new Callback<ArrayList<ExploreGroupData>>() {
            @Override
            public void onResponse(Call<ArrayList<ExploreGroupData>> call, Response<ArrayList<ExploreGroupData>> response) {
                final ArrayList<ExploreGroupData> exploreGroupDataList = response.body();
                if (response.isSuccessful() && exploreGroupDataList != null && isAdded() && !exploreGroupDataList.isEmpty() && isVisible()) {
                    progressbar.setVisibility(View.GONE);
                    exploreGroupAdapter.updateList(exploreGroupDataList);
                    if (isOnboarding) {
                        titleTv.setVisibility(View.GONE);
                        descTv.setVisibility(View.GONE);
                    } else {
                        titleTv.setVisibility(View.VISIBLE);
                        descTv.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (isAdded() && isVisible()) {
                        progressbar.setVisibility(View.GONE);
                        if (exploreGroupDataList != null && exploreGroupDataList.isEmpty()) {
                            if (isOnboarding) {
                                // exit the explore activity if opened during onboarding & there are no unjoined groups to be shown
                                // will happen if an existing user logs back in and has already joined every group
                                getActivity().finish();
                                getActivity().overridePendingTransition(R.anim.none, R.anim.slide_to_bottom);
                            } else {
                                joinedAllTv.setVisibility(View.VISIBLE);
                                titleTv.setVisibility(View.GONE);
                                descTv.setVisibility(View.GONE);
                            }
                        } else {
                            Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<ExploreGroupData>> call, Throwable t) {
                if (isAdded() && isVisible()) {
                    Toast.makeText(getContext(), R.string.check_internet, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onFailure: ");
                    progressbar.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = this;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListFragmentInteraction(ExploreGroupData item, boolean isAdded) {
        if (getActivity() instanceof ExploreGroupAdapter.OnListFragmentInteractionListener) {
            ((ExploreGroupAdapter.OnListFragmentInteractionListener) getActivity()).onListFragmentInteraction(item, isAdded);
        }
    }

}
