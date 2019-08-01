package in.lubble.app.leaderboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.models.ProfileData;
import in.lubble.app.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private final List<ProfileData> profileList;
    private final GlideRequests glide;
    private final Context context;
    private final OnListFragmentInteractionListener mListener;

    public LeaderboardAdapter(GlideRequests glide, OnListFragmentInteractionListener listener, Context context) {
        profileList = new ArrayList<>();
        this.glide = glide;
        this.context = context;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final ProfileData referralPersonData = profileList.get(position);
        holder.rankTv.setText(String.valueOf(position + 4));
        holder.nameTv.setText(StringUtils.getTitleCase(referralPersonData.getInfo().getName()));
        holder.pointsTv.setText(String.valueOf(referralPersonData.getLikes()));

        glide.load(referralPersonData.getInfo().getThumbnail()).circleCrop()
                .placeholder(R.drawable.ic_account_circle_black_no_padding)
                .error(R.drawable.ic_account_circle_black_no_padding)
                .into(holder.iconIv);

        /*if (referralPersonData.getId().equalsIgnoreCase(FirebaseAuth.getInstance().getUid())) {
            holder.view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
            if (referralPersonData.getCurrentUserRank() > 0) {
                holder.rankTv.setText(String.valueOf(referralPersonData.getCurrentUserRank()));
            }
            holder.rankTv.setTextColor(ContextCompat.getColor(context, R.color.white));
            holder.nameTv.setTextColor(ContextCompat.getColor(context, R.color.white));
            holder.pointsTv.setTextColor(ContextCompat.getColor(context, R.color.white));
        } else {
            holder.view.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            holder.rankTv.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
            holder.nameTv.setTextColor(ContextCompat.getColor(context, R.color.black));
            holder.pointsTv.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        }*/

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(profileList.get(holder.getAdapterPosition()).getId());
                }
            }
        });
    }

    public void addList(List<ProfileData> profileDataList) {
        this.profileList.addAll(profileDataList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return profileList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

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
            pointsTv = view.findViewById(R.id.tv_likes);
        }
    }

    public void clear() {
        profileList.clear();
        notifyDataSetChanged();
    }

}
