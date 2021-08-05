package in.lubble.app.groups;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.chat.ChatActivity;
import in.lubble.app.explore.ExploreGroupData;
import in.lubble.app.marketplace.SliderData;
import in.lubble.app.marketplace.SliderViewPagerAdapter;
import in.lubble.app.models.DmData;
import in.lubble.app.models.GroupInfoData;
import in.lubble.app.models.UserGroupData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.utils.UiUtils;
import me.crosswall.lib.coverflow.CoverFlow;
import me.crosswall.lib.coverflow.core.PagerContainer;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static in.lubble.app.chat.ChatActivity.EXTRA_GROUP_ID;
import static in.lubble.app.chat.ChatActivity.EXTRA_IS_JOINING;
import static in.lubble.app.firebase.RealtimeDbHelper.getDmsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getLubbleGroupInfoRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getSellerRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserDmsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserGroupsRef;
import static in.lubble.app.groups.GroupRecyclerAdapter.TYPE_HEADER;

public class GroupListFragment extends Fragment implements OnListFragmentInteractionListener, ChatSearchListener {

    private static final String TAG = "GroupListFragment";
    public static final String USER_INIT_LOGOUT_ACTION = "USER_INIT_LOGOUT_ACTION";

    private OnListFragmentInteractionListener mListener;
    private LinearLayout newGroupContainer;
    private GroupRecyclerAdapter adapter;
    private final HashMap<Query, ValueEventListener> map = new HashMap<>();
    private final HashMap<Query, ValueEventListener> dmListenersMap = new HashMap<>();
    private ChildEventListener userGroupsListener;
    private ChildEventListener userDmsListener;
    private RecyclerView groupsRecyclerView;
    private TextView noSearchResultsTv;
    private ProgressBar progressBar, progressBarPublicGroups;
    private LinearLayoutManager layoutManager;
    private HashMap<String, Set<String>> groupInvitedByMap;
    private HashMap<String, UserGroupData> userGroupDataMap;
    private HashMap<String, Long> dmMap;
    private PagerContainer pagerContainer;
    private ViewPager viewPager;
    private Timer sliderTimer;
    private int currentPage = 0;
    private TabLayout tabLayout;
    private final Handler handler = new Handler();
    private ArrayList<SliderData> sliderDataList = new ArrayList<>();
    private ChildEventListener sellerDmsListener;
    private Trace groupTrace;
    private int queryCounter = 0;
    private boolean isPublicGroupsLoading;
    private boolean isNewUser = true;
    private Map<String, ?> newUserGroupsMap;

    public GroupListFragment() {
    }

    public static GroupListFragment newInstance(boolean isNewUserInThisLubble) {
        GroupListFragment groupListFragment = new GroupListFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isNewUserInThisLubble", isNewUserInThisLubble);
        groupListFragment.setArguments(bundle);
        return groupListFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (requireActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).setChatSearchListener(this);
        }
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

        isNewUser = getArguments().getBoolean("isNewUserInThisLubble", false);

        groupInvitedByMap = new HashMap<>();
        userGroupDataMap = new HashMap<>();
        dmMap = new HashMap<>();
        Analytics.triggerScreenEvent(getContext(), this.getClass());

