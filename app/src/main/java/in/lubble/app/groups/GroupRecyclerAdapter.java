package in.lubble.app.groups;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.emoji.widget.EmojiTextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.models.GroupData;
import in.lubble.app.models.UserGroupData;
import in.lubble.app.utils.UiUtils;

import static in.lubble.app.utils.DateTimeUtils.getHumanTimestamp;
import static in.lubble.app.utils.StringUtils.isValidString;
import static in.lubble.app.utils.UiUtils.dpToPx;

public class GroupRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private static final int TYPE_GROUP = 525;
    static final int TYPE_HEADER = 600;
    private int publicCursorPos = 0;
    private int cursorPos = 0;
    private int dmCursorPos = 0;
    private final List<GroupData> groupDataList;
    private final List<GroupData> groupDataListCopy;
    // <GroupID, UserGroupData>
    private final HashMap<String, UserGroupData> userGroupDataMap;
    private final OnListFragmentInteractionListener mListener;
    private int posToFlash = -1;
    private GroupDataFilter filter;

    public GroupRecyclerAdapter(OnListFragmentInteractionListener listener) {
        groupDataList = new ArrayList<>();
        groupDataListCopy = new ArrayList<>();
        userGroupDataMap = new HashMap<>();
        mListener = listener;
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

            groupViewHolder.titleTv.setText(groupData.getTitle());
            final UserGroupData userGroupData = userGroupDataMap.get(groupData.getId());
            if (!groupData.isJoined() && groupData.getInvitedBy() != null && groupData.getInvitedBy().size() > 0) {
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

            if (groupData.getUnreadCount() > 0) {
                groupViewHolder.unreadCountTv.setVisibility(View.VISIBLE);
                groupViewHolder.unreadCountTv.setText(String.valueOf(groupData.getUnreadCount()));
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
            // nothing to process
        }
    }

    private void toggleViewBtn(GroupData groupData, UserGroupData userGroupData, GroupViewHolder groupViewHolder) {
        if (!groupData.isJoined() && (userGroupData == null || userGroupData.getInvitedBy() == null || userGroupData.getInvitedBy().size() == 0)) {
            groupViewHolder.viewGroupTv.setVisibility(View.VISIBLE);
        } else {
            groupViewHolder.viewGroupTv.setVisibility(View.GONE);
        }

    }

    private void handleTimestamp(TextView timestampTv, GroupData groupData, UserGroupData userGroupData) {
        if ((!groupData.isJoined() && userGroupData != null && userGroupData.getInvitedTimeStamp() > 0) || groupData.getLastMessageTimestamp() == 0) {
            // for pending group invite
            timestampTv.setVisibility(View.GONE);
        } else {
            // joined or unjoined groups
            timestampTv.setVisibility(View.VISIBLE);
            timestampTv.setText(getHumanTimestamp(groupData.getLastMessageTimestamp()));
            if (!groupData.isJoined()) {
                // align time with "view" btn
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, dpToPx(8), dpToPx(4));
                timestampTv.setLayoutParams(params);
            }
        }
    }

    public void addGroupToTop(GroupData groupData) {
        if (getChildIndex(groupData.getId()) == -1) {
            int newIndex = groupData.getIsPinned() ? 0 : cursorPos;
            groupDataList.add(newIndex, groupData);
            notifyItemInserted(newIndex);
            cursorPos = groupData.getIsPinned() ? 1 : cursorPos;
            publicCursorPos++;
            dmCursorPos = publicCursorPos - 1;
            Log.d("trace", "addGroupToTop: ");
        } else {
            updateGroup(groupData);
        }
    }

    private static final String TAG = "GroupRecyclerAdapter";

    public void addGroupWithSortFromBottom(GroupData groupData) {
        if (getChildIndex(groupData.getId()) == -1) {

            GroupData oldGroup = groupDataList.get(dmCursorPos);

            while (oldGroup != null && groupData.getLastMessageTimestamp() > oldGroup.getLastMessageTimestamp() && dmCursorPos > 0) {
                dmCursorPos--;
                oldGroup = groupDataList.get(dmCursorPos);
            }

            dmCursorPos = dmCursorPos < 1 ? 1 : dmCursorPos + 1; //ensure no negative index; 1 due to pinned group
            groupDataList.add(dmCursorPos, groupData);
            notifyItemInserted(dmCursorPos);
            publicCursorPos++;
            Log.d("trace", "addGroupWithSortFromBottom: ");
        } else {
            updateGroup(groupData);
        }
    }

    public void addPublicGroupToTop(GroupData groupData) {
        if (getChildIndex(groupData.getId()) == -1) {
            if (publicCursorPos + 1 < groupDataList.size()) {
                groupDataList.add(publicCursorPos + 1, groupData);
                notifyItemInserted(publicCursorPos + 1);
            } else {
                groupDataList.add(groupData);
                notifyItemInserted(groupDataList.size() - 1);
            }
            Log.d("trace", "addPublicGroupToTop: ");
        } else {
            updateGroup(groupData);
        }
    }

    public void updateUserGroupData(String id, UserGroupData userGroupData) {
        final int pos = getChildIndex(id);
        userGroupDataMap.put(id, userGroupData);
        if (pos != -1) {
            notifyItemChanged(pos);
        }
        Log.d("trace", "updateUserGroupData: ");
    }

    public void updateGroup(GroupData newGroupData) {
        final int pos = getChildIndex(newGroupData.getId());
        if (pos != -1) {
            final GroupData oldGroupData = groupDataList.get(pos);
            if (!oldGroupData.isJoined() && newGroupData.isJoined()) {
                // move group from public list to joined list
                removeGroup(newGroupData.getId());
                addGroupWithSortFromBottom(newGroupData);
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

    void sortGroupList() {
        Log.d("trace", "--------------------\nsorting started: ");
        Collections.sort(groupDataList, new Comparator<GroupData>() {
            @Override
            public int compare(GroupData o1, GroupData o2) {
                if (o1 != null && o2 != null) {
                    if (o1.getIsPinned()) return -1;
                    if (o2.getIsPinned()) return 1;
                    return (o1.getLastMessageTimestamp() < o2.getLastMessageTimestamp()) ? 1 : ((o1.getLastMessageTimestamp() == o2.getLastMessageTimestamp()) ? 0 : -1);
                }
                return 0;
            }
        });
        notifyDataSetChanged();
        Log.d("trace", "--------------------\nsorting ended: ");
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
        groupDataList.add(0, null);
        notifyDataSetChanged();
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

    class GroupViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final ImageView iconIv;
        final ImageView lockIv;
        final EmojiTextView titleTv;
        final EmojiTextView subtitleTv;
        final TextView unreadCountTv;
        final TextView timestampTv;
        final TextView viewGroupTv;
        final ImageView inviteIcon;
        final ImageView pinIv;
        GroupData groupData;

        public GroupViewHolder(View view) {
            super(view);
            mView = view;
            iconIv = view.findViewById(R.id.iv_wheretonight_pic);
            lockIv = view.findViewById(R.id.iv_lock_icon);
            titleTv = view.findViewById(R.id.tv_title);
            subtitleTv = view.findViewById(R.id.tv_subtitle);
            unreadCountTv = view.findViewById(R.id.tv_unread_count);
            timestampTv = view.findViewById(R.id.tv_last_msg_time);
            viewGroupTv = view.findViewById(R.id.tv_view_group);
            inviteIcon = view.findViewById(R.id.ic_invite);
            pinIv = view.findViewById(R.id.iv_pin);
        }

    }

    class PublicGroupHeaderViewHolder extends RecyclerView.ViewHolder {

        public PublicGroupHeaderViewHolder(View view) {
            super(view);
        }
    }

}
