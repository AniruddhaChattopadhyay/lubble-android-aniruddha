package in.lubble.app.summer_camp;

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
import in.lubble.app.utils.RoundedCornersTransformation;

import static in.lubble.app.utils.UiUtils.dpToPx;

public class SummerCampAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<GroupData> groupDataList;
    private Context context;

    public SummerCampAdapter(Context context) {
        groupDataList = new ArrayList<>();
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SummerCampViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_summer_camp, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final GroupData groupData = groupDataList.get(position);
        final SummerCampViewHolder viewHolder = (SummerCampViewHolder) holder;

        GlideApp.with(viewHolder.mView)
                .load(groupData.getProfilePic())
                .placeholder(R.drawable.ic_wb_sunny_black_24dp)
                .error(R.drawable.ic_wb_sunny_black_24dp)
                .transform(new RoundedCornersTransformation(dpToPx(16), 0))
                .into(viewHolder.iconIv);

        viewHolder.titleTv.setText(groupData.getTitle());
        viewHolder.descTv.setText(groupData.getDescription());
        viewHolder.card.setCardBackgroundColor(getVariableColor(position));
    }

    void addGroup(GroupData groupData) {
        groupDataList.add(groupData);
        notifyDataSetChanged();
    }

    private int getVariableColor(int position) {
        int selector = position % 5;
        switch (selector) {
            case 0:
                return context.getResources().getColor(R.color.dk_cyan);
            case 1:
                return context.getResources().getColor(R.color.dk_red);
            case 2:
                return context.getResources().getColor(R.color.fb_color);
            case 3:
                return context.getResources().getColor(R.color.mute_purple);
            case 4:
                return context.getResources().getColor(R.color.dk_green);
            default:
                return context.getResources().getColor(R.color.mute_orange);
        }
    }

    public void clear() {
        groupDataList.clear();
        notifyDataSetChanged();
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
            /*final long phoneNo = groupDataList.get(getAdapterPosition()).getPhone();
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + String.valueOf(phoneNo)));
            context.startActivity(intent);*/
        }
    }

}
