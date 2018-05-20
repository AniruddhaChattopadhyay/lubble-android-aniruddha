package in.lubble.app.events;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.models.GroupData;
import in.lubble.app.summer_camp.SummerCampInfoActivity;
import in.lubble.app.utils.RoundedCornersTransformation;

import static in.lubble.app.utils.UiUtils.dpToPx;

public class EventsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<GroupData> groupDataList;
    private Context context;

    public EventsAdapter(Context context) {
        groupDataList = new ArrayList<>();
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SummerCampViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final GroupData groupData = groupDataList.get(position);
        final SummerCampViewHolder viewHolder = (SummerCampViewHolder) holder;
        if (groupData != null) {

            GlideApp.with(viewHolder.mView)
                    .load(groupData.getProfilePic())
                    .placeholder(R.drawable.ic_wb_sunny_black_24dp)
                    .error(R.drawable.ic_wb_sunny_black_24dp)
                    .transform(new RoundedCornersTransformation(dpToPx(8), 0))
                    .into(viewHolder.iconIv);

            viewHolder.titleTv.setText(groupData.getTitle());
            viewHolder.descTv.setText(groupData.getDescription());
        } else {
            viewHolder.iconIv.setImageResource(R.drawable.ic_add_circle_black_24dp);
            viewHolder.titleTv.setText("Add your class");
            viewHolder.descTv.setText("Submit your Summer Camp class to have it appear here");
        }
    }

    void addGroup(GroupData groupData) {
        groupDataList.add(groupData);
        notifyDataSetChanged();
    }

    public void clear() {
        groupDataList.clear();
        notifyDataSetChanged();
        //addNewGroupCard();
    }

    @Override
    public int getItemCount() {
        return groupDataList.size();
    }

    class SummerCampViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final View mView;
        final CardView card;
        final ImageView iconIv;
        final TextView titleTv;
        final TextView descTv;

        SummerCampViewHolder(View view) {
            super(view);
            mView = view;
            card = view.findViewById(R.id.card_summer_camp);
            iconIv = view.findViewById(R.id.iv_class);
            titleTv = view.findViewById(R.id.tv_camp_title);
            descTv = view.findViewById(R.id.tv_camp_desc);
            mView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            SummerCampInfoActivity.open(context, groupDataList.get(getAdapterPosition()).getId());
        }
    }

}
