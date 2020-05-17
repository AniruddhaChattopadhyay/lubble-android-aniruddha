package in.lubble.app.groups;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.crashlytics.android.Crashlytics;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.chat.ChatActivity;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.marketplace.SliderData;
import in.lubble.app.marketplace.SliderViewPagerAdapter;
import in.lubble.app.models.EventData;
import in.lubble.app.models.GroupData;
import in.lubble.app.models.UserGroupData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.utils.UiUtils;
import me.crosswall.lib.coverflow.CoverFlow;
import me.crosswall.lib.coverflow.core.PagerContainer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static in.lubble.app.chat.ChatActivity.EXTRA_GROUP_ID;
import static in.lubble.app.chat.ChatActivity.EXTRA_IS_JOINING;
import static in.lubble.app.firebase.RealtimeDbHelper.getEventsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getLubbleGroupsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getSellerDmsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserDmsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserGroupsRef;
import static in.lubble.app.groups.GroupRecyclerAdapter.TYPE_HEADER;

public class GroupListFragment extends Fragment implements OnListFragmentInteractionListener {

    private static final String TAG = "GroupListFragment";

    private OnListFragmentInteractionListener mListener;
    private LinearLayout newGroupContainer;
    private GroupRecyclerAdapter adapter;
    private HashMap<Query, ValueEventListener> map = new HashMap<>();
    private RecyclerView groupsRecyclerView;
    private TextView noSearchResultsTv;
    private ProgressBar progressBar, progressBarPublicGroups;
    private LinearLayoutManager layoutManager;
    private HashMap<String, Set<String>> groupInvitedByMap;
    private PagerContainer pagerContainer;
    private ViewPager viewPager;
    private Timer sliderTimer;
    private int currentPage = 0;
    private TabLayout tabLayout;
    private Handler handler = new Handler();
    private ArrayList<SliderData> sliderDataList = new ArrayList<>();
    private int totalUnreadCount = 0;
    private Query query, dmQuery, sellerDmQuery;
    private ChildEventListener childEventListener, userGroupsListener, userDmsListener, sellerDmsListener;
    private Trace groupTrace;
    private int queryCounter = 0;
    private boolean isPublicGroupsLoading;

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

        groupsRecyclerView = view.findViewById(R.id.rv_groups);
        noSearchResultsTv = view.findViewById(R.id.tv_search_no_results);
        progressBar = view.findViewById(R.id.progressBar_groups);
        progressBarPublicGroups = view.findViewById(R.id.progress_bar_public_groups);
        newGroupContainer = view.findViewById(R.id.container_create_group);
        pagerContainer = view.findViewById(R.id.pager_container);
        viewPager = view.findViewById(R.id.viewpager);
        tabLayout = view.findViewById(R.id.tab_dots);
        viewPager.setClipChildren(false);
        viewPager.setOffscreenPageLimit(4);

        groupInvitedByMap = new HashMap<>();
        Analytics.triggerScreenEvent(getContext(), this.getClass());

        layoutManager = new LinearLayoutManager(context);
        groupsRecyclerView.setLayoutManager(layoutManager);
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

        /*nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > oldScrollY) {
                    UiUtils.animateSlideDownHide(getContext(), newGroupContainer);
                } else {
                    UiUtils.animateSlideUpShow(getContext(), newGroupContainer);
                }
            }
        });*/

