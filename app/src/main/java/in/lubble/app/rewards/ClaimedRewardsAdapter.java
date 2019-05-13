package in.lubble.app.rewards;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.rewards.data.RewardCodesData;
import in.lubble.app.rewards.data.RewardCodesRecordData;
import in.lubble.app.utils.RoundedCornersTransformation;

import java.util.List;

import static in.lubble.app.utils.UiUtils.dpToPx;

public class ClaimedRewardsAdapter extends RecyclerView.Adapter<ClaimedRewardsAdapter.ViewHolder> {

    private final List<RewardCodesRecordData> rewardsDataList;
    private GlideRequests glide;

    public ClaimedRewardsAdapter(List<RewardCodesRecordData> items, GlideRequests glide) {
        rewardsDataList = items;
        this.glide = glide;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reward, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = rewardsDataList.get(position);

        final RewardCodesData rewardsData = rewardsDataList.get(position).getFields();
        glide.load(rewardsData.getPhoto().get(0))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .transform(new RoundedCornersTransformation(dpToPx(24), 0))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.rewardIv.setBackground(null);
                        return false;
                    }
                })
                .into(holder.rewardIv);
    }

    @Override
    public int getItemCount() {
        return rewardsDataList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final View mView;
        final ImageView rewardIv;
        RewardCodesRecordData mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            rewardIv = view.findViewById(R.id.iv_reward);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            RewardDetailActiv.open(v.getContext(), rewardsDataList.get(getAdapterPosition()).getFields());
        }
    }
}
