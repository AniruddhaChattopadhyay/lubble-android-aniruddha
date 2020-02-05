package in.lubble.app.events;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.models.EventData;
import in.lubble.app.utils.RoundedCornersTransformation;
import in.lubble.app.utils.StringUtils;

import static in.lubble.app.utils.DateTimeUtils.EVENT_DATE_TIME;
import static in.lubble.app.utils.DateTimeUtils.getTimeFromLong;
import static in.lubble.app.utils.UiUtils.dpToPx;

public class EventsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_EVENT = 121;
    private static final int TYPE_DIV = 365;

    private final List<EventData> eventDataList;
    private Context context;
    private static int POS_DIV = 0;

    public EventsAdapter(Context context) {
        eventDataList = new ArrayList<>();
        POS_DIV = 0;
        eventDataList.add(POS_DIV, null);
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        if (eventDataList.get(position) != null) {
            return TYPE_EVENT;
        } else {
            return TYPE_DIV;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_EVENT) {
            return new EventViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_event, parent, false));
        } else {
            return new DividerViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_past_events_header, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final EventData eventData = eventDataList.get(position);
        if (eventData != null && holder instanceof EventViewHolder) {
            final EventViewHolder viewHolder = (EventViewHolder) holder;

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

            if (position < POS_DIV) {
                viewHolder.card.setAlpha(1f);
            } else {
                viewHolder.card.setAlpha(0.5f);
            }

        } else {
            final DividerViewHolder viewHolder = (DividerViewHolder) holder;
            viewHolder.titleTv.setText(R.string.past_events);
        }
    }

    void addEvent(EventData eventData) {
        if (!eventDataList.contains(eventData)) {
            long timestampToCompare = eventData.getEndTimestamp() == 0L ? eventData.getStartTimestamp() : eventData.getEndTimestamp();
            if (System.currentTimeMillis() < timestampToCompare) {
                // upcoming event
                insertWithSort(eventData, POS_DIV);
                POS_DIV++;
                // persist read event id
                final LubbleSharedPrefs sharedPrefs = LubbleSharedPrefs.getInstance();
                final Set<String> readEventSet = sharedPrefs.getEventSet();
                readEventSet.add(eventData.getId());
                sharedPrefs.setEventSet(readEventSet);
            } else {
                // past event
                eventDataList.add(POS_DIV + 1, eventData);
            }
            notifyDataSetChanged();
        }
    }

    private void insertWithSort(EventData eventData, int pos) {
        int prevPos = pos - 1 < 0 ? 0 : pos - 1;
        if (prevPos == eventDataList.size()) {
            prevPos = eventDataList.size() - 1;
        }
        final EventData prevEventData = eventDataList.get(prevPos);
        if (prevEventData != null) {
            if (eventData.getStartTimestamp() >= prevEventData.getStartTimestamp() || pos == 0) {
                eventDataList.add(pos, eventData);
            } else {
                insertWithSort(eventData, --pos);
            }
        } else {
            eventDataList.add(pos, eventData);
        }
    }

    public void clear() {
        eventDataList.clear();
        POS_DIV = 0;
        eventDataList.add(POS_DIV, null);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return eventDataList.size();
    }




    class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final View mView;
        final CardView card;
        final ImageView iconIv;
        final TextView organizerTv;
        final TextView titleTv;
        final TextView timeTv;

        EventViewHolder(View view) {
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
            EventInfoActivity.open(context, eventDataList.get(getAdapterPosition()).getEvent_id());
        }
    }

    class DividerViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView titleTv;

        DividerViewHolder(View view) {
            super(view);
            mView = view;
            titleTv = view.findViewById(R.id.tv_title);
        }
    }

}
