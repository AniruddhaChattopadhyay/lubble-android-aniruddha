package in.lubble.app.user_search;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.models.ProfileInfo;

import static in.lubble.app.firebase.RealtimeDbHelper.getUserInfoRef;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> implements Filterable {

    private List<ProfileInfo> membersList;
    private final OnUserSelectedListener mListener;
    private HashMap<String, Boolean> checkedMap;
    private HashMap<String, Boolean> groupMembersMap;
    private HashMap<Query, ValueEventListener> listenerMap = new HashMap<>();
    private UserFilter filter;

    UserAdapter(OnUserSelectedListener listener) {
        membersList = new ArrayList<>();
        mListener = listener;
        checkedMap = new HashMap<>();
        groupMembersMap = new HashMap<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_search, parent, false);
        return new ViewHolder(view);
    }

    void addGroupMembersList(HashMap<String, Boolean> groupMembersMap) {
        this.groupMembersMap = groupMembersMap;
        notifyDataSetChanged();
    }

    void addMemberProfile(ProfileInfo profileInfo) {
        membersList.add(profileInfo);
        notifyItemInserted(membersList.size());
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final String userId = membersList.get(position).getId();

        ValueEventListener valueEventListener = getUserInfoRef(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                if (profileInfo != null) {
                    holder.nameTv.setText(profileInfo.getName());
                    GlideApp.with(holder.itemView.getContext())
                            .load(profileInfo.getThumbnail())
                            .placeholder(R.drawable.ic_account_circle_black_no_padding)
                            .circleCrop()
                            .into(holder.iconIv);
                    if (checkedMap.get(userId) != null && checkedMap.get(userId)) {
                        holder.checkIv.setVisibility(View.VISIBLE);
                    } else {
                        holder.checkIv.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        listenerMap.put(getUserInfoRef(userId), valueEventListener);

        if (groupMembersMap.get(userId) != null && groupMembersMap.get(userId)) {
            holder.mView.setOnClickListener(null);
            holder.memberHintTv.setVisibility(View.VISIBLE);
            holder.iconIv.setAlpha(0.5f);
        } else {
            holder.iconIv.setAlpha(1f);
            holder.memberHintTv.setVisibility(View.GONE);
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        final String uid = membersList.get(holder.getAdapterPosition()).getId();
                        toggleView(uid, userId, holder);
                    }
                }
            });
        }
    }

    void deselectUser(String uid) {
        if (checkedMap.get(uid) != null) {
            checkedMap.put(uid, false);
        }
        final ProfileInfo profileInfo = new ProfileInfo();
        profileInfo.setId(uid);
        final int position = membersList.indexOf(profileInfo);
        if (position != -1) {
            notifyItemChanged(position);
        }
    }

    private void toggleView(String uid, String userId, ViewHolder holder) {
        if (checkedMap.get(userId) != null && checkedMap.get(userId)) {
            deselectUser(uid, holder);
        } else {
            selectUser(uid, holder);
        }
    }

    private void deselectUser(String uid, ViewHolder holder) {
        holder.checkIv.setVisibility(View.GONE);
        mListener.onUserDeSelected(uid);
        checkedMap.put(uid, false);
    }

    private void selectUser(String uid, ViewHolder holder) {
        holder.checkIv.setVisibility(View.VISIBLE);
        mListener.onUserSelected(uid);
        checkedMap.put(uid, true);
    }

    void removeAllListeners() {
        for (Query query : listenerMap.keySet()) {
            query.removeEventListener(listenerMap.get(query));
        }
    }

    @Override
    public int getItemCount() {
        return membersList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new UserFilter(this, membersList);
        }
        return filter;
    }

    public void clear() {
        membersList.clear();
    }

    void addAllMembers(ArrayList<ProfileInfo> filteredMembersList) {
        membersList.addAll(filteredMembersList);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        final View mView;
        final ImageView iconIv;
        final ImageView checkIv;
        final TextView nameTv;
        final TextView memberHintTv;

        ViewHolder(View view) {
            super(view);
            mView = view;
            iconIv = view.findViewById(R.id.iv_icon);
            checkIv = view.findViewById(R.id.iv_icon_check);
            nameTv = view.findViewById(R.id.tv_title);
            memberHintTv = view.findViewById(R.id.tv_member_hint);
        }

    }
}
