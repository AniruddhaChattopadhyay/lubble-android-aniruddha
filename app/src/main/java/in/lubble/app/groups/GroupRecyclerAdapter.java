package in.lubble.app.groups;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.emoji.widget.EmojiTextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.models.GroupData;
import in.lubble.app.models.UserGroupData;
import in.lubble.app.utils.DateTimeUtils;
import in.lubble.app.utils.UiUtils;

import static in.lubble.app.utils.StringUtils.isValidString;
import static in.lubble.app.utils.UiUtils.dpToPx;

public class GroupRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_GROUP = 525;
    private static final int TYPE_HEADER = 600;
    private int publicCursorPos = 0;
    private int cursorPos = 0;
    private final List<GroupData> groupDataList;
    // <GroupID, UserGroupData>
    private final HashMap<String, UserGroupData> userGroupDataMap;
    private final OnListFragmentInteractionListener mListener;
    private int posToFlash = -1;

    public GroupRecyclerAdapter(OnListFragmentInteractionListener listener) {
        groupDataList = new ArrayList<>();
        userGroupDataMap = new HashMap<>();
        mListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == publicCursorPos) {
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

            GlideApp.with(groupViewHolder.mView)
                    .load(groupData.getThumbnail())
                    .placeholder(R.drawable.ic_circle_group_24dp)
                    .error(R.drawable.ic_circle_group_24dp)
                    .circleCrop()
                    .into(groupViewHolder.iconIv);

            groupViewHolder.lockIv.setVisibility(groupData.getIsPrivate() ? View.VISIBLE : View.GONE);

            groupViewHolder.titleTv.setText(groupData.getTitle());
            if (!groupData.isJoined() && groupData.getInvitedBy() != null && groupData.getInvitedBy().size() > 0) {
                String inviter = (String) groupData.getInvitedBy().toArray()[0];
                if (inviter.equalsIgnoreCase(LubbleSharedPrefs.getInstance().getSupportUid())) {
                    groupViewHolder.subtitleTv.setText(R.string.ready_to_join);
                } else {
                    groupViewHolder.subtitleTv.setText(R.string.invite_pending);
                }
                groupViewHolder.inviteIcon.setVisibility(View.VISIBLE);
            } else if (isValidString(groupData.getLastMessage())) {
                groupViewHolder.subtitleTv.setText(groupData.getLastMessage());
                groupViewHolder.inviteIcon.setVisibility(View.GONE);
            } else {
                groupViewHolder.subtitleTv.setText("...");
                groupViewHolder.inviteIcon.setVisibility(View.GONE);
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

            final UserGroupData userGroupData = userGroupDataMap.get(groupData.getId());
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

            if (!groupData.isJoined() && (userGroupData == null || userGroupData.getInvitedBy() == null || userGroupData.getInvitedBy().size() == 0)) {
                groupViewHolder.viewGroupTv.setVisibility(View.VISIBLE);
            } else {
                groupViewHolder.viewGroupTv.setVisibility(View.GONE);
            }

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

    private void handleTimestamp(TextView timestampTv, GroupData groupData, UserGroupData userGroupData) {
        if (!groupData.isJoined() && userGroupData != null && userGroupData.getInvitedTimeStamp() > 0) {
            // for pending group invite
            timestampTv.setVisibility(View.GONE);
        } else {
            // joined or unjoined groups
            timestampTv.setVisibility(View.VISIBLE);
            if (groupData.getJoinedTimestamp() > groupData.getLastMessageTimestamp()) {
                timestampTv.setText(DateTimeUtils.getHumanTimestamp(groupData.getJoinedTimestamp()));
            } else {
                timestampTv.setText(DateTimeUtils.getHumanTimestamp(groupData.getLastMessageTimestamp()));
            }
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
    }

    public void updateGroup(GroupData newGroupData) {
        final int pos = getChildIndex(newGroupData.getId());
        if (pos != -1) {
            groupDataList.set(pos, newGroupData);
            notifyItemChanged(pos);
        } else {
            addGroupToTop(newGroupData);
        }
    }

    public void removeGroup(String groupId) {
        final int pos = getChildIndex(groupId);
        if (pos != -1) {
            groupDataList.remove(pos);
            notifyItemRemoved(pos);
        }
    }

    public void updateGroupPos(GroupData groupData) {
        final int oldIndex = getChildIndex(groupData.getId());
        if (oldIndex != -1) {
            groupDataList.remove(oldIndex);
            int newIndex = groupData.getIsPinned() ? 0 : cursorPos;
            groupDataList.add(newIndex, groupData);
            notifyItemMoved(oldIndex, newIndex);
            cursorPos = groupData.getIsPinned() ? 1 : cursorPos;
        }
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
