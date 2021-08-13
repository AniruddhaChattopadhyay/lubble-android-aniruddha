package in.lubble.app.feed_user;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import in.lubble.app.GlideApp;
import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.feed_groups.FeedGroupsFrag;
import in.lubble.app.feed_groups.SingleGroupFeed.GroupFeedActivity;
import in.lubble.app.models.FeedGroupData;

public class JoinedGroupsStoriesAdapter extends RecyclerView.Adapter<JoinedGroupsStoriesAdapter.ViewHolder> {

    private static final String TAG = "StoriesRecyclerViewAdapter";

    private final ArrayList<FeedGroupData> storyDataList;
    private final FragmentManager fmContext;
    private final Context mContext;
    private final static String MORE_GROUPS = "More Groups";

    public JoinedGroupsStoriesAdapter(Context context, ArrayList<FeedGroupData> storyDataList, FragmentManager fmContext) {
        mContext = context;
        this.storyDataList = storyDataList;
        this.fmContext = fmContext;
        FeedGroupData feedGroupData = new FeedGroupData("More Groups", "Explore Feed", "Lubble");
        this.storyDataList.add(feedGroupData);

    }

    @Override
    public @NotNull ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_joined_groups, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull ViewHolder holder, final int position) {
        FeedGroupData feedGroupData = storyDataList.get(position);

        GlideRequests glide = GlideApp.with(mContext);
        if (feedGroupData.getName().equals(MORE_GROUPS)) {
            glide.load(R.drawable.ic_category_colored)
                    .into(holder.image);
        } else {
            glide.load(feedGroupData.getPhotoUrl())
                    .placeholder(R.drawable.ic_circle_group_24dp)
                    .error(R.drawable.ic_circle_group_24dp)
                    .circleCrop()
                    .into(holder.image);
        }

        holder.name.setText(feedGroupData.getName());
        holder.itemView.setOnClickListener(view -> {
            if (feedGroupData.getName().equals(MORE_GROUPS)) {
                fmContext.beginTransaction().replace(R.id.tv_book_author, FeedGroupsFrag.newInstance()).commitAllowingStateLoss();
            } else {
                GroupFeedActivity.open(mContext, feedGroupData);
            }
        });

    }

    @Override
    public int getItemCount() {
        return storyDataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_view);
            name = itemView.findViewById(R.id.name);
        }
    }
}
