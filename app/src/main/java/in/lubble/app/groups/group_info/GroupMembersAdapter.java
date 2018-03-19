package in.lubble.app.groups.group_info;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.models.ProfileInfo;

import static in.lubble.app.firebase.RealtimeDbHelper.getUserInfoRef;

/**
 * Created by ishaan on 11/2/18.
 */

public class GroupMembersAdapter extends RecyclerView.Adapter<GroupMembersAdapter.MemberHolder> {

    private final List<Map.Entry> memberList;

    public GroupMembersAdapter() {
        memberList = new ArrayList<>();
    }

    @Override
    public MemberHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_icon_text, parent, false);
        return new MemberHolder(view);
    }

    @Override
    public void onBindViewHolder(final MemberHolder holder, int position) {
        final Map.Entry memberEntry = memberList.get(position);
        final String memberId = (String) memberEntry.getKey();

        // Single listener becoz it's difficult to keep track of multiple listeners in adapter....
        getUserInfoRef(memberId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                if (profileInfo != null) {
                    holder.titleTv.setText(profileInfo.getName());
                    GlideApp.with(holder.itemView.getContext())
                            .load(profileInfo.getThumbnail())
                            .placeholder(R.drawable.ic_account_circle_black_no_padding)
                            .circleCrop()
                            .into(holder.iconIv);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final HashMap memberPropertyMap = (HashMap) memberEntry.getValue();
        holder.infoTv.setVisibility(memberPropertyMap.get("admin") == Boolean.TRUE ? View.VISIBLE : View.GONE);

    }

    public void addAllMembers(List<Map.Entry> memberList) {
        this.memberList.addAll(memberList);
        notifyDataSetChanged();
    }

    public void clear() {
        memberList.clear();
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    class MemberHolder extends RecyclerView.ViewHolder {
        final ImageView iconIv;
        final TextView titleTv;
        final TextView infoTv;

        public MemberHolder(View view) {
            super(view);
            iconIv = view.findViewById(R.id.iv_icon);
            titleTv = view.findViewById(R.id.tv_title);
            infoTv = view.findViewById(R.id.tv_info);
        }
    }

}