        syncAllGroups();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).toggleSearchInToolbar(true);
        }
        setupSlider();
        fetchHomeBanners();
    }

    private void syncAllGroups() {
        adapter.clearGroups();
        progressBar.setVisibility(View.VISIBLE);
        toggleSearch(false);
        groupsRecyclerView.setVisibility(View.INVISIBLE);
        totalUnreadCount = 0;

        groupTrace = FirebasePerformance.getInstance().newTrace("group_list_trace");
        groupTrace.start();
        queryCounter = 2;
        query = RealtimeDbHelper.getLubbleGroupsRef().orderByChild("members/" + FirebaseAuth.getInstance().getUid()).startAt("");
        dmQuery = RealtimeDbHelper.getDmsRef().orderByChild("members/" + FirebaseAuth.getInstance().getUid()).startAt("");
        if (LubbleSharedPrefs.getInstance().getSellerId() != -1) {
            queryCounter++;
            sellerDmQuery = RealtimeDbHelper.getDmsRef().orderByChild("members/" + LubbleSharedPrefs.getInstance().getSellerId()).startAt("");
        }
        syncUserGroup();
        childEventListener = new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String prevChildKey) {
                final GroupData groupData = dataSnapshot.getValue(GroupData.class);
                if (groupData != null) {
                    if (groupData.getMembers().containsKey(FirebaseAuth.getInstance().getUid()) && groupData.getId() != null) {
                        // joined group
                        HashMap userMap = (HashMap) groupData.getMembers().get(FirebaseAuth.getInstance().getUid());
                        if (userMap != null && !userMap.isEmpty() && userMap.containsKey("unreadCount")) {
                            groupData.setUnreadCount((Long) userMap.get("unreadCount"));
                        }
                        adapter.addGroupToTop(groupData);
                    } else if (!groupData.getIsPrivate() && groupData.getId() != null && !TextUtils.isEmpty(groupData.getTitle())
                            && groupData.getMembers().size() > 0) {
                        // non-joined public groups with non-zero members
                        adapter.addPublicGroupToTop(groupData);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                final GroupData groupData = dataSnapshot.getValue(GroupData.class);
                if (groupData != null && groupData.isJoined() && groupData.getId() != null) {
                    HashMap userMap = (HashMap) groupData.getMembers().get(FirebaseAuth.getInstance().getUid());
                    if (userMap != null && !userMap.isEmpty() && userMap.containsKey("unreadCount")) {
                        groupData.setUnreadCount((Long) userMap.get("unreadCount"));
                    }
                    adapter.updateGroup(groupData);
                    adapter.sortGroupList();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                adapter.removeGroup(dataSnapshot.getKey());

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String prevKey) {
                final GroupData groupData = dataSnapshot.getValue(GroupData.class);
                if (groupData != null && groupData.isJoined() && groupData.getId() != null) {
                    adapter.updateGroupPos(groupData);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        query.addChildEventListener(childEventListener);
        dmQuery.addChildEventListener(dmChildEventListener);
        if (sellerDmQuery != null) {
            sellerDmQuery.addChildEventListener(dmChildEventListener);
        }
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isAdded()) {
                    queryCounter--;
                    checkAllChatsSynced();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        dmQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isAdded()) {
                    queryCounter--;
                    checkAllChatsSynced();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (sellerDmQuery != null) {
            sellerDmQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (isAdded()) {
                        queryCounter--;
                        checkAllChatsSynced();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void checkAllChatsSynced() {
        if (queryCounter == 0) {
            // all groups, DMs, and seller DMs (if applicable) are synced
            adapter.addPublicHeader();
            adapter.sortGroupList();
            groupsRecyclerView.setVisibility(View.VISIBLE);
            if (progressBar.getVisibility() == View.VISIBLE) {
                progressBar.setVisibility(View.GONE);
            }
            toggleSearch(true);
            groupTrace.stop();
            reinitGroupListCopy();
            isPublicGroupsLoading = false;

            groupsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    if (dy > 0) {
                        UiUtils.animateSlideDownHide(getContext(), newGroupContainer);
                    } else {
                        UiUtils.animateSlideUpShow(getContext(), newGroupContainer);
                    }

                    if (adapter.getItemViewType(layoutManager.findLastVisibleItemPosition()) == TYPE_HEADER && !isPublicGroupsLoading) {
                        Log.d(TAG, "onScrolled: last item");
                        isPublicGroupsLoading = true;
                        progressBarPublicGroups.setVisibility(View.VISIBLE);
                        Query publicGroupsQuery = getLubbleGroupsRef().orderByChild("members/" + FirebaseAuth.getInstance().getUid()).endAt(null);
                        publicGroupsQuery.addChildEventListener(childEventListener);
                        publicGroupsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                // custom sort of just public groups
                                progressBarPublicGroups.setVisibility(View.GONE);
                                adapter.sortPublicGroupList();
                                groupsRecyclerView.setPadding(0,0,0,0);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            });
        }
    }

    private void toggleSearch(boolean isEnabled) {
        if (getActivity() != null & getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setIsSearchEnabled(isEnabled);
        }
    }

    private ChildEventListener dmChildEventListener = new ChildEventListener() {

        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String prevChildKey) {
            try {
                final GroupData groupData = dataSnapshot.getValue(GroupData.class);
                if (groupData != null) {
                    final String sellerIdStr = String.valueOf(LubbleSharedPrefs.getInstance().getSellerId());
                    if ((groupData.getMembers().containsKey(FirebaseAuth.getInstance().getUid()) && ((HashMap) groupData.getMembers().get(FirebaseAuth.getInstance().getUid())).get("blocked_status") == null)
                            || (!sellerIdStr.equalsIgnoreCase("-1") && groupData.getMembers().containsKey(sellerIdStr) && ((HashMap) groupData.getMembers().get(sellerIdStr)).get("blocked_status") == null)) {
                        // joined chat
                        groupData.setId(dataSnapshot.getKey());
                        groupData.setIsDm(true);
                        groupData.setIsPrivate(true);
                        for (String memberUid : groupData.getMembers().keySet()) {
                            if (!memberUid.equalsIgnoreCase(FirebaseAuth.getInstance().getUid())
                                    && !memberUid.equalsIgnoreCase(sellerIdStr)) {
                                HashMap memberMap = (HashMap) groupData.getMembers().get(memberUid);
                                if (memberMap != null) {
                                    final String name = String.valueOf(memberMap.get("name"));
                                    final String dp = String.valueOf(memberMap.get("profilePic"));
                                    groupData.setTitle(name);
                                    groupData.setThumbnail(dp);

                                }
                            } else {
                                //this user/seller
                                HashMap memberMap = (HashMap) groupData.getMembers().get(memberUid);
                                if (memberMap.containsKey("unreadCount")) {
                                    final long unreadCount = (long) memberMap.get("unreadCount");
                                    groupData.setUnreadCount(unreadCount);
                                }
                            }
                        }
                        adapter.addGroupToTop(groupData);
                    }
                }
            } catch (NullPointerException npe) {
                npe.printStackTrace();
                Crashlytics.logException(npe);
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            final GroupData groupData = dataSnapshot.getValue(GroupData.class);
            if (groupData != null && groupData.isJoined()
                    && groupData.getMembers().get("blocked_status") == null) {
                final String sellerIdStr = String.valueOf(LubbleSharedPrefs.getInstance().getSellerId());
                groupData.setId(dataSnapshot.getKey());
                groupData.setIsDm(true);
                groupData.setIsPrivate(true);

                for (String memberUid : groupData.getMembers().keySet()) {
                    if (!memberUid.equalsIgnoreCase(FirebaseAuth.getInstance().getUid()) && !memberUid.equalsIgnoreCase(sellerIdStr)) {
                        HashMap memberMap = (HashMap) groupData.getMembers().get(memberUid);
                        if (memberMap != null) {
                            final String name = String.valueOf(memberMap.get("name"));
                            final String dp = String.valueOf(memberMap.get("profilePic"));
                            groupData.setTitle(name);
                            groupData.setThumbnail(dp);
                        }
                    } else {
                        //this user/seller
                        HashMap memberMap = (HashMap) groupData.getMembers().get(memberUid);
                        if (memberMap.containsKey("unreadCount")) {
                            final long unreadCount = (long) memberMap.get("unreadCount");
                            groupData.setUnreadCount(unreadCount);
                        }
                    }
                }
                adapter.updateGroup(groupData);
                adapter.sortGroupList();
            }
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            adapter.removeGroup(dataSnapshot.getKey());

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String prevKey) {
            final GroupData groupData = dataSnapshot.getValue(GroupData.class);
            if (groupData != null && groupData.isJoined() && groupData.getMembers().get("blocked_status") == null) {
                groupData.setId(dataSnapshot.getKey());
                groupData.setIsDm(true);
                groupData.setIsPrivate(true);
                adapter.updateGroupPos(groupData);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    private void syncUserGroup() {
        userGroupsListener = getUserGroupsRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                final UserGroupData userGroupData = dataSnapshot.getValue(UserGroupData.class);
                if (userGroupData != null) {
                    //adapter.updateUserGroupData(dataSnapshot.getKey(), userGroupData);
                    if (!userGroupData.isJoined() && userGroupData.getInvitedBy() != null) {
                        groupInvitedByMap.put(dataSnapshot.getKey(), userGroupData.getInvitedBy().keySet());
                        syncInvitedGroups(dataSnapshot.getKey());
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                final UserGroupData userGroupData = dataSnapshot.getValue(UserGroupData.class);
                if (userGroupData != null) {
                    if (userGroupData.isJoined()) {
                        adapter.updateUserGroupData(dataSnapshot.getKey(), userGroupData);
                    }
                }
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

        userDmsListener = getUserDmsRef(FirebaseAuth.getInstance().getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                final UserGroupData userGroupData = dataSnapshot.getValue(UserGroupData.class);
                if (userGroupData != null) {
                    //adapter.updateUserGroupData(dataSnapshot.getKey(), userGroupData);
                    if (!userGroupData.isJoined() && userGroupData.getInvitedBy() != null) {
                        groupInvitedByMap.put(dataSnapshot.getKey(), userGroupData.getInvitedBy().keySet());
                        syncInvitedGroups(dataSnapshot.getKey());
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                final UserGroupData userGroupData = dataSnapshot.getValue(UserGroupData.class);
                if (userGroupData != null) {
                    adapter.updateUserGroupData(dataSnapshot.getKey(), userGroupData);
                }
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

        sellerDmsListener = getSellerDmsRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                final UserGroupData userGroupData = dataSnapshot.getValue(UserGroupData.class);
                if (userGroupData != null) {
                    //adapter.updateUserGroupData(dataSnapshot.getKey(), userGroupData);
                    if (!userGroupData.isJoined() && userGroupData.getInvitedBy() != null) {
                        groupInvitedByMap.put(dataSnapshot.getKey(), userGroupData.getInvitedBy().keySet());
                        syncInvitedGroups(dataSnapshot.getKey());
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                final UserGroupData userGroupData = dataSnapshot.getValue(UserGroupData.class);
                if (userGroupData != null) {
                    adapter.updateUserGroupData(dataSnapshot.getKey(), userGroupData);
                }
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

        getUserGroupsRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // all done
                /*if (totalUnreadCount == 0 && isAdded() && !LubbleSharedPrefs.getInstance().getIsRewardsOpened()) {
                    ((MainActivity) getActivity()).showRewardsTooltip();
                }*/
                if (totalUnreadCount == 0 && isAdded()) {
                    //******************************************************************************
                    //showEventUnreadCount();
                    //******************************************************************************
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setupSlider() {
        tabLayout.setupWithViewPager(viewPager, true);
        viewPager.setAdapter(new SliderViewPagerAdapter(getChildFragmentManager(), sliderDataList, true));

        new CoverFlow.Builder()
                .with(viewPager)
                .pagerMargin(getResources().getDimensionPixelSize(R.dimen.pager_margin))
                .scale(0.3f)
                .spaceSize(0f)
                .rotationY(0f)
                .build();

        viewPager.clearOnPageChangeListeners();
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                tabLayout.getTabAt(position).select();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (sliderTimer != null) {
            sliderTimer.cancel();
        }
        sliderTimer = new Timer();
        sliderTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(update);
            }
        }, 7000, 5000);
    }

    private Runnable update = new Runnable() {
        public void run() {
            if (currentPage == sliderDataList.size() + 1) {
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

    @Override
    public void onSearched(int resultSize) {
        noSearchResultsTv.setVisibility(resultSize == 0 ? View.VISIBLE : View.GONE);
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
                            adapter.addGroupToTop(groupData);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        map.put(getLubbleGroupsRef().child(groupId), joinedGroupListener);
    }

    private void showEventUnreadCount() {
        getEventsRef().orderByChild("startTimestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (totalUnreadCount == 0 && isAdded()) {
                    int upcomingEventsCount = 0;
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        final EventData eventData = child.getValue(EventData.class);
                        if (eventData != null) {
                            eventData.setId(child.getKey());
                            long timestampToCompare = eventData.getEndTimestamp() == 0L ? eventData.getStartTimestamp() : eventData.getEndTimestamp();
                            if (timestampToCompare > System.currentTimeMillis()) {
                                final Set<String> readEventSet = LubbleSharedPrefs.getInstance().getEventSet();
                                if (!readEventSet.contains(eventData.getId())) {
                                    upcomingEventsCount++;
                                }
                            }
                        }
                    }
                    ((MainActivity) getActivity()).showEventsBadge(upcomingEventsCount);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = this;
    }

    public void reinitGroupListCopy() {
        adapter.reinitGroupListCopy();
    }

    public void toggleVisibilityOfSlider(boolean show) {
        if (show) {
            pagerContainer.setVisibility(View.VISIBLE);
            tabLayout.setVisibility(View.VISIBLE);
        } else {
            pagerContainer.setVisibility(View.GONE);
            tabLayout.setVisibility(View.GONE);
        }
    }

    public void filterGroups(String searchString) {
        adapter.getFilter().filter(searchString);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (getActivity() != null && getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).toggleSearchInToolbar(false);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        /*if (childEventListener != null) {
            query.removeEventListener(childEventListener);
        }
        if (dmChildEventListener != null) {
            dmQuery.removeEventListener(dmChildEventListener);
        }
        if (sellerDmQuery != null && dmChildEventListener != null) {
            sellerDmQuery.removeEventListener(dmChildEventListener);
        }
        if (userGroupsListener != null) {
            getUserGroupsRef().removeEventListener(userGroupsListener);
        }
        if (userDmsListener != null) {
            getUserDmsRef(FirebaseAuth.getInstance().getUid()).removeEventListener(userDmsListener);
        }
        if (sellerDmsListener != null) {
            getSellerDmsRef().removeEventListener(sellerDmsListener);
        }
        for (Query query : map.keySet()) {
            query.removeEventListener(map.get(query));
        }*/
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
        //final Intent intent = new Intent(getContext(), ChatActivity.class);
        //intent.putExtra(EXTRA_DM_ID, dmId);
        //intent.putExtra(EXTRA_RECEIVER_NAME, name);
        //intent.putExtra(EXTRA_RECEIVER_DP_URL, thumbnailUrl);
        //startActivity(intent);
        ChatActivity.openForDm(requireContext(), dmId, null, "");
    }
}
