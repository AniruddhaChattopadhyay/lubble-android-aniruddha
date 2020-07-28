package in.lubble.app.marketplace;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.models.ProfileInfo;

import static in.lubble.app.firebase.RealtimeDbHelper.getUserInfoRef;

public class ReviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "ReviewAdapter";

    private ArrayList<RatingData> ratingDataList;
    private final GlideRequests glide;

    public ReviewAdapter(GlideRequests glide, ArrayList<RatingData> ratingDataList) {
        this.ratingDataList = ratingDataList;
        this.glide = glide;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ReviewAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        final ReviewAdapter.ViewHolder viewHolder = (ReviewAdapter.ViewHolder) holder;
        final RatingData ratingData = ratingDataList.get(position);

        viewHolder.ratingbar.setRating(ratingData.getStarRating());

        final String review = ratingData.getReview();
        if (!TextUtils.isEmpty(review)) {
            viewHolder.reviewTv.setVisibility(View.VISIBLE);
            viewHolder.reviewTv.setText(review);
        } else {
            viewHolder.reviewTv.setVisibility(View.GONE);
        }

        fetchUserProfileInfo(ratingData.getUserId(), viewHolder);

    }

    private void fetchUserProfileInfo(String userId, final ViewHolder viewHolder) {
        getUserInfoRef(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                try {
                    glide.load(profileInfo == null ? "" : profileInfo.getThumbnail())
                            .circleCrop()
                            .placeholder(R.drawable.ic_account_circle_black_no_padding)
                            .error(R.drawable.ic_account_circle_black_no_padding)
                            .into(viewHolder.userIv);
                    viewHolder.userNameTv.setText(profileInfo == null? "deleted user" : profileInfo.getName());
                } catch (IllegalArgumentException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return ratingDataList.size();
    }

    public void addData(RatingData ratingData) {
        ratingDataList.add(ratingData);
        notifyDataSetChanged();
    }

    public void clear() {
        ratingDataList.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView userIv;
        final TextView userNameTv;
        final RatingBar ratingbar;
        final TextView reviewTv;

        ViewHolder(View view) {
            super(view);
            userIv = view.findViewById(R.id.iv_user_pic);
            userNameTv = view.findViewById(R.id.tv_user_name);
            ratingbar = view.findViewById(R.id.ratingbar_user);
            reviewTv = view.findViewById(R.id.tv_user_review);
        }
    }

}
