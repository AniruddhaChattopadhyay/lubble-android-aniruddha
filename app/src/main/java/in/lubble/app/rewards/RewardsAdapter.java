package in.lubble.app.rewards;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import in.lubble.app.GlideRequests;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.rewards.data.RewardsData;
import in.lubble.app.rewards.data.RewardsRecordData;
import in.lubble.app.utils.RoundedCornersTransformation;

import java.util.ArrayList;
import java.util.List;

import static in.lubble.app.Constants.REWARDS_EXPLAINER;
import static in.lubble.app.utils.UiUtils.dpToPx;

public class RewardsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "RewardsAdapter";
    private static final int TYPE_EXPLAINER = 825;
    private static final int TYPE_REWARD = 467;

    private List<RewardsRecordData> rewardsDataList = new ArrayList<>();
    private GlideRequests glide;
    private Fragment fragment;

    public RewardsAdapter(GlideRequests glide, Fragment fragment) {
        this.glide = glide;
        this.fragment = fragment;
        addExplainer();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && (!LubbleSharedPrefs.getInstance().getIsRewardsExplainerSeen())) {
            return TYPE_EXPLAINER;
        } else {
            return TYPE_REWARD;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_REWARD) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_reward, parent, false);
            return new RewardViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_reward_explainer, parent, false);
            return new ExplainerViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RewardViewHolder) {
            final RewardViewHolder rewardViewHolder = (RewardViewHolder) holder;
            rewardViewHolder.mItem = rewardsDataList.get(position);

            final RewardsData rewardsData = rewardsDataList.get(position).getFields();
            glide.load(rewardsData.getPhoto())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .transform(new RoundedCornersTransformation(dpToPx(24), 0))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            rewardViewHolder.rewardIv.setBackground(null);
                            return false;
                        }
                    })
                    .into(rewardViewHolder.rewardIv);
        } else {
            final ExplainerViewHolder explainerViewHolder = (ExplainerViewHolder) holder;
            glide.load(FirebaseRemoteConfig.getInstance().getString(REWARDS_EXPLAINER)).into(explainerViewHolder.explainerIv);
        }
    }

    void addExplainer() {
        if (!LubbleSharedPrefs.getInstance().getIsRewardsExplainerSeen()) {
            rewardsDataList.add(0, null);
            notifyItemInserted(0);
        }
    }

    private void removeExplainer() {
        if (getItemViewType(0) == TYPE_EXPLAINER) {
            rewardsDataList.remove(0);
            notifyItemRemoved(0);
            LubbleSharedPrefs.getInstance().setIsRewardsExplainerSeen(true);
        }
    }

    @Override
    public int getItemCount() {
        return rewardsDataList.size();
    }

    public void setList(List<RewardsRecordData> activeRewardList) {
        rewardsDataList = activeRewardList;
        addExplainer();
        notifyDataSetChanged();
    }

    void clearAll() {
        rewardsDataList.clear();
        addExplainer();
        notifyDataSetChanged();
    }

    class RewardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final View mView;
        final ImageView rewardIv;

        RewardsRecordData mItem;

        public RewardViewHolder(View view) {
            super(view);
            mView = view;
            rewardIv = view.findViewById(R.id.iv_reward);
            view.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {

            RewardDetailActiv.open(fragment, rewardsDataList.get(getAdapterPosition()).getFields());
        }

    }

    class ExplainerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final View mView;
        final ImageView explainerIv;
        final TextView dismissTv;
        RewardsRecordData mItem;

        public ExplainerViewHolder(View view) {
            super(view);
            mView = view;
            explainerIv = view.findViewById(R.id.iv_explainer);
            dismissTv = view.findViewById(R.id.tv_dismiss);
            dismissTv.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            removeExplainer();
        }
    }
}