        layoutManager = new LinearLayoutManager(context);
        groupsRecyclerView.setLayoutManager(layoutManager);
        adapter = new GroupRecyclerAdapter(mListener, getFragmentManager());
        groupsRecyclerView.setAdapter(adapter);
        DividerItemDecoration itemDecor = new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL);
        groupsRecyclerView.addItemDecoration(itemDecor);

        newGroupContainer.setOnClickListener(v -> startActivity(new Intent(getContext(), NewGroupActivity.class)));

        final SliderData sliderData = new SliderData();
        sliderDataList.add(sliderData);

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
        lbm.registerReceiver(userInitiatedLogoutReceiver, new IntentFilter(USER_INIT_LOGOUT_ACTION));

        syncAllGroups();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).toggleSearchInToolbar(true);
        }
        if (adapter != null && adapter.getItemCount() > 0 && !adapter.isFilterNull()) {
            adapter.clearFilter();
            syncAllGroups();
        }
        setupSlider();
        fetchHomeBanners();
    }

    private void syncAllGroups() {
        queryCounter = 0;
        adapter.clearGroups();
        progressBar.setVisibility(View.VISIBLE);
        toggleSearch(false);
        groupsRecyclerView.setVisibility(View.INVISIBLE);
        groupTrace = FirebasePerformance.getInstance().newTrace("group_list_trace");
        groupTrace.start();
        syncUserGroupIds();
        syncUserDmIds();
        syncSellerDmIds();
    }

    private void checkAllChatsSynced() {
        if (queryCounter == 0) {
            // all groups, DMs, and seller DMs (if applicable) are synced
            adapter.addPublicHeader();
            adapter.sortJoinedGroupsList();
            groupsRecyclerView.setVisibility(View.VISIBLE);
            if (progressBar.getVisibility() == View.VISIBLE) {
                progressBar.setVisibility(View.GONE);
            }
            toggleSearch(true);
            groupTrace.stop();
            reInitGroupListCopy();
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

                    int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                    if (lastVisibleItemPosition != NO_POSITION && adapter.getItemViewType(lastVisibleItemPosition) == TYPE_HEADER && !isPublicGroupsLoading) {
                        Log.d(TAG, "onScrolled: last item");
                        isPublicGroupsLoading = true;
                        groupsRecyclerView.setPadding(0, 0, 0, UiUtils.dpToPx(80));
                        progressBarPublicGroups.setVisibility(View.VISIBLE);
                        fetchPublicGroups();
                    }
                }
            });
        }
    }

    private void fetchPublicGroups() {
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.fetchExploreGroups(LubbleSharedPrefs.getInstance().requireLubbleId()).enqueue(new Callback<ArrayList<ExploreGroupData>>() {
            @Override
            public void onResponse(@NotNull Call<ArrayList<ExploreGroupData>> call, @NotNull Response<ArrayList<ExploreGroupData>> response) {
                final ArrayList<ExploreGroupData> exploreGroupDataList = response.body();
                if (response.isSuccessful() && exploreGroupDataList != null && isAdded() && !exploreGroupDataList.isEmpty() && isVisible()) {
                    progressBarPublicGroups.setVisibility(View.GONE);
                    int startingIndex = -1;
                    for (ExploreGroupData exploreGroupData : exploreGroupDataList) {
                        int index = adapter.addPublicGroupToTop(exploreGroupData);
                        if (startingIndex == -1) {
                            startingIndex = index;
                        }
                    }
                    adapter.sortPublicGroupList(startingIndex);
                } else {
                    if (isAdded() && isVisible()) {
                        progressBarPublicGroups.setVisibility(View.GONE);
                        if (exploreGroupDataList != null && exploreGroupDataList.isEmpty()) {
                            Snackbar.make(getView(), "Joined all groups \ud83d\ude0e", Snackbar.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<ExploreGroupData>> call, Throwable t) {
                if (isAdded() && isVisible()) {
                    progressBarPublicGroups.setVisibility(View.GONE);
                    Toast.makeText(getContext(), R.string.check_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void toggleSearch(boolean isEnabled) {
        if (getActivity() != null & getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setIsSearchEnabled(isEnabled);
        }
    }

    private void setupSlider() {
        tabLayout.setupWithViewPager(viewPager, true);
        viewPager.setAdapter(new SliderViewPagerAdapter(getChildFragmentManager(), sliderDataList, false));

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

    private final Runnable update = new Runnable() {
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

    private void syncSellerDmIds() {
        sellerDmsListener = getSellerRef().child(String.valueOf(LubbleSharedPrefs.getInstance().getSellerId())).child("dms")
                .addChildEventListener(new ChildEventListener() {
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
                        adapter.updateDmUnreadCounter(dataSnapshot.getKey(), count);
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

    private void syncUserDmIds() {
        userDmsListener = getUserDmsRef(FirebaseAuth.getInstance().getUid()).addChildEventListener(new ChildEventListener() {
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
                adapter.updateDmUnreadCounter(dataSnapshot.getKey(), count);
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
        queryCounter++;
        final String sellerIdStr = String.valueOf(LubbleSharedPrefs.getInstance().getSellerId());
        final ValueEventListener dmValueListener = getDmsRef().child(dmId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final DmData dmData = dataSnapshot.getValue(DmData.class);
                if (dmData != null) {
                    if ((dmData.getMembers().containsKey(FirebaseAuth.getInstance().getUid()) && ((HashMap) dmData.getMembers().get(FirebaseAuth.getInstance().getUid())).get("blocked_status") == null)
                            || (!sellerIdStr.equalsIgnoreCase("-1") && dmData.getMembers().containsKey(sellerIdStr) && ((HashMap) dmData.getMembers().get(sellerIdStr)).get("blocked_status") == null)) {
                        // joined chat
                        dmData.setId(dataSnapshot.getKey());

                        final GroupInfoData dmGroupData = new GroupInfoData();
                        dmGroupData.setId(dmData.getId());
                        dmGroupData.setLastMessage(dmData.getLastMessage());
                        dmGroupData.setLastMessageTimestamp(dmData.getLastMessageTimestamp());
                        dmGroupData.setIsPrivate(true);
                        dmGroupData.setIsDm(true);

                        final UserGroupData userGroupData = new UserGroupData();
                        final HashMap<String, Object> members = dmData.getMembers();
                        for (String memberUid : members.keySet()) {
                            if (!memberUid.equalsIgnoreCase(FirebaseAuth.getInstance().getUid())
                                    && !memberUid.equalsIgnoreCase(sellerIdStr)) {
                                // Other user
                                HashMap otherMemberMap = (HashMap) members.get(memberUid);
                                if (otherMemberMap != null) {
                                    final String name = String.valueOf(otherMemberMap.get("name"));
                                    final String dp = String.valueOf(otherMemberMap.get("profilePic"));
                                    dmGroupData.setTitle(name);
                                    dmGroupData.setThumbnail(dp);

                                }
                            } else {
                                // this user/seller
                                HashMap thisMemberMap = (HashMap) members.get(memberUid);
                                if (thisMemberMap.containsKey("joinedTimestamp") && (Long) thisMemberMap.get("joinedTimestamp") > 0) {
                                    userGroupData.setJoined(true);
                                }
                            }
                        }
                        userGroupData.setUnreadCount(dmMap.get(dmGroupData.getId()));
                        adapter.addGroupToTop(dmGroupData, userGroupData);
                    }
                }
                queryCounter--;
                checkAllChatsSynced();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                FirebaseCrashlytics.getInstance().recordException(databaseError.toException());
            }
        });
        dmListenersMap.put(getDmsRef().child(dmId), dmValueListener);
    }

    private void syncUserGroupIds() {
        // gets list of group IDs joined by the user
        userGroupsListener = getUserGroupsRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NotNull DataSnapshot dataSnapshot, String s) {
                final UserGroupData userGroupData = dataSnapshot.getValue(UserGroupData.class);
                if (userGroupData != null) {
                    userGroupDataMap.put(dataSnapshot.getKey(), userGroupData);
                    if (userGroupData.isJoined()) {
                        queryCounter++;
                        syncJoinedGroups(dataSnapshot.getKey());
                    } else {
                        if (userGroupData.getInvitedBy() != null) {
                            queryCounter++;
                            groupInvitedByMap.put(dataSnapshot.getKey(), userGroupData.getInvitedBy().keySet());
                            syncInvitedGroups(dataSnapshot.getKey());
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@NotNull DataSnapshot dataSnapshot, String s) {
                final UserGroupData userGroupData = dataSnapshot.getValue(UserGroupData.class);
                if (userGroupData != null) {
                    userGroupDataMap.put(dataSnapshot.getKey(), userGroupData);
                    if (userGroupData.isJoined()) {
                        adapter.updateUserGroupData(dataSnapshot.getKey(), userGroupData);
                    } else {
                        groupInvitedByMap.put(dataSnapshot.getKey(), userGroupData.getInvitedBy().keySet());
                        //syncInvitedGroups(dataSnapshot.getKey());
                        adapter.updateUserGroupData(dataSnapshot.getKey(), userGroupData);
                    }
                }
            }

            @Override
            public void onChildRemoved(@NotNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getKey() != null) {
                    // remove from list
                    adapter.removeGroup(dataSnapshot.getKey());
                }
            }

            @Override
            public void onChildMoved(@NotNull DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void syncJoinedGroups(String groupId) {
        // get meta data of the groups joined by the user
        final ValueEventListener joinedGroupListener =
                getLubbleGroupInfoRef(groupId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        GroupInfoData groupData = dataSnapshot.getValue(GroupInfoData.class);
                        String groupId = dataSnapshot.getRef().getParent().getKey();
                        final UserGroupData userGroupData = userGroupDataMap.get(groupId);
                        if (groupData != null && userGroupData != null) {
                            groupData.setId(groupId);
                            //groupData.setJoined(true);
                            userGroupData.setJoined(true);
                            adapter.addGroupToTop(groupData, userGroupData);
                            groupsRecyclerView.scrollToPosition(0);
                    /* todo check if reqd with new user onboarding
                    // delayed sorting for new users
                        if (isNewUser && newUserGroupsMap == null && GroupPromptSharedPrefs.getInstance().getPreferences().getAll().size() > 0) {
                            newUserGroupsMap = GroupPromptSharedPrefs.getInstance().getPreferences().getAll();
                        }
                        if (newUserGroupsMap != null) {
                            newUserGroupsMap.remove(groupData.getId());
                            if (newUserGroupsMap.size() == 0) {
                                adapter.sortJoinedGroupsList();
                            }
                        }
                     */
                        }
                        queryCounter--;
                        checkAllChatsSynced();
                    }

                    @Override
                    public void onCancelled(@NotNull DatabaseError databaseError) {

                    }
                });
        map.put(getLubbleGroupInfoRef(groupId), joinedGroupListener);
    }

    private void syncInvitedGroups(final String groupId) {
        // get meta data of the groups joined by the user
        final ValueEventListener invitedGroupListener = getLubbleGroupInfoRef(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                        GroupInfoData groupData = dataSnapshot.getValue(GroupInfoData.class);
                        if (groupData != null) {
                            if (groupInvitedByMap.containsKey(groupData.getId())) {
                                groupData.setInvitedBy(groupInvitedByMap.get(groupData.getId()));
                            }
                            final UserGroupData userGroupData = userGroupDataMap.get(groupData.getId());
                            adapter.addGroupToTop(groupData, userGroupData);
                        }
                        queryCounter--;
                        checkAllChatsSynced();
                    }

                    @Override
                    public void onCancelled(@NotNull DatabaseError databaseError) {

                    }
                });
        map.put(getLubbleGroupInfoRef(groupId), invitedGroupListener);
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        mListener = this;
    }

    @Override
    public void reInitGroupListCopy() {
        adapter.reinitGroupListCopy();
    }

    @Override
    public void toggleSliderVisibility(boolean isShown) {
        if (isShown) {
            pagerContainer.setVisibility(View.VISIBLE);
            tabLayout.setVisibility(View.VISIBLE);
        } else {
            pagerContainer.setVisibility(View.GONE);
            tabLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void filterGroups(String searchString) {
        adapter.getFilter().filter(searchString);
    }


    @Override
    public ActionMode onActionModeEnabled(@NonNull ActionMode.Callback callback) {
        if (getActivity() != null && getActivity() instanceof ChatGroupListActivity) {
            return ((ChatGroupListActivity) getActivity()).toggleActionMode(true, callback);
        } else if (getActivity() != null && getActivity() instanceof MainActivity) {
            return ((MainActivity) getActivity()).toggleActionMode(true, callback);
        }
        return null;
    }

    @Override
    public void onPause() {
        super.onPause();

        if (getActivity() != null && getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).toggleSearchInToolbar(false);
            ((MainActivity) getActivity()).toggleActionMode(false, null);
        } else if (getActivity() != null && getActivity() instanceof ChatGroupListActivity) {
            ((ChatGroupListActivity) getActivity()).toggleActionMode(false, null);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        removeListeners();
    }

    private void removeListeners() {
        mListener = null;
        if (userGroupsListener != null) {
            getUserGroupsRef().removeEventListener(userGroupsListener);
            userGroupsListener = null;
        }
        if (userDmsListener != null) {
            getUserDmsRef(FirebaseAuth.getInstance().getUid()).removeEventListener(userDmsListener);
            userDmsListener = null;
        }
        if (sellerDmsListener != null) {
            getSellerRef().child(String.valueOf(LubbleSharedPrefs.getInstance().getSellerId())).child("dms").removeEventListener(sellerDmsListener);
            sellerDmsListener = null;
        }
        for (Query query : map.keySet()) {
            ValueEventListener listener = map.get(query);
            if (listener != null) {
                query.removeEventListener(listener);
            }
        }
        for (Query query : dmListenersMap.keySet()) {
            ValueEventListener listener = dmListenersMap.get(query);
            if (listener != null) {
                query.removeEventListener(listener);
            }
        }
    }

    public BroadcastReceiver userInitiatedLogoutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            removeListeners();
        }
    };

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
