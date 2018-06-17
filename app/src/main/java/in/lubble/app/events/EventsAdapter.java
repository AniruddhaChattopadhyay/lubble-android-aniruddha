package in.lubble.app.events;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.models.EventData;
import in.lubble.app.utils.RoundedCornersTransformation;
import in.lubble.app.utils.StringUtils;

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
                .placeholder(R.drawable.ic_star_party)
                .error(R.drawable.ic_star_party)
                .transform(new RoundedCornersTransformation(dpToPx(8), 0))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        viewHolder.iconIv.setBackgroundColor(ContextCompat.getColor(context, R.color.dark_teal));
                        viewHolder.iconIv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        viewHolder.iconIv.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        viewHolder.iconIv.setBackgroundColor(ContextCompat.getColor(context, R.color.black));
                        viewHolder.iconIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        viewHolder.iconIv.setPadding(0, 0, 0, 0);
                        return false;
                    }
                })
                .into(viewHolder.iconIv);

        if (StringUtils.isValidString(eventData.getOrganizer())) {
            viewHolder.organizerTv.setVisibility(View.VISIBLE);
            viewHolder.organizerTv.setText(eventData.getOrganizer());
        } else {
            viewHolder.organizerTv.setVisibility(View.GONE);
        }
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
            EventInfoActivity.open(context, eventDataList.get(getAdapterPosition()).getId());
        }
    }

}
