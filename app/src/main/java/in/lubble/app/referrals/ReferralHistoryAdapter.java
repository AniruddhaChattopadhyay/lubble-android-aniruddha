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
import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ReferralHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "ReferralHistoryAdapter";

    private final List<ReferralPersonData> referralList;
    private final GlideRequests glide;
    private final Context context;

    public ReferralHistoryAdapter(GlideRequests glide, Context context) {
        referralList = new ArrayList<>();
        this.glide = glide;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ReferralHistoryAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_referral_history, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final ReferralPersonData referralPersonData = referralList.get(position);

        final ViewHolder viewHolder = (ViewHolder) holder;

        viewHolder.nameTv.setText(StringUtils.getTitleCase(referralPersonData.getName()));
        viewHolder.pointCountTv.setText(referralPersonData.getPoints());

        if (referralPersonData.getType() <= 1) {
            viewHolder.sellerIconIv.setVisibility(View.VISIBLE);
            viewHolder.sellerTv.setVisibility(View.VISIBLE);
            viewHolder.joinedIconIv.setVisibility(View.VISIBLE);
            viewHolder.joinedTv.setVisibility(View.VISIBLE);
            viewHolder.bonusReasonTv.setVisibility(View.GONE);

            glide.load(referralPersonData.getThumbnail()).circleCrop()
                    .placeholder(R.drawable.ic_account_circle_black_no_padding)
                    .error(R.drawable.ic_account_circle_black_no_padding)
                    .into(viewHolder.iconIv);

            if (referralPersonData.getIsSeller()) {
                viewHolder.sellerTv.setText("Became a seller");
                viewHolder.sellerTv.setTextColor(ContextCompat.getColor(context, R.color.black));
                viewHolder.sellerIconIv.setImageResource(R.drawable.ic_check_circle_black_24dp);
            } else {
                viewHolder.sellerTv.setText("Not yet a seller");
                viewHolder.sellerTv.setTextColor(ContextCompat.getColor(context, R.color.default_text_color));
                viewHolder.sellerIconIv.setImageResource(R.drawable.ic_cancel_black_24dp);
            }
        } else {
            viewHolder.nameTv.setText("Bonus");
            viewHolder.bonusReasonTv.setText(referralPersonData.getBonusReason());
            viewHolder.bonusReasonTv.setVisibility(View.VISIBLE);
            viewHolder.sellerIconIv.setVisibility(View.GONE);
            viewHolder.sellerTv.setVisibility(View.GONE);
            viewHolder.joinedIconIv.setVisibility(View.GONE);
            viewHolder.joinedTv.setVisibility(View.GONE);

            glide.load(R.drawable.ic_medal).circleCrop()
                    .placeholder(R.drawable.ic_account_circle_black_no_padding)
                    .error(R.drawable.ic_account_circle_black_no_padding)
                    .into(viewHolder.iconIv);
        }

    }

    @Override
    public int getItemCount() {
        return referralList.size();
    }

    public void addReferral(ReferralPersonData person) {
        referralList.add(person);
        notifyDataSetChanged();
    }

    public void clear() {
        referralList.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View view;
        final ImageView iconIv;
        final ImageView joinedIconIv;
        final ImageView sellerIconIv;
        final TextView nameTv;
        final TextView joinedTv;
        final TextView sellerTv;
        final TextView bonusReasonTv;
        final TextView pointCountTv;

        ViewHolder(View view) {
            super(view);
            this.view = view;
            iconIv = view.findViewById(R.id.iv_referral_pic);
            nameTv = view.findViewById(R.id.tv_name);
            joinedIconIv = view.findViewById(R.id.iv_joined);
            joinedTv = view.findViewById(R.id.tv_joined);
            sellerIconIv = view.findViewById(R.id.iv_seller);
            sellerTv = view.findViewById(R.id.tv_seller);
            bonusReasonTv = view.findViewById(R.id.tv_bonus_reason);
            pointCountTv = view.findViewById(R.id.tv_point_count);
        }

    }


}
