package in.lubble.app.groups;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.chat.ChatActivity;
import in.lubble.app.marketplace.SliderData;
import in.lubble.app.marketplace.SliderViewPagerAdapter;
import in.lubble.app.models.DmData;
import in.lubble.app.models.GroupData;
import in.lubble.app.models.ProfileInfo;
import in.lubble.app.models.UserGroupData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.utils.UiUtils;
import me.crosswall.lib.coverflow.CoverFlow;
import me.crosswall.lib.coverflow.core.PagerContainer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static in.lubble.app.chat.ChatActivity.EXTRA_DM_ID;
import static in.lubble.app.chat.ChatActivity.EXTRA_GROUP_ID;
import static in.lubble.app.chat.ChatActivity.EXTRA_IS_JOINING;
import static in.lubble.app.chat.ChatActivity.EXTRA_RECEIVER_DP_URL;
import static in.lubble.app.chat.ChatActivity.EXTRA_RECEIVER_NAME;
import static in.lubble.app.firebase.RealtimeDbHelper.getDmsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getLubbleGroupsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getSellerRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserDmsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserGroupsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserInfoRef;

public class GroupListFragment extends Fragment implements OnListFragmentInteractionListener {

    private static final String TAG = "GroupListFragment";

    private OnListFragmentInteractionListener mListener;
    private LinearLayout newGroupContainer;
    private GroupRecyclerAdapter adapter;
    private HashMap<Query, ValueEventListener> map = new HashMap<>();
    private HashMap<Query, ValueEventListener> dmListenersMap = new HashMap<>();
    private ChildEventListener joinedGroupListener;
    private ChildEventListener unjoinedGroupListener;
    private ChildEventListener userDmsListener;
    private RecyclerView groupsRecyclerView;
    private ProgressBar progressBar;
    private HashMap<String, Set<String>> groupInvitedByMap;
    private HashMap<String, UserGroupData> userGroupDataMap;
    private HashMap<String, Long> dmMap;
    private PagerContainer pagerContainer;
    private ViewPager viewPager;
    private int currentPage = 0;
    private Handler handler = new Handler();
    private ArrayList<SliderData> sliderDataList = new ArrayList<>();

    public GroupListFragment() {
    }

    public static GroupListFragment newInstance() {
        return new GroupListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_item, container, false);
        Context context = view.getContext();

        NestedScrollView nestedScrollView = view.findViewById(R.id.container_scrollview);
        groupsRecyclerView = view.findViewById(R.id.rv_groups);
        progressBar = view.findViewById(R.id.progressBar_groups);
        newGroupContainer = view.findViewById(R.id.container_create_group);
        pagerContainer = view.findViewById(R.id.pager_container);
        viewPager = view.findViewById(R.id.viewpager);
        TabLayout tabLayout = view.findViewById(R.id.tab_dots);
        tabLayout.setupWithViewPager(viewPager, true);
        viewPager.setClipChildren(false);
        viewPager.setOffscreenPageLimit(4);
        groupsRecyclerView.setNestedScrollingEnabled(false);

