package in.lubble.app.explore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.core.content.ContextCompat;
import androidx.emoji.widget.EmojiTextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.chat.ChatActivity;
import in.lubble.app.utils.RoundedCornersTransformation;
import in.lubble.app.utils.UiUtils;

import java.util.List;

import static in.lubble.app.firebase.RealtimeDbHelper.getCreateOrJoinGroupRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserGroupsRef;
import static in.lubble.app.utils.RoundedCornersTransformation.CornerType.TOP;

public class ExploreGroupAdapter extends RecyclerView.Adapter<ExploreGroupAdapter.ViewHolder> {

    private List<ExploreGroupData> mValues;
    private final OnListFragmentInteractionListener mListener;
    private final GlideRequests glide;
    private final boolean isOnboarding;

    public ExploreGroupAdapter(List<ExploreGroupData> items, OnListFragmentInteractionListener listener, GlideRequests glide, boolean isOnboarding) {
        mValues = items;
        mListener = listener;
        this.glide = glide;
        this.isOnboarding = isOnboarding;
    }

    public void updateList(List<ExploreGroupData> items){
        this.mValues = items;
        notifyDataSetChanged();
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
        } else {
            holder.joinTv.setText("JOIN");
            holder.joinTv.setTextColor(ContextCompat.getColor(holder.mView.getContext(), R.color.colorAccent));
        }

        if (!isOnboarding) {
            holder.joinTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isOnboarding) {
                        String groupId = exploreGroupData.getFirebaseGroupId();
                        holder.joinTv.setVisibility(View.GONE);
                        holder.joinProgressbar.setVisibility(View.VISIBLE);
                        getCreateOrJoinGroupRef().child(groupId).setValue(true);
                        holder.mView.setOnClickListener(null);

                        getUserGroupsRef().child(groupId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                holder.joinTv.setVisibility(View.VISIBLE);
                                holder.joinProgressbar.setVisibility(View.GONE);
                                holder.joinTv.setText("Member");
                                holder.joinTv.setOnClickListener(null);
                                initCardClickListener(holder, exploreGroupData);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                holder.joinProgressbar.setVisibility(View.GONE);
                                Toast.makeText(holder.mView.getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                                initCardClickListener(holder, exploreGroupData);
                            }
                        });
                    }
                }
            });
        } else {
            holder.joinTv.setOnClickListener(null);
            holder.joinTv.setClickable(false);
        }

        initCardClickListener(holder, exploreGroupData);
    }

    private void initCardClickListener(final ViewHolder holder, final ExploreGroupData exploreGroupData) {
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    if (isOnboarding) {
                        if (holder.selectedContainer.getVisibility() == View.GONE) {
                            mListener.onListFragmentInteraction(holder.groupItem, true);
                            UiUtils.animateFadeShow(holder.mView.getContext(), holder.selectedContainer);
                            holder.joinTv.setText("REMOVE");
                            holder.joinTv.setTextColor(ContextCompat.getColor(holder.mView.getContext(), R.color.red));
                        } else {
                            mListener.onListFragmentInteraction(holder.groupItem, false);
                            UiUtils.animateFadeHide(holder.mView.getContext(), holder.selectedContainer);
                            holder.joinTv.setText("SELECT");
                            holder.joinTv.setTextColor(ContextCompat.getColor(holder.mView.getContext(), R.color.colorAccent));
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
        final TextView joinTv;
        final ProgressBar joinProgressbar;
        public ExploreGroupData groupItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            imageView = view.findViewById(R.id.iv_group_pic);
            selectedContainer = view.findViewById(R.id.container_selected);
            titleTv = view.findViewById(R.id.tv_group_title);
            joinTv = view.findViewById(R.id.tv_join);
            joinProgressbar = view.findViewById(R.id.progressbar_join);
        }

    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(ExploreGroupData item, boolean isAdded);
    }
}
