package in.lubble.app.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.models.ProfileInfo;

public class ChatUserTagsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ProfileInfo> profileInfoList = new ArrayList<>();
    private Context context;
    private final GlideRequests glide;
    private final OnUserTagClick onUserTagClickListener;

    public ChatUserTagsAdapter(Context context, GlideRequests glide, OnUserTagClick onUserTagClickListener) {
        this.context = context;
        this.glide = glide;
        this.onUserTagClickListener = onUserTagClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new TagViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_tag, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final ProfileInfo profileInfo = profileInfoList.get(position);
        final TagViewHolder viewHolder = (TagViewHolder) holder;

        glide.load(profileInfo.getThumbnail())
                .placeholder(R.drawable.ic_account_circle_black_no_padding)
                .error(R.drawable.ic_account_circle_black_no_padding)
                .circleCrop()
                .into(viewHolder.iconIv);

        viewHolder.titleTv.setText(profileInfo.getName());
    }

    void replaceUserList(List<ProfileInfo> profileInfoList) {
        this.profileInfoList.clear();
        this.profileInfoList = profileInfoList;
        notifyDataSetChanged();
    }

    public void clear() {
        profileInfoList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return profileInfoList.size();
    }

    class TagViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final View mView;
        final ImageView iconIv;
        final TextView titleTv;

        TagViewHolder(View view) {
            super(view);
            mView = view;
            iconIv = view.findViewById(R.id.iv_tag_user);
            titleTv = view.findViewById(R.id.tv_tag_user);
            mView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onUserTagClickListener.onUserTagClick(profileInfoList.get(getAdapterPosition()));
        }
    }

    public interface OnUserTagClick {
        void onUserTagClick(ProfileInfo profileInfo);
    }

}
