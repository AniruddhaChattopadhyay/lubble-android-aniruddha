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
import in.lubble.app.models.EventData;
import in.lubble.app.utils.RoundedCornersTransformation;

import static in.lubble.app.utils.DateTimeUtils.EVENT_DATE_TIME;
import static in.lubble.app.utils.DateTimeUtils.getTimeFromLong;
import static in.lubble.app.utils.UiUtils.dpToPx;

public class EventsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<EventData> eventDataList;
    private Context context;

    public EventsAdapter(Context context) {
        eventDataList = new ArrayList<>();
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SummerCampViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final EventData eventData = eventDataList.get(position);
        final SummerCampViewHolder viewHolder = (SummerCampViewHolder) holder;

        GlideApp.with(viewHolder.mView)
                .load(eventData.getProfilePic())
                .placeholder(R.drawable.ic_wb_sunny_black_24dp)
                .error(R.drawable.ic_wb_sunny_black_24dp)
                .transform(new RoundedCornersTransformation(dpToPx(8), 0))
                .into(viewHolder.iconIv);

        viewHolder.organizerTv.setText(eventData.getOrganizer());
        viewHolder.titleTv.setText(eventData.getTitle());

        viewHolder.timeTv.setText(getTimeFromLong(eventData.getStartTimestamp(), EVENT_DATE_TIME));
    }

    void addEvent(EventData eventData) {
        eventDataList.add(eventData);
        notifyDataSetChanged();
    }

    public void clear() {
        eventDataList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return eventDataList.size();
    }

    class SummerCampViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final View mView;
        final CardView card;
        final ImageView iconIv;
        final TextView organizerTv;
        final TextView titleTv;
        final TextView timeTv;

        SummerCampViewHolder(View view) {
            super(view);
            mView = view;
            card = view.findViewById(R.id.card_event);
            iconIv = view.findViewById(R.id.iv_event);
            organizerTv = view.findViewById(R.id.tv_organizer_name);
            titleTv = view.findViewById(R.id.tv_event_title);
            timeTv = view.findViewById(R.id.tv_event_time);
            mView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //SummerCampInfoActivity.open(context, eventDataList.get(getAdapterPosition()).getId());
        }
    }

}
