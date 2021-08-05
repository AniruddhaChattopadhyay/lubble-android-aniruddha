package in.lubble.app.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.emoji.widget.EmojiTextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.models.GroupData;

import static in.lubble.app.utils.StringUtils.isValidString;

public class BlockedChatsAdapter extends RecyclerView.Adapter<BlockedChatsAdapter.ViewHolder> {

    private final List<GroupData> groupDataList;
    private final OnBlockedChatClickListener mListener;
    // <GroupID, UserGroupData>

    public BlockedChatsAdapter(OnBlockedChatClickListener listener) {
        groupDataList = new ArrayList<>();
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_group_list, parent, false);
        return new ViewHolder(view);
    }

    void addToTop(GroupData groupData) {
        groupDataList.add(0, groupData);
        notifyItemInserted(0);
    }

    void updateGroup(GroupData newGroupData) {
        final int pos = getChildIndex(newGroupData.getId());
        if (pos != -1) {
            groupDataList.set(pos, newGroupData);
            notifyItemChanged(pos);
        } else {
            addToTop(newGroupData);
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final ViewHolder groupViewHolder = holder;
        final GroupData groupData = groupDataList.get(position);
        groupViewHolder.groupData = groupData;

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
        if (groupData.getInvitedBy() != null && groupData.getInvitedBy().size() > 0) {
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
                    mListener.onBlockedChatClicked(groupViewHolder.groupData.getId(), groupData.getTitle(), groupData.getThumbnail());
                }
            }
        });
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
            int newIndex = 0;
            groupDataList.add(newIndex, groupData);
            notifyItemMoved(oldIndex, newIndex);
        }
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

    @Override
    public int getItemCount() {
        return groupDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        final ImageView iconIv;
        final ImageView lockIv;
        final EmojiTextView titleTv;
        final EmojiTextView subtitleTv;
        final TextView unreadCountTv;
        final TextView timestampTv;
        final TextView viewGroupTv;
        final ImageView inviteIcon;
        GroupData groupData;

        public ViewHolder(View view) {
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

            unreadCountTv.setVisibility(View.GONE);
            timestampTv.setVisibility(View.GONE);
            viewGroupTv.setVisibility(View.GONE);
        }

    }
}
