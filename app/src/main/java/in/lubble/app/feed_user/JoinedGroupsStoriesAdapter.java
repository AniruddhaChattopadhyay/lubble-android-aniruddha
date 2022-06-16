package in.lubble.app.feed_user;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import in.lubble.app.GlideApp;
import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.feed_groups.FeedGroupsFrag;
import in.lubble.app.feed_groups.SingleGroupFeed.GroupFeedActivity;
import in.lubble.app.models.FeedGroupData;
import in.lubble.app.utils.UiUtils;

public class JoinedGroupsStoriesAdapter extends RecyclerView.Adapter<JoinedGroupsStoriesAdapter.ViewHolder> {

    private static final String TAG = "StoriesRecyclerViewAdapter";

    private final ArrayList<FeedGroupData> storyDataList;
    private final JoinedGroupsListener joinedGroupsListener;
    private final Context mContext;
    private final static String MORE_GROUPS = "More Groups";

    public JoinedGroupsStoriesAdapter(Context context, ArrayList<FeedGroupData> storyDataList, JoinedGroupsListener joinedGroupsListener) {
        mContext = context;
        this.storyDataList = storyDataList;
        this.joinedGroupsListener = joinedGroupsListener;
        FeedGroupData feedGroupData = new FeedGroupData("More Groups", "Explore Feed", "Lubble");
        this.storyDataList.add(0, feedGroupData);

    }

    public interface JoinedGroupsListener {
        void onExploreClicked();
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
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transform(new CenterCrop(), new RoundedCorners(UiUtils.dpToPx(8)));
        if (feedGroupData.getName().equals(MORE_GROUPS)) {
            glide.load(R.drawable.ic_category_colored)
                    .apply(requestOptions)
                    .into(holder.image);
        } else {
            glide.load(feedGroupData.getPhotoUrl())
                    .placeholder(R.drawable.ic_circle_group_24dp)
                    .error(R.drawable.ic_circle_group_24dp)
                    .apply(requestOptions)
                    .into(holder.image);
        }

        holder.name.setText(feedGroupData.getName());
        holder.itemView.setOnClickListener(view -> {
            if (feedGroupData.getName().equals(MORE_GROUPS)) {
                joinedGroupsListener.onExploreClicked();
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
