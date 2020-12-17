package in.lubble.app.chat.stories;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import in.lubble.app.R;
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
        Glide.with(mContext)
                .asBitmap()
                .load(storyDataList.get(position).getStoryPic())
                .into(holder.image);

        holder.name.setText(storyDataList.get(position).getStoryName());
        //holder.name.setText(mNames.get(0));

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

//                Toast.makeText(mContext, mNames.get(position), Toast.LENGTH_SHORT).show();
                showStories(storyDataList.get(position).getStory(),storyDataList.get(position).getStoryName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return storyDataList.size();
    }

    public void showStories(ArrayList<HashMap<String, Object>> storyList, String storyTitle) {

        final ArrayList<MyStory> myStories = new ArrayList<>();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");

        for (HashMap<String, Object> storylist : storyList) {
            try {
                MyStory story1 = new MyStory(
                        (String) storylist.get("url"),
                        null,                        //simpleDateFormat.parse("20-10-2019 10:00:00")
                        (String) storylist.get("caption")
                );

                myStories.add(story1);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

//        try {
//            MyStory story1 = new MyStory(
//                    "https://media.pri.org/s3fs-public/styles/story_main/public/images/2019/09/092419-germany-climate.jpg?itok=P3FbPOp-",
//                    simpleDateFormat.parse("20-10-2019 10:00:00")
//            );
//            myStories.add(story1);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            MyStory story2 = new MyStory(
//                    "http://i.imgur.com/0BfsmUd.jpg",
//                    simpleDateFormat.parse("26-10-2019 15:00:00"),
//                    "#TEAM_STANNIS"
//            );
//            myStories.add(story2);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//        MyStory story3 = new MyStory(
//                "https://mfiles.alphacoders.com/681/681242.jpg"
//        );
//        myStories.add(story3);

        new StoryView.Builder(((AppCompatActivity) mContext).getSupportFragmentManager())
                .setStoriesList(myStories)
                .setStoryDuration(5000)
                .setTitleText(storyTitle)
//                .setSubtitleText("Damascus")
                .setStoryClickListeners(new StoryClickListeners() {
                    @Override
                    public void onDescriptionClickListener(int position) {
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

    public class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView image;
        TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_view);
            name = itemView.findViewById(R.id.name);
        }
    }
}
