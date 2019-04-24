package in.lubble.app.groups;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.emoji.widget.EmojiTextView;
import androidx.recyclerview.widget.RecyclerView;
import in.lubble.app.Constants;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.models.GroupData;
import in.lubble.app.models.UserGroupData;
import in.lubble.app.utils.DateTimeUtils;
import in.lubble.app.utils.UiUtils;

import java.util.*;

import static in.lubble.app.firebase.RealtimeDbHelper.getCreateOrJoinGroupRef;
import static in.lubble.app.utils.StringUtils.isValidString;

public class GroupRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

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
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GroupViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_group_list, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
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
                    final boolean isDm = groupData.getMembers().size() == 0;
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
            if (!LubbleSharedPrefs.getInstance().getIsDefaultGroupOpened() && groupData.getId().equalsIgnoreCase(Constants.DEFAULT_GROUP)) {
                groupViewHolder.unreadCountTv.setVisibility(View.VISIBLE);
                groupViewHolder.unreadCountTv.setText("1");
                groupViewHolder.pinIv.setVisibility(View.GONE);
            } else {
                groupViewHolder.unreadCountTv.setVisibility(View.GONE);
                if (groupData.getId().equalsIgnoreCase(Constants.DEFAULT_GROUP)) {
                    groupViewHolder.pinIv.setVisibility(View.VISIBLE);
                } else {
                    groupViewHolder.pinIv.setVisibility(View.GONE);
                }
            }
        }
        handleTimestamp(groupViewHolder.timestampTv, groupData, userGroupData);

        if (!groupData.isJoined() && (userGroupData == null || userGroupData.getInvitedBy() == null || userGroupData.getInvitedBy().size() == 0)) {
            groupViewHolder.joinBtn.setVisibility(View.VISIBLE);
        } else {
            groupViewHolder.joinBtn.setVisibility(View.GONE);
        }

        if (posToFlash == position) {
            UiUtils.animateColor(groupViewHolder.itemView, ContextCompat.getColor(groupViewHolder.mView.getContext(),
                    R.color.trans_colorAccent), Color.TRANSPARENT);
            posToFlash = -1;
        } else {
            groupViewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void handleTimestamp(TextView timestampTv, GroupData groupData, UserGroupData userGroupData) {
        if (groupData.isJoined()) {
            timestampTv.setVisibility(View.VISIBLE);
            if (groupData.getJoinedTimestamp() > groupData.getLastMessageTimestamp()) {
                timestampTv.setText(DateTimeUtils.getHumanTimestamp(groupData.getJoinedTimestamp()));
            } else {
                timestampTv.setText(DateTimeUtils.getHumanTimestamp(groupData.getLastMessageTimestamp()));
            }
        } else if (!groupData.isJoined() && userGroupData != null && userGroupData.getInvitedTimeStamp() > 0) {
            timestampTv.setVisibility(View.GONE);
        } else {
            timestampTv.setVisibility(View.GONE);
        }
    }

    public void addGroup(GroupData groupData, UserGroupData userGroupData) {
        if (getChildIndex(groupData.getId()) == -1) {
            groupDataList.add(groupData);
            userGroupDataMap.put(groupData.getId(), userGroupData);
            sortList();
            notifyDataSetChanged();
        } else {
            updateGroup(groupData, userGroupData);
        }
    }

    public void addPublicGroup(GroupData groupData) {
        if (getChildIndex(groupData.getId()) == -1) {
            groupDataList.add(groupData);
            notifyDataSetChanged();
        } else {
            updateGroup(groupData, null);
        }
    }

    public void updateGroup(GroupData newGroupData, @Nullable UserGroupData userGroupData) {
        final int pos = getChildIndex(newGroupData.getId());
        if (pos != -1) {
            groupDataList.set(pos, newGroupData);
            if (userGroupData != null) {
                userGroupDataMap.put(newGroupData.getId(), userGroupData);
            }
            sortList();
            notifyDataSetChanged();
        }
    }

    public void removeGroup(String groupId) {
        final int pos = getChildIndex(groupId);
        if (pos != -1) {
            groupDataList.remove(pos);
            sortList();
            notifyDataSetChanged();
        }
    }

    public void flashPos(int pos) {
        posToFlash = pos;
        notifyItemChanged(pos);
    }

    private void sortList() {
        Collections.sort(groupDataList, new Comparator<GroupData>() {
            @Override
            public int compare(GroupData lhs, GroupData rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                if (lhs.getId() == null || rhs.getId() == null) {
                    return 0;
                }
                if (lhs.getId().equalsIgnoreCase(Constants.DEFAULT_GROUP)) {
                    return -1;
                }
                if (rhs.getId().equalsIgnoreCase(Constants.DEFAULT_GROUP)) {
                    return 1;
                }
                long lhsTs = 0;
                long rhsTs = 0;
                if (!lhs.isJoined()) {
                    final UserGroupData userGroupData = userGroupDataMap.get(lhs.getId());
                    if (userGroupData != null) {
                        lhsTs = userGroupData.getInvitedTimeStamp();
                    }
                } else {
                    if (lhs.getJoinedTimestamp() > lhs.getLastMessageTimestamp()) {
                        lhsTs = lhs.getJoinedTimestamp();
                    } else {
                        lhsTs = lhs.getLastMessageTimestamp();
                    }
                }
                if (!rhs.isJoined()) {
                    final UserGroupData userGroupData = userGroupDataMap.get(rhs.getId());
                    if (userGroupData != null) {
                        rhsTs = userGroupData.getInvitedTimeStamp();
                    }
                } else {
                    if (rhs.getJoinedTimestamp() > rhs.getLastMessageTimestamp()) {
                        rhsTs = rhs.getJoinedTimestamp();
                    } else {
                        rhsTs = rhs.getLastMessageTimestamp();
                    }
                }
                return (lhsTs > rhsTs) ? -1 : 1;
            }
        });
    }

    private int getChildIndex(String groupIdToFind) {
        for (int i = 0; i < groupDataList.size(); i++) {
            final GroupData groupData = groupDataList.get(i);
            if (groupIdToFind.equalsIgnoreCase(groupData.getId())) {
                return i;
            }
        }
        return -1;
    }

    public void clearGroups() {
        groupDataList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return groupDataList.size();
    }

    class GroupViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final View mView;
        final ImageView iconIv;
        final ImageView lockIv;
        final EmojiTextView titleTv;
        final EmojiTextView subtitleTv;
        final TextView unreadCountTv;
        final TextView timestampTv;
        final Button joinBtn;
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
            joinBtn = view.findViewById(R.id.btn_join_group);
            inviteIcon = view.findViewById(R.id.ic_invite);
            pinIv = view.findViewById(R.id.iv_pin);
            joinBtn.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_join_group:
                    getCreateOrJoinGroupRef().child(groupData.getId()).setValue(true);
                    mListener.onListFragmentInteraction(groupData.getId(), true);
                    break;
            }
        }
    }

    class PublicGroupHeaderViewHolder extends RecyclerView.ViewHolder {

        public PublicGroupHeaderViewHolder(View view) {
            super(view);
        }
    }

}
