package in.lubble.app.referrals;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.utils.StringUtils;

public class ReferralLeaderboardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "ReferralLeaderboardAdapter";

    private static final int TYPE_NORMAL = 898;
    private static final int TYPE_CURR_USER = 154;

    private final List<LeaderboardPersonData> referralList;
    private final GlideRequests glide;
    private final Context context;

    public ReferralLeaderboardAdapter(GlideRequests glide, Context context) {
        referralList = new ArrayList<>();
        this.glide = glide;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        if (referralList.get(position).getCurrentUserRank() > 0) {
            return TYPE_CURR_USER;
        } else {
            return TYPE_NORMAL;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NormalViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_referral_leaderboard, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final LeaderboardPersonData referralPersonData = referralList.get(position);

        final NormalViewHolder normalViewHolder = (NormalViewHolder) holder;

        normalViewHolder.rankTv.setText(String.valueOf(position + 1));
        normalViewHolder.nameTv.setText(StringUtils.getTitleCase(referralPersonData.getName()));
        normalViewHolder.pointsTv.setText(String.valueOf(referralPersonData.getPoints()));

        glide.load(referralPersonData.getThumbnail()).circleCrop()
                .placeholder(R.drawable.ic_account_circle_black_no_padding)
                .error(R.drawable.ic_account_circle_black_no_padding)
                .into(normalViewHolder.iconIv);

        if (referralPersonData.getCurrentUserRank() > 0) {
            normalViewHolder.view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
            normalViewHolder.rankTv.setText(String.valueOf(referralPersonData.getCurrentUserRank()));
            normalViewHolder.rankTv.setTextColor(ContextCompat.getColor(context, R.color.white));
            normalViewHolder.nameTv.setTextColor(ContextCompat.getColor(context, R.color.white));
            normalViewHolder.pointsTv.setTextColor(ContextCompat.getColor(context, R.color.white));
        } else {
            normalViewHolder.view.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            normalViewHolder.rankTv.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
            normalViewHolder.nameTv.setTextColor(ContextCompat.getColor(context, R.color.black));
            normalViewHolder.pointsTv.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        }
    }

    @Override
    public int getItemCount() {
        return referralList.size();
    }

    public void addPerson(LeaderboardPersonData person) {
        referralList.add(person);
        notifyDataSetChanged();
    }

    public void clear() {
        referralList.clear();
        notifyDataSetChanged();
    }

    class NormalViewHolder extends RecyclerView.ViewHolder {
        final View view;
        final TextView rankTv;
        final ImageView iconIv;
        final TextView nameTv;
        final TextView pointsTv;

        NormalViewHolder(View view) {
            super(view);
            this.view = view;
            rankTv = view.findViewById(R.id.tv_rank);
            iconIv = view.findViewById(R.id.iv_icon);
            nameTv = view.findViewById(R.id.tv_name);
            pointsTv = view.findViewById(R.id.tv_points);
        }

    }

    class CurrUserViewHolder extends RecyclerView.ViewHolder {
        final View view;
        final TextView rankTv;
        final ImageView iconIv;
        final TextView nameTv;
        final TextView pointsTv;

        CurrUserViewHolder(View view) {
            super(view);
            this.view = view;
            rankTv = view.findViewById(R.id.tv_rank);
            iconIv = view.findViewById(R.id.iv_icon);
            nameTv = view.findViewById(R.id.tv_name);
            pointsTv = view.findViewById(R.id.tv_points);
        }

    }

}
