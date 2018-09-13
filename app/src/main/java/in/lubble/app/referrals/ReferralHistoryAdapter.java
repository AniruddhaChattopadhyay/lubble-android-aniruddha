package in.lubble.app.referrals;

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

public class ReferralHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "ReferralHistoryAdapter";

    private final List<ReferralPersonData> referralList;
    private final GlideRequests glide;

    public ReferralHistoryAdapter(GlideRequests glide) {
        referralList = new ArrayList<>();
        this.glide = glide;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ReferralHistoryAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_referral_history, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

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
            pointCountTv = view.findViewById(R.id.tv_point_count);
        }

    }


}
