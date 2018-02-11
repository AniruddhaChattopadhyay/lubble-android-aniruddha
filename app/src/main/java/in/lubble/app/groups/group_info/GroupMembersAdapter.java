package in.lubble.app.groups.group_info;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.models.ProfileInfo;

/**
 * Created by ishaan on 11/2/18.
 */

public class GroupMembersAdapter extends RecyclerView.Adapter<GroupMembersAdapter.MemberHolder> {

    private final ArrayList<String> memberList;

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
        final String memberId = memberList.get(position);

        FirebaseDatabase.getInstance().getReference("users/" + memberId + "/info").addListenerForSingleValueEvent(new ValueEventListener() {
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
    }

    public void addAllMembers(ArrayList<String> memberList) {
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

        public MemberHolder(View view) {
            super(view);
            iconIv = view.findViewById(R.id.iv_icon);
            titleTv = view.findViewById(R.id.tv_title);
        }
    }

}