        groupInvitedByMap = new HashMap<>();
        userGroupDataMap = new HashMap<>();
        dmMap = new HashMap<>();
        Analytics.triggerScreenEvent(getContext(), this.getClass());

        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        adapter = new GroupRecyclerAdapter(mListener);
        groupsRecyclerView.setAdapter(adapter);
        DividerItemDecoration itemDecor = new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL);
        groupsRecyclerView.addItemDecoration(itemDecor);

        newGroupContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), NewGroupActivity.class));
            }
        });

        final SliderData sliderData = new SliderData();
        sliderDataList.add(sliderData);

        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > oldScrollY) {
                    UiUtils.animateSlideDownHide(getContext(), newGroupContainer);
                } else {
                    UiUtils.animateSlideUpShow(getContext(), newGroupContainer);
                }
            }
        });

        setupSlider();
        fetchHomeBanners();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        adapter.clearGroups();
        groupsRecyclerView.setVisibility(View.INVISIBLE);
        syncUserGroupIds();
        syncUserDmIds();
        syncSellerDmIds();
    }

    private void setupSlider() {
        viewPager.setAdapter(new SliderViewPagerAdapter(getChildFragmentManager(), sliderDataList));

        new CoverFlow.Builder()
                .with(viewPager)
                .pagerMargin(getResources().getDimensionPixelSize(R.dimen.pager_margin))
                .scale(0.3f)
                .spaceSize(0f)
                .rotationY(0f)
                .build();

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentPage = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(update);
            }
        }, 7000, 5000);
    }

    private Runnable update = new Runnable() {
        public void run() {
            if (currentPage == sliderDataList.size()) {
                currentPage = 0;
            }
            viewPager.setCurrentItem(currentPage++, true);
        }
    };

    private void fetchHomeBanners() {

        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.fetchHomeData().enqueue(new Callback<ArrayList<SliderData>>() {
            @Override
            public void onResponse(Call<ArrayList<SliderData>> call, Response<ArrayList<SliderData>> response) {
                final ArrayList<SliderData> bannerDataList = response.body();
                if (response.isSuccessful() && bannerDataList != null && isAdded() && !bannerDataList.isEmpty()) {
                    sliderDataList = bannerDataList;
                    setupSlider();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<SliderData>> call, Throwable t) {
                Log.e(TAG, "onFailure: ");
            }
        });

    }

    private void syncSellerDmIds() {
        getSellerRef().child(String.valueOf(LubbleSharedPrefs.getInstance().getSellerId())).child("dms").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                final String dmId = dataSnapshot.getKey();
                if (dmId != null) {
                    HashMap<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();
                    Long count = 0L;
                    if (map != null && map.containsKey("unreadCount")) {
                        count = (Long) map.get("unreadCount");
                    }
                    dmMap.put(dmId, count);
                    fetchDmFrom(dmId);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                HashMap<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();
                Long count = 0L;
                if (map != null && map.containsKey("unreadCount")) {
                    count = (Long) map.get("unreadCount");
                }
                dmMap.put(dataSnapshot.getKey(), count);
                fetchDmFrom(dataSnapshot.getKey());
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void syncUserDmIds() {

        userDmsListener = getUserDmsRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                final String dmId = dataSnapshot.getKey();
                if (dmId != null) {
                    HashMap<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();
                    Long count = 0L;
                    if (map != null && map.containsKey("unreadCount")) {
                        count = (Long) map.get("unreadCount");
                    }
                    dmMap.put(dmId, count);
                    fetchDmFrom(dmId);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                HashMap<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();
                Long count = 0L;
                if (map != null && map.containsKey("unreadCount")) {
                    count = (Long) map.get("unreadCount");
                }
                dmMap.put(dataSnapshot.getKey(), count);
                fetchDmFrom(dataSnapshot.getKey());
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getKey() != null) {
                    // remove from list
                    adapter.removeGroup(dataSnapshot.getKey());
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchDmFrom(@NonNull final String dmId) {
        final ValueEventListener dmValueListener = getDmsRef().child(dmId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final DmData dmData = dataSnapshot.getValue(DmData.class);
                if (dmData != null) {
                    dmData.setId(dataSnapshot.getKey());

                    final GroupData dmGroupData = new GroupData();
                    dmGroupData.setId(dmData.getId());
                    dmGroupData.setLastMessage(dmData.getLastMessage());
                    dmGroupData.setLastMessageTimestamp(dmData.getLastMessageTimestamp());
                    dmGroupData.setIsPrivate(true);

                    final HashMap<String, Object> members = dmData.getMembers();
                    for (String profileId : members.keySet()) {
                        final String sellerId = String.valueOf(LubbleSharedPrefs.getInstance().getSellerId());
                        if (!FirebaseAuth.getInstance().getUid().equalsIgnoreCase(profileId) && !sellerId.equalsIgnoreCase(profileId)) {
                            final HashMap<String, Object> profileMap = (HashMap<String, Object>) members.get(profileId);
                            if (profileMap != null) {
                                final boolean isSeller = (boolean) profileMap.get("isSeller");
                                if (isSeller) {
                                    fetchSellerProfileFrom(profileId, dmGroupData);
                                } else {
                                    fetchProfileFrom(profileId, dmGroupData);
                                }
                            }
                        }
                    }
                }
            }

            private void fetchProfileFrom(String profileId, final GroupData dmGroupData) {
                getUserInfoRef(profileId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        HashMap<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();
                        if (map != null) {
                            final ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                            if (profileInfo != null) {
                                profileInfo.setId(dataSnapshot.getRef().getParent().getKey()); // this works. Don't touch.
                                dmGroupData.setTitle(profileInfo.getName());
                                dmGroupData.setThumbnail(profileInfo.getThumbnail());
                                final UserGroupData userGroupData = new UserGroupData();
                                userGroupData.setJoined(true);
                                userGroupData.setUnreadCount(dmMap.get(dmGroupData.getId()));
                                adapter.addGroup(dmGroupData, userGroupData);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            private synchronized void fetchSellerProfileFrom(String profileId, final GroupData dmGroupData) {
                getSellerRef().child(profileId).child("info").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                        if (profileInfo != null) {
                            profileInfo.setId(dataSnapshot.getKey());
                            dmGroupData.setTitle(profileInfo.getName());
                            dmGroupData.setThumbnail(profileInfo.getThumbnail());
                            final UserGroupData userGroupData = new UserGroupData();
                            userGroupData.setJoined(true);
                            userGroupData.setUnreadCount(dmMap.get(dmGroupData.getId()));
                            adapter.addGroup(dmGroupData, userGroupData);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        dmListenersMap.put(getDmsRef().child(dmId), dmValueListener);
    }

    private void syncUserGroupIds() {
        // gets list of group IDs joined by the user
        joinedGroupListener = getUserGroupsRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final UserGroupData userGroupData = dataSnapshot.getValue(UserGroupData.class);
                userGroupDataMap.put(dataSnapshot.getKey(), userGroupData);
                if (userGroupData.isJoined()) {
                    syncJoinedGroups(dataSnapshot.getKey());
                } else {
                    if (userGroupData.getInvitedBy() != null) {
                        groupInvitedByMap.put(dataSnapshot.getKey(), userGroupData.getInvitedBy().keySet());
                        syncInvitedGroups(dataSnapshot.getKey());
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                final UserGroupData userGroupData = dataSnapshot.getValue(UserGroupData.class);
                userGroupDataMap.put(dataSnapshot.getKey(), userGroupData);
                if (userGroupData.isJoined()) {
                    syncJoinedGroups(dataSnapshot.getKey());
                } else {
                    groupInvitedByMap.put(dataSnapshot.getKey(), userGroupData.getInvitedBy().keySet());
                    syncInvitedGroups(dataSnapshot.getKey());
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getKey() != null) {
                    // remove from list
                    adapter.removeGroup(dataSnapshot.getKey());
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        // single listener as this just needs to be called once after all joined groups have been synced.
        getUserGroupsRef().orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // all user groups have been synced now
                if (progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(View.GONE);
                }
                ArrayList<String> list = new ArrayList<>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    list.add(child.getKey());
                }
                syncAllPublicGroups(list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void syncJoinedGroups(String groupId) {
        // get meta data of the groups joined by the user
        final ValueEventListener joinedGroupListener = getLubbleGroupsRef().child(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GroupData groupData = dataSnapshot.getValue(GroupData.class);

                final UserGroupData userGroupData = userGroupDataMap.get(dataSnapshot.getKey());
                if (groupData != null && groupData.getId() != null) {
                    adapter.addGroup(groupData, userGroupData);
                    groupsRecyclerView.scrollToPosition(0);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        map.put(getLubbleGroupsRef().child(groupId), joinedGroupListener);
    }

    private void syncInvitedGroups(final String groupId) {
        // get meta data of the groups joined by the user
        final ValueEventListener joinedGroupListener = getLubbleGroupsRef().child(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        GroupData groupData = dataSnapshot.getValue(GroupData.class);
                        if (groupData != null) {
                            if (groupData.getMembers().get(FirebaseAuth.getInstance().getUid()) == null) {
                                groupData.setInvitedBy(groupInvitedByMap.get(groupData.getId()));
                            }
                            final UserGroupData userGroupData = userGroupDataMap.get(dataSnapshot.getKey());
                            adapter.addGroup(groupData, userGroupData);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        map.put(getLubbleGroupsRef().child(groupId), joinedGroupListener);
    }

    private void syncAllPublicGroups(final ArrayList<String> joinedGroupIdList) {
        unjoinedGroupListener = getLubbleGroupsRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final GroupData unJoinedGroup = dataSnapshot.getValue(GroupData.class);
                if (unJoinedGroup != null && unJoinedGroup.getId() != null && !joinedGroupIdList.contains(unJoinedGroup.getId()) && !unJoinedGroup.getIsPrivate()) {
                    adapter.addPublicGroup(unJoinedGroup);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        getLubbleGroupsRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                groupsRecyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = this;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (joinedGroupListener != null) {
            getUserGroupsRef().removeEventListener(joinedGroupListener);
        }
        if (unjoinedGroupListener != null) {
            getLubbleGroupsRef().removeEventListener(unjoinedGroupListener);
        }
        if (userDmsListener != null) {
            getUserDmsRef().removeEventListener(userDmsListener);
        }
        for (Query query : map.keySet()) {
            query.removeEventListener(map.get(query));
        }
        for (Query query : dmListenersMap.keySet()) {
            query.removeEventListener(dmListenersMap.get(query));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListFragmentInteraction(String groupId, boolean isJoining) {
        final Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        intent.putExtra(EXTRA_IS_JOINING, isJoining);
        startActivity(intent);
    }

    @Override
    public void onDmClick(String dmId, String name, String thumbnailUrl) {
        final Intent intent = new Intent(getContext(), ChatActivity.class);
        intent.putExtra(EXTRA_DM_ID, dmId);
        intent.putExtra(EXTRA_RECEIVER_NAME, name);
        intent.putExtra(EXTRA_RECEIVER_DP_URL, thumbnailUrl);
        startActivity(intent);
    }
}
