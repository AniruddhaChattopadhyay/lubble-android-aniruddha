package in.lubble.app.chat.stories;

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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.utils.StringUtils;
import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.OnStoryChangedCallback;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class StoriesRecyclerViewAdapter extends RecyclerView.Adapter<StoriesRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "StoriesRecyclerViewAdapter";

    //vars
    //private ArrayList<String> mNames = new ArrayList<>();
    //private ArrayList<String> mImageUrls = new ArrayList<>();
    private ArrayList<StoryData> storyDataList;
    private Context mContext;

    public StoriesRecyclerViewAdapter(Context context, ArrayList<StoryData> storyDataList) {
//        mNames = names;
//        mImageUrls = imageUrls;
        mContext = context;
        this.storyDataList = storyDataList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.story_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        StoryData storyData = storyDataList.get(position);
        Glide.with(mContext)
                .asBitmap()
                .load(storyData.getStoryPic())
                .circleCrop()
                .into(holder.image);

        holder.name.setText(storyData.getStoryName());
        //holder.name.setText(mNames.get(0));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                Toast.makeText(mContext, mNames.get(position), Toast.LENGTH_SHORT).show();
                showStories(storyData.getStory(), storyData.getStoryName(), storyData.getStoryPic());
            }
        });
    }

    @Override
    public int getItemCount() {
        return storyDataList.size();
    }

    public void showStories(ArrayList<HashMap<String, Object>> storyList, String storyTitle, String storyLogo) {

        final ArrayList<MyStory> myStories = new ArrayList<>();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");

        for (HashMap<String, Object> storylist : storyList) {
            String captions = storylist.get("caption").toString();
            MyStory story1 = new MyStory(
                    (String) storylist.get("url"),
                    null,                        //simpleDateFormat.parse("20-10-2019 10:00:00")
                    (String) storylist.get("caption")
            );
            myStories.add(story1);
        }

        new StoryView.Builder(((AppCompatActivity) mContext).getSupportFragmentManager())
                .setStoriesList(myStories)
                .setStoryDuration(5000)
                .setTitleText(storyTitle)
                .setTitleLogoUrl(storyLogo)
                .setSubtitleText(LubbleSharedPrefs.getInstance().getLubbleName())
                .setStoryClickListeners(new StoryClickListeners() {
                    @Override
                    public void onDescriptionClickListener(int position) {
                        HashMap<String, Object> storyMap = storyList.get(position);
                        Log.d("testing", "clicked" + storyMap.get("link"));
                        if (storyMap.get("link") != null) {
                            String link = storyMap.get("link").toString();
                            final Bundle bundle = new Bundle();
                            bundle.putString("placeUrl", storyMap.get("url").toString());
                            if (StringUtils.isValidMobile(link)) {
                                bundle.putString("action", "phone");
                                Intent intent = new Intent(Intent.ACTION_DIAL);
                                intent.setData(Uri.parse("tel:" + link));
                                mContext.startActivity(intent);
                            } else {
                                bundle.putString("action", "link");
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(link));
                                mContext.startActivity(intent);
                            }
                            Analytics.triggerEvent(AnalyticsEvents.COLLECTION_PLACE_CTA_CLICK, bundle, mContext);
                        }
                    }

                    @Override
                    public void onTitleIconClickListener(int position) {
                    }
                })
                .setOnStoryChangedCallback(new OnStoryChangedCallback() {
                    @Override
                    public void storyChanged(int position) {
                    }
                })
                .setStartingIndex(0)
                .setRtl(false)
                .build()
                .show();

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
