package in.lubble.app.user_search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.models.ProfileInfo;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> implements Filterable {

    private List<ProfileInfo> membersList;
    private final OnUserSelectedListener mListener;
    private HashMap<String, Boolean> checkedMap;
    private HashMap<String, Boolean> groupMembersMap;
    private final GlideRequests glide;
    private UserFilter filter;

    UserAdapter(OnUserSelectedListener listener, GlideRequests glide) {
        membersList = new ArrayList<>();
        mListener = listener;
        checkedMap = new HashMap<>();
        groupMembersMap = new HashMap<>();
        this.glide = glide;
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
        sort();
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final ProfileInfo profileInfo = membersList.get(position);
        final String userId = profileInfo.getId();

        holder.nameTv.setText(profileInfo.getName());
        glide.load(profileInfo.getThumbnail())
                .placeholder(R.drawable.ic_account_circle_black_no_padding)
                .circleCrop()
                .into(holder.iconIv);
        if (checkedMap.get(userId) != null && checkedMap.get(userId)) {
            holder.checkIv.setVisibility(View.VISIBLE);
        } else {
            holder.checkIv.setVisibility(View.GONE);
        }

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

    private void sort() {
        Collections.sort(membersList, new Comparator<ProfileInfo>() {
            @Override
            public int compare(ProfileInfo o1, ProfileInfo o2) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
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
        notifyDataSetChanged();
    }

    void addAllMembers(ArrayList<ProfileInfo> filteredMembersList) {
        membersList.addAll(filteredMembersList);
        sort();
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
