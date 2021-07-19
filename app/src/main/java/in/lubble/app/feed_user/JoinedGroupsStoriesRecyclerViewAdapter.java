package in.lubble.app.feed_user;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.lubble.app.LubbleApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.chat.stories.StoryData;
import in.lubble.app.feed_groups.FeedGroupsFrag;
import in.lubble.app.feed_groups.SingleGroupFeed.GroupFeedActivity;
import in.lubble.app.models.FeedGroupData;
import in.lubble.app.utils.StringUtils;
import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.OnStoryChangedCallback;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;
import retrofit2.Callback;

public class JoinedGroupsStoriesRecyclerViewAdapter extends RecyclerView.Adapter<JoinedGroupsStoriesRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "StoriesRecyclerViewAdapter";

    private ArrayList<FeedGroupData> storyDataList;
    private FragmentManager fmContext;
    private Context mContext;

    public JoinedGroupsStoriesRecyclerViewAdapter(Context context, ArrayList<FeedGroupData> storyDataList, FragmentManager fmContext) {
        mContext = context;
        this.storyDataList = storyDataList;
        this.fmContext = fmContext;
        FeedGroupData feedGroupData = new FeedGroupData("Explore","Explore Feed","Lubble");
        feedGroupData.setPhotoUrl("https://media.istockphoto.com/vectors/black-plus-sign-positive-symbol-vector-id688550958?k=6&m=688550958&s=612x612&w=0&h=nVa-a5Fb79Dgmqk3F00kop9kF4CXFpF4kh7vr91ERGk=");
        this.storyDataList.add(feedGroupData);

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.story_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        FeedGroupData feedGroupData = storyDataList.get(position);
        if(feedGroupData.getPhotoUrl() ==null){
            Glide.with(mContext)
                    .asBitmap()
                    .load("https://student.argiaacademy.sch.id/wp-content/plugins/profilegrid-user-profiles-groups-and-communities/public/partials/images/default-group.png")
                    .circleCrop()
                    .into(holder.image);
        }
        else{
            Glide.with(mContext)
                    .asBitmap()
                    .load(feedGroupData.getPhotoUrl())
                    .circleCrop()
                    .into(holder.image);
        }
        holder.name.setText(feedGroupData.getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(feedGroupData.getName().equals("Explore")){
                    FragmentManager fm = fmContext;
                    fm.beginTransaction().replace(R.id.tv_book_author, FeedGroupsFrag.newInstance()).commitAllowingStateLoss();
                }
                else{
                    GroupFeedActivity.open(mContext, feedGroupData);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return storyDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_view);
            name = itemView.findViewById(R.id.name);
        }
    }
}
