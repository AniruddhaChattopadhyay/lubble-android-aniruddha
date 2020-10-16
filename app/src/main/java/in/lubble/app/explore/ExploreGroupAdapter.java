package in.lubble.app.explore;

import android.content.res.ColorStateList;
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

import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.emoji.widget.EmojiTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import in.lubble.app.GlideRequests;
import in.lubble.app.LubbleApp;
import in.lubble.app.R;
import in.lubble.app.chat.ChatActivity;
import in.lubble.app.utils.RoundedCornersTransformation;
import in.lubble.app.utils.UiUtils;

import static in.lubble.app.utils.RoundedCornersTransformation.CornerType.TOP;

public class ExploreGroupAdapter extends RecyclerView.Adapter<ExploreGroupAdapter.ViewHolder> implements Filterable {

    private static final String TAG = "ExploreGroupAdapter";
    private List<ExploreGroupData> mValues;
    private List<ExploreGroupData> mValues_copy;
    private final OnListFragmentInteractionListener mListener;
    private final GlideRequests glide;
    private final boolean isOnboarding;
    private int lubbleMemberCount = 500;
    private HashMap<Integer, Boolean> selectedMap = new HashMap<>();

    public ExploreGroupAdapter(List<ExploreGroupData> items, OnListFragmentInteractionListener listener, GlideRequests glide, boolean isOnboarding) {
        mValues = items;
        mValues_copy = new ArrayList<>(items);
        mListener = listener;
        this.glide = glide;
        this.isOnboarding = isOnboarding;
    }

    public void updateList(List<ExploreGroupData> items) {
        this.mValues = items;
        this.mValues_copy = new ArrayList<>(items);
        notifyDataSetChanged();
    }

    public void updateGroup(ExploreGroupData exploreGroupData) {
        int existingGroupIndex = mValues.indexOf(exploreGroupData);
        if (existingGroupIndex != -1) {
            this.mValues.set(existingGroupIndex, exploreGroupData);
            this.mValues_copy.set(mValues_copy.indexOf(exploreGroupData), exploreGroupData);
            notifyItemChanged(existingGroupIndex);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_explore_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final ExploreGroupData exploreGroupData = mValues.get(position);
        holder.groupItem = exploreGroupData;
        holder.titleTv.setText(exploreGroupData.getTitle());
        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCornersTransformation(UiUtils.dpToPx(8), 0, TOP));
        glide.load(exploreGroupData.getPhotoUrl())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.rounded_rect_gray)
                .error(R.drawable.explore_default)
                .apply(requestOptions)
                .into(holder.imageView);

