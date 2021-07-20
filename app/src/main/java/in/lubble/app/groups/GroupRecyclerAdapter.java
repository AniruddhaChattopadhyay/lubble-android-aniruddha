package in.lubble.app.groups;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ActionMode;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.emoji.widget.EmojiTextView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.lubble.app.GlideApp;
import in.lubble.app.LubbleApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.chat.SnoozeGroupBottomSheet;
import in.lubble.app.explore.ExploreGroupData;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.GroupData;
import in.lubble.app.models.UserGroupData;
import in.lubble.app.notifications.SnoozedGroupsSharedPrefs;
import in.lubble.app.utils.CompleteListener;
import in.lubble.app.utils.NotifUtils;
import in.lubble.app.utils.UiUtils;

import static in.lubble.app.utils.DateTimeUtils.getHumanTimestamp;
import static in.lubble.app.utils.NotifUtils.isGroupSnoozed;
import static in.lubble.app.utils.StringUtils.isValidString;
import static in.lubble.app.utils.UiUtils.dpToPx;

public class GroupRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private static final int TYPE_GROUP = 525;
    static final int TYPE_HEADER = 600;
    private int publicCursorPos = 0;
    private int cursorPos = 0;
    private final List<GroupData> groupDataList;
    private final List<GroupData> groupDataListCopy;
    // <GroupID, UserGroupData>
    private final HashMap<String, UserGroupData> userGroupDataMap;
    private final OnListFragmentInteractionListener mListener;
    private final FragmentManager fragmentManager;
    private int posToFlash = -1;
    private GroupDataFilter filter;
    @Nullable
    private String selectedGroupId = null;
    private int highlightedPos = -1;

    GroupRecyclerAdapter(OnListFragmentInteractionListener listener, FragmentManager fragmentManager) {
        groupDataList = new ArrayList<>();
        groupDataListCopy = new ArrayList<>();
        userGroupDataMap = new HashMap<>();
        mListener = listener;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == publicCursorPos || groupDataList.get(position) == null) {
            return TYPE_HEADER;
        } else {
            return TYPE_GROUP;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_GROUP) {
            return new GroupViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_group_list, parent, false));
        } else {
            return new PublicGroupHeaderViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_group_public_header, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof GroupViewHolder) {
            final GroupViewHolder groupViewHolder = (GroupViewHolder) holder;
            final GroupData groupData = groupDataList.get(position);
            groupViewHolder.groupData = groupData;

            if (groupData == null) {
                return;
            }

            int placeholderThumbnail;
            if (groupData.getIsDm()) {
                placeholderThumbnail = R.drawable.ic_account_circle_black_no_padding;
            } else {
                placeholderThumbnail = R.drawable.ic_circle_group_24dp;
            }

            GlideApp.with(groupViewHolder.mView)
                    .load(groupData.getThumbnail())
                    .placeholder(placeholderThumbnail)
                    .error(placeholderThumbnail)
                    .circleCrop()
                    .into(groupViewHolder.iconIv);

            groupViewHolder.lockIv.setVisibility(groupData.getIsPrivate() ? View.VISIBLE : View.GONE);
            groupViewHolder.notifStatusIv.setVisibility(isGroupSnoozed(groupData.getId()) ? View.VISIBLE : View.GONE);

            groupViewHolder.titleTv.setText(groupData.getTitle());
            final UserGroupData userGroupData = userGroupDataMap.get(groupData.getId());
            if (userGroupData != null && !userGroupData.isJoined() && groupData.getInvitedBy() != null && groupData.getInvitedBy().size() > 0) {
                String inviter = (String) groupData.getInvitedBy().toArray()[0];
                if (inviter.equalsIgnoreCase(LubbleSharedPrefs.getInstance().getSupportUid())) {
                    groupViewHolder.subtitleTv.setText(R.string.ready_to_join);
                } else {
                    groupViewHolder.subtitleTv.setText(R.string.invite_pending);
                }
                groupViewHolder.inviteIcon.setVisibility(View.VISIBLE);
                groupViewHolder.viewGroupTv.setVisibility(View.GONE);
            } else if (isValidString(groupData.getLastMessage())) {
                groupViewHolder.subtitleTv.setText(groupData.getLastMessage());
                groupViewHolder.inviteIcon.setVisibility(View.GONE);
                toggleViewBtn(groupData, userGroupData, groupViewHolder);
            } else {
                groupViewHolder.subtitleTv.setText("...");
                groupViewHolder.inviteIcon.setVisibility(View.GONE);
                toggleViewBtn(groupData, userGroupData, groupViewHolder);
            }

            if (highlightedPos == position) {
                groupViewHolder.itemView.setBackgroundColor(ContextCompat.getColor(groupViewHolder.mView.getContext(), R.color.very_light_gray));
            } else {
                groupViewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
            }

            groupViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        // Notify the active callbacks interface (the activity, if the
                        // fragment is attached to one) that an item has been selected.
                        final boolean isDm = groupData.getIsDm();
                        if (isDm) {
                            mListener.onDmClick(groupViewHolder.groupData.getId(), groupData.getTitle(), groupData.getThumbnail());
                        } else {
                            mListener.onListFragmentInteraction(groupViewHolder.groupData.getId(), false);
                        }
                    }
                }
            });

            if (userGroupData != null && userGroupData.getUnreadCount() > 0) {
                groupViewHolder.unreadCountTv.setVisibility(View.VISIBLE);
                groupViewHolder.unreadCountTv.setText(String.valueOf(userGroupData.getUnreadCount()));
                groupViewHolder.pinIv.setVisibility(View.GONE);
            } else {
                if (!LubbleSharedPrefs.getInstance().getIsDefaultGroupOpened() && groupData.getIsPinned()) {
                    groupViewHolder.unreadCountTv.setVisibility(View.VISIBLE);
                    groupViewHolder.unreadCountTv.setText("1");
                    groupViewHolder.pinIv.setVisibility(View.GONE);
                } else {
                    groupViewHolder.unreadCountTv.setVisibility(View.GONE);
                    if (groupData.getIsPinned()) {
                        groupViewHolder.pinIv.setVisibility(View.VISIBLE);
                    } else {
                        groupViewHolder.pinIv.setVisibility(View.GONE);
                    }
                }
            }
            handleTimestamp(groupViewHolder.timestampTv, groupData, userGroupData);

            if (posToFlash == position) {
                UiUtils.animateColor(groupViewHolder.itemView, ContextCompat.getColor(groupViewHolder.mView.getContext(),
                        R.color.trans_colorAccent), Color.TRANSPARENT);
                posToFlash = -1;
            } else {
                groupViewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
            }
        } else {
            ((PublicGroupHeaderViewHolder) holder).publicHeaderTv.setText(LubbleSharedPrefs.getInstance().getLubbleName() + " Public Groups");
        }
    }

    private void toggleViewBtn(GroupData groupData, UserGroupData userGroupData, GroupViewHolder groupViewHolder) {
        if (userGroupData == null || (!userGroupData.isJoined() && (userGroupData.getInvitedBy() == null || userGroupData.getInvitedBy().size() == 0))) {
            groupViewHolder.viewGroupTv.setVisibility(View.VISIBLE);
        } else {
            groupViewHolder.viewGroupTv.setVisibility(View.GONE);
        }

    }

    private void handleTimestamp(TextView timestampTv, GroupData groupData, UserGroupData userGroupData) {
        if ((userGroupData != null && !userGroupData.isJoined() && userGroupData.getInvitedTimeStamp() > 0) || groupData.getLastMessageTimestamp() == 0) {
            // for pending group invite
            timestampTv.setVisibility(View.GONE);
        } else {
            // joined or unjoined groups
            timestampTv.setVisibility(View.VISIBLE);
            timestampTv.setText(getHumanTimestamp(groupData.getLastMessageTimestamp()));
            if (userGroupData != null && !userGroupData.isJoined()) {
                // align time with "view" btn
                ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, dpToPx(8), dpToPx(4));
                timestampTv.setLayoutParams(params);
            }
        }
    }

    @Deprecated
    public void addGroupToTop(GroupData groupData) {
        if (getChildIndex(groupData.getId()) == -1) {
            int newIndex = groupData.getIsPinned() ? 0 : cursorPos;
            if (newIndex > groupDataList.size()) {
                // happens while groupDataList is cleared (user searched something) & a new group invite is added
                // then add new group to groupDataListCopy so that when user closes search, the new groups are added properly
                groupDataListCopy.add(newIndex, groupData);
                publicCursorPos++;
            } else {
                groupDataList.add(newIndex, groupData);
                //notifyItemInserted(newIndex);
            }
            cursorPos = groupData.getIsPinned() ? 1 : cursorPos;
            publicCursorPos++;
            Log.d("trace", "addGroupToTop: ");
        } else {
            updateGroup(groupData);
        }
    }

    public void addGroupToTop(GroupData groupData, UserGroupData userGroupData) {
        if (getChildIndex(groupData.getId()) == -1) {
            userGroupDataMap.put(groupData.getId(), userGroupData);
            int newIndex = groupData.getIsPinned() ? 0 : cursorPos;
            if (newIndex > groupDataList.size()) {
                // happens while groupDataList is cleared (user searched something) & a new group invite is added
                // then add new group to groupDataListCopy so that when user closes search, the new groups are added properly
                groupDataListCopy.add(newIndex, groupData);
                publicCursorPos++;
            } else {
                groupDataList.add(newIndex, groupData);
                notifyItemInserted(newIndex);
            }
            cursorPos = groupData.getIsPinned() ? 1 : cursorPos;
            publicCursorPos++;
            Log.d("trace", "addGroupToTop: ");
        } else {
            updateGroup(groupData, userGroupData);
        }
    }

    public int addPublicGroupToTop(GroupData groupData) {
        if (getChildIndex(groupData.getId()) == -1) {
            if (publicCursorPos + 1 < groupDataList.size()) {
                groupDataList.add(publicCursorPos + 1, groupData);
                notifyItemInserted(publicCursorPos + 1);
                Log.d("trace", "addPublicGroupToTop: ");
                return publicCursorPos + 1;
            } else {
                groupDataList.add(groupData);
                notifyItemInserted(groupDataList.size() - 1);
                Log.d("trace", "addPublicGroupToTop: ");
                return groupDataList.size() - 1;
            }
        } else {
            updateGroup(groupData);
            return -1;
        }
    }

    public void updateGroup(GroupData newGroupData, @Nullable UserGroupData userGroupData) {
        final int pos = getChildIndex(newGroupData.getId());
        if (pos != -1) {
            final GroupData oldGroupData = groupDataList.get(pos);
            if (!oldGroupData.isJoined() && newGroupData.isJoined()
                    || (userGroupDataMap.get(newGroupData.getId()) == null
                    && userGroupData != null && userGroupData.isJoined())) {
                // move group from public list to joined list
                removeGroup(newGroupData.getId());
                addGroupToTop(newGroupData, userGroupData);
            } else {
                groupDataList.set(pos, newGroupData);
                if (userGroupData != null) {
                    userGroupDataMap.put(newGroupData.getId(), userGroupData);
                }
                notifyItemChanged(pos);
            }
            sortJoinedGroupsList();
        }
        Log.d("trace", "updateGroup1: ");
    }

    public void updateUserGroupData(String id, UserGroupData userGroupData) {
        final int pos = getChildIndex(id);
        userGroupDataMap.put(id, userGroupData);
        if (pos != -1) {
            notifyItemChanged(pos);
        }
        Log.d("trace", "updateUserGroupData: ");
    }

    public void updateDmUnreadCounter(String id, Long unreadCount) {
        final int pos = getChildIndex(id);
        UserGroupData userGroupData = userGroupDataMap.get(id);
        if (userGroupData != null) {
            userGroupData.setUnreadCount(unreadCount);
            userGroupDataMap.put(id, userGroupData);
            if (pos != -1) {
                notifyItemChanged(pos);
            }
            Log.d("trace", "updateUserGroupData: ");
        }
    }

    @Deprecated
    public void updateGroup(GroupData newGroupData) {
        final int pos = getChildIndex(newGroupData.getId());
        if (pos != -1) {
            final GroupData oldGroupData = groupDataList.get(pos);
            if (!oldGroupData.isJoined() && newGroupData.isJoined()) {
                // move group from public list to joined list
                removeGroup(newGroupData.getId());
                addGroupToTop(newGroupData);
                sortJoinedGroupsList();
                //addGroupWithSortFromBottom(newGroupData);
            } else {
                groupDataList.set(pos, newGroupData);
                notifyItemChanged(pos);
            }
        }
        Log.d("trace", "updateGroup: ");
    }

    public void removeGroup(String groupId) {
        final int pos = getChildIndex(groupId);
        if (pos != -1) {
            groupDataList.remove(pos);
            if (pos < publicCursorPos) {
                publicCursorPos--;
            }
            notifyItemRemoved(pos);
        }
    }

    @Deprecated
    /**
     * use sortGroupList(), it takes 2-5ms on a list of 100+ groups/dms
     */
    public void updateGroupPos(GroupData groupData) {
        final int oldIndex = getChildIndex(groupData.getId());
        if (oldIndex != -1) {
            groupDataList.remove(oldIndex);
            int newIndex = groupData.getIsPinned() ? 0 : cursorPos;
            groupDataList.add(newIndex, groupData);
            notifyItemMoved(oldIndex, newIndex);
            cursorPos = groupData.getIsPinned() ? 1 : cursorPos;
        }
        Log.d("trace", "updateGroupPos: ");
    }

    void sortJoinedGroupsList() {
        Log.d("trace", "--------------------\nsorting started: ");
        Collections.sort(groupDataList, new Comparator<GroupData>() {
            @Override
            public int compare(GroupData o1, GroupData o2) {
                if (o1 != null && o2 != null) {
                    UserGroupData userGroup1Data = userGroupDataMap.get(o1.getId());
                    UserGroupData userGroup2Data = userGroupDataMap.get(o2.getId());
                    if (userGroup1Data != null && userGroup2Data != null && userGroup1Data.isJoined() && userGroup2Data.isJoined()) {
                        if (o1.getIsPinned()) return -1;
                        if (o2.getIsPinned()) return 1;
                        return (o1.getLastMessageTimestamp() < o2.getLastMessageTimestamp()) ? 1 : ((o1.getLastMessageTimestamp() == o2.getLastMessageTimestamp()) ? 0 : -1);
                    }
                }
                return 0;
            }
        });
        notifyDataSetChanged();
        Log.d("trace", "--------------------\nsorting ended: ");
    }

    void sortPublicGroupList(int startingIndex) {
        Log.d("trace", "--------------------\npublic group sorting started: ");
        startingIndex = startingIndex == -1 ? publicCursorPos : startingIndex;
        List<GroupData> list = groupDataList.subList(startingIndex, groupDataList.size());
        Collections.sort(list, new Comparator<GroupData>() {
            @Override
            public int compare(GroupData o1, GroupData o2) {
                if (o1 instanceof ExploreGroupData && o2 instanceof ExploreGroupData) {
                    return Integer.compare(((ExploreGroupData) o2).getPriority(), ((ExploreGroupData) o1).getPriority());
                }
                return 0;
            }
        });
        //notifyDataSetChanged();
        notifyItemRangeChanged(startingIndex, list.size());
        groupDataListCopy.addAll(list);
        if (filter != null) {
            filter.addGroups(list);
        }
        Log.d("trace", "--------------------\npublic group sorting ended: ");
    }

    public void flashPos(int pos) {
        posToFlash = pos;
        notifyItemChanged(pos);
    }

    private int getChildIndex(String groupIdToFind) {
        for (int i = 0; i < groupDataList.size(); i++) {
            final GroupData groupData = groupDataList.get(i);
            if (groupData != null && groupIdToFind.equalsIgnoreCase(groupData.getId())) {
                return i;
            }
        }
        return -1;
    }

    public void clearGroups() {
        groupDataList.clear();
        cursorPos = 0;
        publicCursorPos = 0;

        notifyDataSetChanged();
    }

    void addPublicHeader() {
        groupDataList.add(publicCursorPos, null);
        notifyItemInserted(publicCursorPos);
    }

    @Override
    public int getItemCount() {
        return groupDataList.size();
    }

    public void reinitGroupListCopy() {
        groupDataListCopy.clear();
        groupDataListCopy.addAll(groupDataList);
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new GroupDataFilter(this, groupDataList);
        }
        return filter;
    }

    public void replaceAll(List<GroupData> filteredList) {
        groupDataList.clear();
        groupDataList.addAll(groupDataListCopy);

        if (filteredList.size() == 0) {
            // no results; remove the public header
            groupDataList.clear();
        } else {
            for (int i = groupDataList.size() - 1; i >= 0; i--) {
                final GroupData groupData = groupDataList.get(i);
                if (groupData != null && !filteredList.contains(groupData)) {
                    groupDataList.remove(groupData);
                }
            }
        }

        mListener.onSearched(filteredList.size());
        notifyDataSetChanged();
    }

    boolean isFilterNull() {
        return filter == null;
    }

    void clearFilter() {
        filter = null;
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final ImageView iconIv;
        final ImageView lockIv, notifStatusIv;
        final EmojiTextView titleTv;
        final EmojiTextView subtitleTv;
        final TextView unreadCountTv;
        final TextView timestampTv;
        final TextView viewGroupTv;
        final ImageView inviteIcon;
        final ImageView pinIv;
        GroupData groupData;
        @Nullable
        private ActionMode actionMode;

        public GroupViewHolder(View view) {
            super(view);
            mView = view;
            iconIv = view.findViewById(R.id.iv_wheretonight_pic);
            lockIv = view.findViewById(R.id.iv_lock_icon);
            titleTv = view.findViewById(R.id.tv_title);
            subtitleTv = view.findViewById(R.id.tv_subtitle);
            unreadCountTv = view.findViewById(R.id.tv_unread_count);
            notifStatusIv = view.findViewById(R.id.iv_notif_status);
            timestampTv = view.findViewById(R.id.tv_last_msg_time);
            viewGroupTv = view.findViewById(R.id.tv_view_group);
            inviteIcon = view.findViewById(R.id.ic_invite);
            pinIv = view.findViewById(R.id.iv_pin);

            mView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (getAdapterPosition() != highlightedPos) {
                        GroupData groupData = groupDataList.get(getAdapterPosition());
                        if (userGroupDataMap.containsKey(groupData.getId())
                                && userGroupDataMap.get(groupData.getId()).isJoined()) {
                            selectedGroupId = groupData.getId();
                            actionMode = mListener.onActionModeEnabled(actionModeCallbacks);
                            itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.very_light_gray));
                            if (highlightedPos != -1) {
                                // another item was highlighted, remove its highlight
                                notifyItemChanged(highlightedPos);
                            }
                            highlightedPos = getAdapterPosition();
                        }
                    } else {
                        if (actionMode != null) {
                            actionMode.finish();
                            actionMode = null;
                        }
                    }
                    return true;
                }
            });
        }

        private ActionMode.Callback actionModeCallbacks = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_group, menu);
                MenuItem snoozeItem = menu.findItem(R.id.action_mute);
                MenuItem exitGroupItem = menu.findItem(R.id.action_exit);
                if (NotifUtils.isGroupSnoozed(selectedGroupId)) {
                    snoozeItem.setIcon(R.drawable.ic_volume_up_black_24dp);
                } else {
                    snoozeItem.setIcon(R.drawable.ic_mute);
                }
                snoozeItem.setVisible(true);
                exitGroupItem.setVisible(!groupDataList.get(getAdapterPosition()).getIsDm());
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_mute:
                        if (null != selectedGroupId) {
                            if (NotifUtils.isGroupSnoozed(selectedGroupId)) {
                                SnoozedGroupsSharedPrefs.getInstance().getPreferences().edit().remove(selectedGroupId).apply();
                                actionMode.finish();
                                actionMode = null;
                                Toast.makeText(LubbleApp.getAppContext(), "Un-Snoozed Chat", Toast.LENGTH_SHORT).show();
                            } else {
                                SnoozeGroupBottomSheet.newInstance(selectedGroupId, "group_list", new CompleteListener() {
                                    @Override
                                    public void onComplete(boolean isSuccess) {
                                        notifyItemChanged(highlightedPos);
                                        mode.finish();
                                    }
                                }).show(fragmentManager, null);
                            }
                        }
                        break;
                    case R.id.action_exit:
                        showConfirmationDialog(itemView.getContext(), selectedGroupId);
                        actionMode.finish();
                        actionMode = null;
                        break;
                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                //selectedGroupId = null;
                if (highlightedPos != -1) {
                    notifyItemChanged(highlightedPos);
                    highlightedPos = -1;
                }
            }
        };

    }

    private void showConfirmationDialog(final Context context, final String selectedGroupId) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(context.getString(R.string.leave_group_ques));
        alertDialog.setMessage(context.getString(R.string.leave_group_desc));
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.leave_group_title), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                leaveGroup(context, selectedGroupId);
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getString(R.string.all_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void leaveGroup(final Context context, final String groupId) {
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(RealtimeDbHelper.getUserGroupPath() + "/" + groupId, null);
        childUpdates.put(
                RealtimeDbHelper.getLubbleGroupPath() + "/" + groupId + "/members/" + FirebaseAuth.getInstance().getUid(),
                null
        );

        FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (!fragmentManager.isStateSaved() && !fragmentManager.isDestroyed()) {
                    removeGroup(selectedGroupId);
                }
            }
        });
    }

    class PublicGroupHeaderViewHolder extends RecyclerView.ViewHolder {

        final TextView publicHeaderTv;

        public PublicGroupHeaderViewHolder(View view) {
            super(view);
            publicHeaderTv = view.findViewById(R.id.tv_public_groups_hdr);
        }
    }

}
