package in.lubble.app.referrals;

import android.content.Context;
import android.support.annotation.NonNull;
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

    private final List<ReferralLeaderboardData.LeaderboardData> referralList;
    private final GlideRequests glide;
    private final Context context;

    public ReferralLeaderboardAdapter(GlideRequests glide, Context context) {
        referralList = new ArrayList<>();
        this.glide = glide;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ReferralLeaderboardAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_referral_leaderboard, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final ReferralLeaderboardData.LeaderboardData referralPersonData = referralList.get(position);

        final ViewHolder viewHolder = (ViewHolder) holder;

        viewHolder.rankTv.setText(String.valueOf(position + 1));
        viewHolder.nameTv.setText(StringUtils.getTitleCase(referralPersonData.getName()));
        viewHolder.pointsTv.setText(String.valueOf(referralPersonData.getPoints()));

        glide.load(referralPersonData.getThumbnail()).circleCrop()
                .placeholder(R.drawable.ic_account_circle_black_no_padding)
                .error(R.drawable.ic_account_circle_black_no_padding)
                .into(viewHolder.iconIv);

    }

    @Override
    public int getItemCount() {
        return referralList.size();
    }

    public void addPerson(ReferralLeaderboardData.LeaderboardData person) {
        referralList.add(person);
        notifyDataSetChanged();
    }

    public void clear() {
        referralList.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View view;
        final TextView rankTv;
        final ImageView iconIv;
        final TextView nameTv;
        final TextView pointsTv;

        ViewHolder(View view) {
            super(view);
            this.view = view;
            rankTv = view.findViewById(R.id.tv_rank);
            iconIv = view.findViewById(R.id.iv_icon);
            nameTv = view.findViewById(R.id.tv_name);
            pointsTv = view.findViewById(R.id.tv_points);
        }

    }


}
