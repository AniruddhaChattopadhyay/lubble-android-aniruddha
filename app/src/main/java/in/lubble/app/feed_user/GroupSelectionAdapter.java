package in.lubble.app.feed_user;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.radiobutton.MaterialRadioButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.models.FeedGroupData;
import in.lubble.app.utils.UiUtils;

public class GroupSelectionAdapter extends RecyclerView.Adapter<GroupSelectionAdapter.GroupSelectionViewHolder> {

    private int lastCheckedPos = -1;
    private final List<FeedGroupData> stringList;
    private final List<FeedGroupData> stringListCopy;
    private MaterialButton postSubmitBtn;
    private boolean isQnA;




    public GroupSelectionAdapter(List<FeedGroupData> stringList, MaterialButton postSubmitBtn,boolean isQnA) {
        this.stringList = stringList;
        this.postSubmitBtn = postSubmitBtn;
        stringListCopy = new ArrayList<>();
        stringListCopy.addAll(stringList);
        this.isQnA = isQnA;
        lastCheckedPos = -1;
        if(isQnA) {
            for (int i = 0; i < stringList.size(); i++) {
                if (stringList.get(i).getName().equals("QnAs")) {
                    lastCheckedPos = i;
                }
            }
        }

    }

    @NonNull
    @Override
    public GroupSelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_selection, parent, false);
        return new GroupSelectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupSelectionViewHolder holder, int position) {
        FeedGroupData feedGroupData = stringList.get(position);

        holder.titleTv.setText(feedGroupData.getName());
        holder.followerCountTv.setText(Integer.toString(feedGroupData.getFollowersCount()));
        holder.selectionRb.setChecked(position == lastCheckedPos);
        holder.titleTv.setOnClickListener(v -> holder.selectionRb.performClick());
        holder.groupIv.setOnClickListener(v -> holder.selectionRb.performClick());

        GlideApp.with(holder.itemView.getContext())
                .load(feedGroupData.getPhotoUrl())
                .apply(new RequestOptions().override(UiUtils.dpToPx(32), UiUtils.dpToPx(32)))
                .placeholder(R.drawable.ic_group)
                .circleCrop()
                .error(R.drawable.ic_group)
                .into(holder.groupIv);
        holder.selectionRb.setOnClickListener(v -> {
            int copyOfLastCheckedPosition = lastCheckedPos;
            lastCheckedPos = holder.getBindingAdapterPosition();
            if(stringList.get(lastCheckedPos).isGroupJoined()){
                postSubmitBtn.setText("Post");
            }
            else{
                postSubmitBtn.setText("Join and Post");
            }
            notifyItemChanged(copyOfLastCheckedPosition);
            notifyItemChanged(lastCheckedPos);
        });
    }

    int getLastCheckedPos() {
        return lastCheckedPos;
    }

    public void filter(String text) {
        stringList.clear();
        if (text.isEmpty()) {
            stringList.addAll(stringListCopy);
        } else {
            text = text.toLowerCase();
            for (FeedGroupData groupData : stringListCopy) {
                if (groupData.getName().toLowerCase().contains(text) || groupData.getName().toLowerCase().contains(text)) {
                    stringList.add(groupData);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return stringList.size();
    }

    public static class GroupSelectionViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView titleTv;
        final ImageView groupIv;
        final TextView followerCountTv;
        final MaterialRadioButton selectionRb;

        GroupSelectionViewHolder(View view) {
            super(view);
            mView = view;
            groupIv = view.findViewById(R.id.iv_group_selection);
            titleTv = view.findViewById(R.id.tv_group_name);
            selectionRb = view.findViewById(R.id.rb_group_selection);
            followerCountTv = view.findViewById(R.id.follower_count);
        }

    }

}
