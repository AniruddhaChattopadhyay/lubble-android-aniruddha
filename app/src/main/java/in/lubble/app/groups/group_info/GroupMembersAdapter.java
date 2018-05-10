package in.lubble.app.groups.group_info;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.models.ProfileInfo;
import in.lubble.app.profile.ProfileActivity;

/**
 * Created by ishaan on 11/2/18.
 */

public class GroupMembersAdapter extends RecyclerView.Adapter<GroupMembersAdapter.MemberHolder> {

    private final List<ProfileInfo> memberList;
    private final HashMap<String, String> adminList;
    private final GlideRequests glide;

    public GroupMembersAdapter(GlideRequests glide) {
        memberList = new ArrayList<>();
        adminList = new HashMap<>();
        this.glide = glide;
    }

    @Override
    public MemberHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_icon_text, parent, false);
        return new MemberHolder(view);
    }

    @Override
    public void onBindViewHolder(final MemberHolder holder, int position) {
        final ProfileInfo profileInfo = memberList.get(position);

        holder.titleTv.setText(profileInfo.getName());
        glide.load(profileInfo.getThumbnail())
                .placeholder(R.drawable.ic_account_circle_black_no_padding)
                .circleCrop()
                .into(holder.iconIv);

        if (adminList.containsKey(profileInfo.getId())) {
            holder.infoTv.setVisibility(View.VISIBLE);
        } else {
            holder.infoTv.setVisibility(View.GONE);
        }
    }

    public void clear() {
        memberList.clear();
        adminList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    public void addProfile(ProfileInfo profileInfo) {
        memberList.add(profileInfo);
        sort();
        notifyDataSetChanged();
    }

    public void addAdminId(String adminId) {
        adminList.put(adminId, "true");
        notifyDataSetChanged();
    }

    private void sort() {
        Collections.sort(memberList, new Comparator<ProfileInfo>() {
            @Override
            public int compare(ProfileInfo o1, ProfileInfo o2) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
    }

    class MemberHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView iconIv;
        final TextView titleTv;
        final TextView infoTv;

        public MemberHolder(View view) {
            super(view);
            iconIv = view.findViewById(R.id.iv_icon);
            titleTv = view.findViewById(R.id.tv_title);
            infoTv = view.findViewById(R.id.tv_info);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            ProfileActivity.open(v.getContext(), (String) memberList.get(getAdapterPosition()).getId());
        }
    }

}
