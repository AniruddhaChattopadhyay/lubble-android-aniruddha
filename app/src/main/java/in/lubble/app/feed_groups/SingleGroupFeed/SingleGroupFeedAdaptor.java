package in.lubble.app.feed_groups.SingleGroupFeed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import in.lubble.app.R;
import io.getstream.core.models.Activity;

public class SingleGroupFeedAdaptor extends RecyclerView.Adapter<SingleGroupFeedAdaptor.MyViewHolder> {

    private List<Activity> activityList;
    private Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView textContentTv;
        private ImageView photoContentIv;
        private TextView authorNameTv;
        private TextView timePostedTv;

        public MyViewHolder(View view) {
            super(view);
            textContentTv = view.findViewById(R.id.feed_text_content);
            photoContentIv = view.findViewById(R.id.feed_photo_content);
            authorNameTv = view.findViewById(R.id.feed_author_name);
            timePostedTv = view.findViewById(R.id.feed_post_timestamp);
        }
    }


    public SingleGroupFeedAdaptor(Context context, List<Activity> moviesList) {
        this.activityList = moviesList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.feed_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Activity activity = activityList.get(position);
        String postDateDisplay = getPostDateDisplay(activity.getTime());
        Map<String,Object> extras = activity.getExtra();
        if(extras.containsKey("message")){
            holder.textContentTv.setVisibility(View.VISIBLE);
            holder.textContentTv.setText(extras.get("message").toString());
        }
        if(extras.containsKey("photoLink")){
            holder.photoContentIv.setVisibility(View.VISIBLE);
                Glide.with(context)
                .asBitmap()
                .load(extras.get("photoLink").toString())
                .circleCrop()
                .into(holder.photoContentIv);
        }

        if(extras.containsKey("authorName")){
            holder.authorNameTv.setText(extras.get("authorName").toString());
        }
        holder.timePostedTv.setText(postDateDisplay);
    }

    private String getPostDateDisplay(Date timePosted){
        Date timeNow = new Date(System.currentTimeMillis());
        long duration  = timeNow.getTime() - timePosted.getTime();

        long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(duration);
        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        long diffInHours = TimeUnit.MILLISECONDS.toHours(duration);
        long diffInDays = TimeUnit.MILLISECONDS.toDays(duration);

        if(diffInDays>0){
            return diffInDays + "D";
        }
        else if(diffInHours>0){
            return diffInHours + "Hr";
        }
        else if(diffInMinutes>0){
            return diffInMinutes + "Min";
        }
        else if(diffInSeconds>0){
            return "Just Now";
        }
        return "some time ago";
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }
}