        if (isOnboarding) {
            holder.joinTv.setText("SELECT");
            holder.joinTv.setTextColor(ContextCompat.getColor(holder.mView.getContext(), R.color.colorAccent));
            if (selectedMap.containsKey(position)) {
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

        if (!isOnboarding) {
            holder.joinTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isOnboarding) {
                        ChatActivity.openForGroup(holder.mView.getContext(), exploreGroupData.getFirebaseGroupId(), false, null);
                    }
                }
            });
        } else {
            holder.joinTv.setOnClickListener(null);
            holder.joinTv.setClickable(false);
        }

        if (exploreGroupData.getMemberCount() == 0) {
            holder.memberCountTv.setVisibility(View.GONE);
        } else if (exploreGroupData.getMemberCount() < 10) {
            holder.memberCountTv.setVisibility(View.VISIBLE);
            holder.memberCountTv.setText("<10");
        } else {
            holder.memberCountTv.setVisibility(View.VISIBLE);
            holder.memberCountTv.setText(String.valueOf(exploreGroupData.getMemberCount()));
        }

        setLabel(holder, exploreGroupData);

        initCardClickListener(holder, exploreGroupData);
    }

    private void setLabel(ViewHolder holder, ExploreGroupData exploreGroupData) {
        holder.labelTv.setVisibility(View.GONE);

        if (exploreGroupData.getPriority() > 0 && exploreGroupData.getMemberCount() > 0) {
            //added check for member count to ensure the priority label isnt applied before the firebase values are fetched
            // otherwise it looks janky & buggy
            holder.labelTv.setVisibility(View.VISIBLE);
            holder.labelTv.setText("Suggested");
            ViewCompat.setBackgroundTintList(holder.labelTv, ColorStateList.valueOf(ContextCompat.getColor(LubbleApp.getAppContext(), R.color.md_green_50)));
            holder.labelTv.setTextColor(ContextCompat.getColor(LubbleApp.getAppContext(), R.color.md_blue_grey_900));
            holder.labelTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_stars_green_14dp, 0, 0, 0);
        }
        if (exploreGroupData.getMemberCount() > lubbleMemberCount / 2.5) {
            holder.labelTv.setVisibility(View.VISIBLE);
            holder.labelTv.setText("Popular");
            ViewCompat.setBackgroundTintList(holder.labelTv, ColorStateList.valueOf(ContextCompat.getColor(LubbleApp.getAppContext(), R.color.md_red_50)));
            holder.labelTv.setTextColor(ContextCompat.getColor(LubbleApp.getAppContext(), R.color.md_brown_900));
            holder.labelTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_whatshot_14, 0, 0, 0);
        }
        if (System.currentTimeMillis() - exploreGroupData.getLastMessageTimestamp() < TimeUnit.HOURS.toMillis(1)) {
            holder.labelTv.setVisibility(View.VISIBLE);
            holder.labelTv.setText("Active");
            ViewCompat.setBackgroundTintList(holder.labelTv, ColorStateList.valueOf(ContextCompat.getColor(LubbleApp.getAppContext(), R.color.md_blue_50)));
            holder.labelTv.setTextColor(ContextCompat.getColor(LubbleApp.getAppContext(), R.color.md_blue_grey_900));
            holder.labelTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_live_active_14, 0, 0, 0);
        }
    }

    private void initCardClickListener(final ViewHolder holder, final ExploreGroupData exploreGroupData) {
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    if (isOnboarding) {
                        if (holder.selectedContainer.getVisibility() == View.GONE) {
                            if (selectedMap.size() < 10) {
                                mListener.onListFragmentInteraction(holder.groupItem, true);
                                UiUtils.animateFadeShow(holder.mView.getContext(), holder.selectedContainer);
                                holder.joinTv.setText("REMOVE");
                                holder.joinTv.setTextColor(ContextCompat.getColor(holder.mView.getContext(), R.color.red));
                                selectedMap.put(holder.getAdapterPosition(), true);
                            } else {
                                Toast.makeText(v.getContext(), "You can join more groups later", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            mListener.onListFragmentInteraction(holder.groupItem, false);
                            UiUtils.animateFadeHide(holder.mView.getContext(), holder.selectedContainer);
                            holder.joinTv.setText("SELECT");
                            holder.joinTv.setTextColor(ContextCompat.getColor(holder.mView.getContext(), R.color.colorAccent));
                            selectedMap.remove(holder.getAdapterPosition());
                        }
                    } else {
                        // for explore bottom tab
                        ChatActivity.openForGroup(holder.mView.getContext(), exploreGroupData.getFirebaseGroupId(), false, null);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final ImageView imageView;
        final RelativeLayout selectedContainer;
        final EmojiTextView titleTv;
        final TextView joinTv, memberCountTv, labelTv;
        final ProgressBar joinProgressbar;
        public ExploreGroupData groupItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            imageView = view.findViewById(R.id.iv_wheretonight_pic);
            selectedContainer = view.findViewById(R.id.container_selected);
            titleTv = view.findViewById(R.id.tv_group_title);
            joinTv = view.findViewById(R.id.tv_join);
            memberCountTv = view.findViewById(R.id.tv_member_count);
            labelTv = view.findViewById(R.id.tv_label);
            joinProgressbar = view.findViewById(R.id.progressbar_join);
        }

    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(ExploreGroupData item, boolean isAdded);
    }

    void setLubbleMemberCount(int count) {
        lubbleMemberCount = count;
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return explorefilter;
    }

    private Filter explorefilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<ExploreGroupData> filteredList = new ArrayList<>();
            if(constraint == null || constraint.length()==0)
                filteredList.addAll(mValues_copy);
            else{
                String filterString = constraint.toString().toLowerCase().trim();
                for( ExploreGroupData item: mValues_copy){
                    if(item.getTitle().toLowerCase().contains(filterString)){
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList ;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mValues.clear();
            mValues.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}
