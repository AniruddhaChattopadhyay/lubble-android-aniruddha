package in.lubble.app.groups;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.models.GroupData;
import in.lubble.app.models.UserGroupData;
import in.lubble.app.utils.DateTimeUtils;

import static in.lubble.app.firebase.RealtimeDbHelper.getCreateOrJoinGroupRef;
import static in.lubble.app.utils.StringUtils.isValidString;

public class GroupRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_GROUP = 74;
    private static final int TYPE_SEPARATOR = 58;

    private final List<GroupData> groupDataList;
    // <GroupID, UserGroupData>
    private final HashMap<String, UserGroupData> userGroupDataMap;
    private final OnListFragmentInteractionListener mListener;

    public GroupRecyclerAdapter(OnListFragmentInteractionListener listener) {
        groupDataList = new ArrayList<>();
        userGroupDataMap = new HashMap<>();
        mListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (!isValidString(groupDataList.get(position).getTitle())) {
            return TYPE_SEPARATOR;
        } else {
            return TYPE_GROUP;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_SEPARATOR) {
            return new PublicGroupHeaderViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_public_group_header, parent, false));
        } else {
            return new GroupViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_group_list, parent, false));
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
                    .circleCrop()
                    .placeholder(R.drawable.ic_account_circle_black_no_padding)
                    .error(R.drawable.ic_account_circle_black_no_padding)
                    .into(groupViewHolder.iconIv);

            groupViewHolder.titleTv.setText(groupData.getTitle());
            if (!groupData.isJoined() && groupData.getInvitedBy() != null && groupData.getInvitedBy().size() > 0) {
                groupViewHolder.subtitleTv.setText("Invitation Pending");
            } else if (isValidString(groupData.getLastMessage())) {
                groupViewHolder.subtitleTv.setText(groupData.getLastMessage());
            } else {
                groupViewHolder.subtitleTv.setText(groupData.getDescription());
            }

            groupViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        // Notify the active callbacks interface (the activity, if the
                        // fragment is attached to one) that an item has been selected.
                        mListener.onListFragmentInteraction(groupViewHolder.groupData.getId(), false);
                    }
                }
            });

            final UserGroupData userGroupData = userGroupDataMap.get(groupData.getId());
            if (userGroupData != null && userGroupData.getUnreadCount() > 0) {
                groupViewHolder.unreadCountTv.setVisibility(View.VISIBLE);
                groupViewHolder.unreadCountTv.setText(String.valueOf(userGroupData.getUnreadCount()));
            } else {
                groupViewHolder.unreadCountTv.setVisibility(View.GONE);
            }
            handleTimestamp(groupViewHolder.timestampTv, groupData, userGroupData);

            if (!groupData.isJoined() && (userGroupData == null || userGroupData.getInvitedBy() == null || userGroupData.getInvitedBy().size() == 0)) {
                groupViewHolder.joinBtn.setVisibility(View.VISIBLE);
            } else {
                groupViewHolder.joinBtn.setVisibility(View.GONE);
            }

        } else {
            //todo
        }
    }

    private void handleTimestamp(TextView timestampTv, GroupData groupData, UserGroupData userGroupData) {

        if (groupData.isJoined() && groupData.getLastMessageTimestamp() > 0) {
            timestampTv.setVisibility(View.VISIBLE);
            timestampTv.setText(DateTimeUtils.getHumanTimestamp(groupData.getLastMessageTimestamp()));
        } else if (!groupData.isJoined() && userGroupData != null && userGroupData.getInvitedTimeStamp() > 0) {
            timestampTv.setVisibility(View.VISIBLE);
            timestampTv.setText(DateTimeUtils.getHumanTimestamp(userGroupData.getInvitedTimeStamp()));
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

    private void sortList() {
        Collections.sort(groupDataList, new Comparator<GroupData>() {
            @Override
            public int compare(GroupData lhs, GroupData rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                long lhsTs = 0;
                long rhsTs = 0;
                if (!lhs.isJoined()) {
                    final UserGroupData userGroupData = userGroupDataMap.get(lhs.getId());
                    if (userGroupData != null) {
                        lhsTs = userGroupData.getInvitedTimeStamp();
                    }
                } else {
                    lhsTs = lhs.getLastMessageTimestamp();
                }
                if (!rhs.isJoined()) {
                    final UserGroupData userGroupData = userGroupDataMap.get(rhs.getId());
                    if (userGroupData != null) {
                        rhsTs = userGroupData.getInvitedTimeStamp();
                    }
                } else {
                    rhsTs = rhs.getLastMessageTimestamp();
                }
                return (lhsTs > rhsTs) ? -1 : 1;
            }
        });
    }

    private int getChildIndex(String groupIdToFind) {
        for (int i = 0; i < groupDataList.size(); i++) {
            final GroupData groupData = groupDataList.get(i);
            if (groupData.getId().equalsIgnoreCase(groupIdToFind)) {
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
        final TextView titleTv;
        final TextView subtitleTv;
        final TextView unreadCountTv;
        final TextView timestampTv;
        final Button joinBtn;
        GroupData groupData;

        public GroupViewHolder(View view) {
            super(view);
            mView = view;
            iconIv = view.findViewById(R.id.iv_group_pic);
            titleTv = view.findViewById(R.id.tv_title);
            subtitleTv = view.findViewById(R.id.tv_subtitle);
            unreadCountTv = view.findViewById(R.id.tv_unread_count);
            timestampTv = view.findViewById(R.id.tv_last_msg_time);
            joinBtn = view.findViewById(R.id.btn_join_group);
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
