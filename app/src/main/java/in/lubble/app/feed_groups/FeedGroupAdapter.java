package in.lubble.app.feed_groups;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.emoji.widget.EmojiTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.models.FeedGroupData;
import in.lubble.app.utils.UiUtils;

public class FeedGroupAdapter extends RecyclerView.Adapter<FeedGroupAdapter.FeedGroupViewHolder> implements Filterable {

    private List<FeedGroupData> feedGroupList;
    private List<FeedGroupData> feedGroupList_copy;
    private final GlideRequests glide;
    private final OnListFragmentInteractionListener mListener;
    private final boolean isOnboarding;
    private HashMap<String, Boolean> selectedMap = new HashMap<>();

    public FeedGroupAdapter(List<FeedGroupData> feedGroupList, OnListFragmentInteractionListener listener, GlideRequests glide, boolean isOnboarding) {
        this.feedGroupList = feedGroupList;
        feedGroupList_copy = new ArrayList<>(feedGroupList);
        this.glide = glide;
        mListener = listener;
        this.isOnboarding = isOnboarding;
    }

    public void updateList(List<FeedGroupData> items) {
        this.feedGroupList = items;
        this.feedGroupList_copy = new ArrayList<>(items);
        notifyDataSetChanged();
    }

    public void updateGroup(FeedGroupData groupData) {
        int index = feedGroupList.indexOf(groupData);
        if (index != -1) {
            //FeedGroupData oldGroupData = feedGroupList.get(index);
            //groupData.setPriority(oldExploreGroupData.getPriority());
            this.feedGroupList.set(index, groupData);
            this.feedGroupList_copy.set(feedGroupList_copy.indexOf(groupData), groupData);
            notifyItemChanged(index);
        }
    }

    @NonNull
    @Override
    public FeedGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.feed_groups_recyclerview_row, parent, false);
        return new FeedGroupViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedGroupViewHolder holder, int position) {
        FeedGroupData feedGroupData = feedGroupList.get(position);
        holder.groupNameTV.setText(feedGroupData.getName());

        /*todo RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCornersTransformation(UiUtils.dpToPx(8), 0, TOP));
        glide.load(feedGroupData.getPhotoUrl())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.rounded_rect_gray)
                .error(R.drawable.explore_default)
                .apply(requestOptions)
                .into(holder.imageView);*/

        if (isOnboarding) {
            holder.joinTv.setText("SELECT");
            holder.joinTv.setTextColor(ContextCompat.getColor(holder.mView.getContext(), R.color.colorAccent));
            if (selectedMap.containsKey(feedGroupData.getId())) {
                holder.selectedContainer.setVisibility(View.VISIBLE);
                holder.joinTv.setText("REMOVE");
                holder.joinTv.setTextColor(ContextCompat.getColor(holder.mView.getContext(), R.color.red));
            } else {
                holder.selectedContainer.setVisibility(View.GONE);
                holder.joinTv.setText("SELECT");
                holder.joinTv.setTextColor(ContextCompat.getColor(holder.mView.getContext(), R.color.colorAccent));
            }
        } else {
            holder.joinTv.setText("VIEW");
            holder.joinTv.setTextColor(ContextCompat.getColor(holder.mView.getContext(), R.color.colorAccent));
        }

        initCardClickListener(holder, feedGroupData);
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(String groupId, boolean isAdded);
        void openGroup(FeedGroupData feedGroupData);
    }

    private void initCardClickListener(final FeedGroupAdapter.FeedGroupViewHolder holder, final FeedGroupData exploreGroupData) {
        holder.mView.setOnClickListener(v -> {
            if (null != mListener) {
                if (isOnboarding) {
                    try {
                        String touchedGroupId = feedGroupList.get(holder.getAdapterPosition()).getId().toString();
                        if (holder.selectedContainer.getVisibility() == View.GONE) {
                            if (selectedMap.size() < 10) {
                                mListener.onListFragmentInteraction(touchedGroupId, true);
                                UiUtils.animateFadeShow(holder.mView.getContext(), holder.selectedContainer);
                                holder.joinTv.setText("REMOVE");
                                holder.joinTv.setTextColor(ContextCompat.getColor(holder.mView.getContext(), R.color.red));
                                selectedMap.put(touchedGroupId, true);
                            } else {
                                Toast.makeText(v.getContext(), "You can join more groups later", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            mListener.onListFragmentInteraction(touchedGroupId, false);
                            UiUtils.animateFadeHide(holder.mView.getContext(), holder.selectedContainer);
                            holder.joinTv.setText("SELECT");
                            holder.joinTv.setTextColor(ContextCompat.getColor(holder.mView.getContext(), R.color.colorAccent));
                            selectedMap.remove(touchedGroupId);
                        }
                    } catch (IndexOutOfBoundsException e) {
                        Toast.makeText(v.getContext(), R.string.all_something_wrong_try_again, Toast.LENGTH_SHORT).show();
                        FirebaseCrashlytics.getInstance().log("IndexOutOfBoundsException in onboarding group selection");
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }
                } else {
                    // for explore bottom tab
                    mListener.openGroup(exploreGroupData);
                }
            }
        });
    }

    public static class FeedGroupViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final ImageView imageView;
        final RelativeLayout selectedContainer;
        final TextView joinTv, memberCountTv, labelTv;
        final ProgressBar joinProgressbar;
        private EmojiTextView groupNameTV;

        public FeedGroupViewHolder(View view) {
            super(view);
            mView = view;
            imageView = view.findViewById(R.id.iv_wheretonight_pic);
            selectedContainer = view.findViewById(R.id.container_selected);
            groupNameTV = view.findViewById(R.id.tv_group_title);
            joinTv = view.findViewById(R.id.tv_join);
            memberCountTv = view.findViewById(R.id.tv_member_count);
            labelTv = view.findViewById(R.id.tv_label);
            joinProgressbar = view.findViewById(R.id.progressbar_join);
        }
    }

    @Override
    public int getItemCount() {
        return feedGroupList.size();
    }

    @Override
    public Filter getFilter() {
        return exploreFilter;
    }

    private final Filter exploreFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<FeedGroupData> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0)
                filteredList.addAll(feedGroupList_copy);
            else {
                String filterString = constraint.toString().toLowerCase().trim();
                for (FeedGroupData item : feedGroupList_copy) {
                    if (item.getName().toLowerCase().contains(filterString)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            feedGroupList.clear();
            feedGroupList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

}
