package in.lubble.app.groups;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.GroupData;

import static in.lubble.app.firebase.RealtimeDbHelper.getCreateOrJoinGroupRef;
import static in.lubble.app.utils.StringUtils.isValidString;

public class GroupRecyclerAdapter extends RecyclerView.Adapter<GroupRecyclerAdapter.GroupViewHolder> {

    private final List<GroupData> groupDataList;
    private final OnListFragmentInteractionListener mListener;

    public GroupRecyclerAdapter(OnListFragmentInteractionListener listener) {
        groupDataList = new ArrayList<>();
        mListener = listener;
    }

    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_group_list, parent, false);
        return new GroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final GroupViewHolder holder, int position) {
        final GroupData groupData = groupDataList.get(position);
        holder.groupData = groupData;

        GlideApp.with(holder.mView)
                .load(groupData.getThumbnail())
                .circleCrop()
                .placeholder(R.drawable.ic_account_circle_black_no_padding)
                .error(R.drawable.ic_account_circle_black_no_padding)
                .into(holder.iconIv);

        holder.titleTv.setText(groupData.getTitle());
        if (isValidString(groupData.getLastMessage())) {
            holder.subtitleTv.setText(groupData.getLastMessage());
        } else {
            holder.subtitleTv.setText(groupData.getDescription());
        }

        if (groupData.getInvitedBy() != null && groupData.getInvitedBy().size() != 0) {
            final Set<String> invitedBy = groupData.getInvitedBy();
            String inviters = "";
            for (String s : invitedBy) {
                inviters += s + " ";
            }
            holder.subtitleTv.setText("Invited by " + inviters);
            holder.joinBtn.setText("Accept");
            holder.rejectTv.setVisibility(View.VISIBLE);
        } else {
            holder.joinBtn.setText("Join");
            holder.rejectTv.setVisibility(View.GONE);
        }
        holder.joinBtn.setVisibility(groupData.isJoined() ? View.GONE : View.VISIBLE);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.groupData);
                }
            }
        });

        holder.joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.joinBtn.setEnabled(false);
                getCreateOrJoinGroupRef().child(groupData.getId()).setValue(true, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        groupDataList.remove(holder.getAdapterPosition());
                        notifyItemRemoved(holder.getAdapterPosition());
                    }
                });
            }
        });

        RealtimeDbHelper.getUserGroupsRef().child(groupData.getId()).child("unreadCount").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    final Integer unreadCount = dataSnapshot.getValue(Integer.class);
                    if (unreadCount > 0) {
                        holder.unreadCountTv.setVisibility(View.VISIBLE);
                        holder.unreadCountTv.setText(String.valueOf(unreadCount));
                    } else {
                        holder.unreadCountTv.setVisibility(View.GONE);
                    }
                } else {
                    holder.unreadCountTv.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void addGroup(GroupData groupData) {
        if (getChildIndex(groupData) == -1) {
            groupDataList.add(groupData);
            sortList();
            notifyDataSetChanged();
        } else {
            updateGroup(groupData);
        }
    }

    public void updateGroup(GroupData newGroupData) {
        final int pos = getChildIndex(newGroupData);
        if (pos != -1) {
            groupDataList.set(pos, newGroupData);
            sortList();
            notifyDataSetChanged();
        }
    }

    private void sortList() {
        Collections.sort(groupDataList, new Comparator<GroupData>() {
            @Override
            public int compare(GroupData lhs, GroupData rhs) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return lhs.isJoined() && !rhs.isJoined() ? -1 : (!lhs.isJoined() && rhs.isJoined()) ? 1 :
                        (lhs.getLastMessageTimestamp() > rhs.getLastMessageTimestamp()) ? -1 : 1;
            }
        });
    }

    private int getChildIndex(GroupData groupDataToCompare) {
        for (int i = 0; i < groupDataList.size(); i++) {
            final GroupData groupData = groupDataList.get(i);
            if (groupData.getId().equalsIgnoreCase(groupDataToCompare.getId())) {
                return i;
            }
        }
        return -1;
    }

    public void clearGroups() {
        groupDataList.clear();
    }

    @Override
    public int getItemCount() {
        return groupDataList.size();
    }

    class GroupViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final ImageView iconIv;
        final TextView titleTv;
        final TextView subtitleTv;
        final Button joinBtn;
        final TextView unreadCountTv;
        final TextView timestampTv;
        final TextView rejectTv;
        GroupData groupData;

        public GroupViewHolder(View view) {
            super(view);
            mView = view;
            iconIv = view.findViewById(R.id.iv_group_pic);
            titleTv = view.findViewById(R.id.tv_title);
            subtitleTv = view.findViewById(R.id.tv_subtitle);
            rejectTv = view.findViewById(R.id.tv_reject_group);
            joinBtn = view.findViewById(R.id.btn_join_group);
            unreadCountTv = view.findViewById(R.id.tv_unread_count);
            timestampTv = view.findViewById(R.id.tv_last_msg_time);
        }
    }
}
