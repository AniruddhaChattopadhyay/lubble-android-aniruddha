package in.lubble.app.referrals;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.utils.StringUtils;
import in.lubble.app.utils.mapUtils.MathUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ReferralLeaderboardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "ReferralLeaderboardAdapter";

    private final List<LeaderboardPersonData> referralList;
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

        if (referralPersonData.getUid().equalsIgnoreCase(FirebaseAuth.getInstance().getUid())) {
            normalViewHolder.view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
            if (referralPersonData.getCurrentUserRank() > 0) {
                normalViewHolder.rankTv.setText(String.valueOf(referralPersonData.getCurrentUserRank()));
            }
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
        return referralList.size() > 10 ? 10 : referralList.size();
    }

    public void addPerson(LeaderboardPersonData person) {
        referralList.add(person);
        // sort by coins desc
        Collections.sort(referralList, new Comparator<LeaderboardPersonData>() {
            @Override
            public int compare(LeaderboardPersonData o1, LeaderboardPersonData o2) {
                // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending=
                return MathUtil.compareDesc(o1.getPoints(), o2.getPoints());
            }
        });
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
