package in.lubble.app.events;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import in.lubble.app.GlideApp;
import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.events.new_event.NewEventActivity;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.EventData;

import java.util.ArrayList;
import java.util.Collections;

import static in.lubble.app.utils.DateTimeUtils.*;

public class EventPickerActiv extends AppCompatActivity {

    private static final String TAG = "EventPickerActiv";

    private ProgressBar progressbar;
    private LinearLayout emptyEventsContainer;
    private RecyclerView recyclerView;
    private Query query;
    private ValueEventListener valueEventListener;

    public static Intent getIntent(Context context) {
        return new Intent(context, EventPickerActiv.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_picker);

        progressbar = findViewById(R.id.progressbar_event_picker);
        emptyEventsContainer = findViewById(R.id.container_empty_events);
        recyclerView = findViewById(R.id.rv_event_picker);
        ImageView closeIv = findViewById(R.id.iv_event_picker_close);
        Button newEventBtn = findViewById(R.id.btn_new_event);
        newEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewEventActivity.open(EventPickerActiv.this);
            }
        });

        progressbar.setVisibility(View.VISIBLE);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration itemDecor = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(itemDecor);

        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        recyclerView.setAdapter(new EventPickerAdapter(new ArrayList<EventData>(), GlideApp.with(EventPickerActiv.this)));

        query = RealtimeDbHelper.getEventsRef().orderByChild("startTimestamp");
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final ArrayList<EventData> eventDataList = new ArrayList<>();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    final EventData eventData = child.getValue(EventData.class);
                    if (eventData != null && eventData.getStartTimestamp() > System.currentTimeMillis()) {
                        eventData.setId(child.getKey());
                        eventDataList.add(eventData);
                    }
                }
                progressbar.setVisibility(View.GONE);
                if (eventDataList.isEmpty()) {
                    emptyEventsContainer.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    emptyEventsContainer.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    Collections.reverse(eventDataList);
                    recyclerView.setAdapter(new EventPickerAdapter(eventDataList, GlideApp.with(EventPickerActiv.this)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        query.addValueEventListener(valueEventListener);
    }

    private class EventPickerAdapter extends RecyclerView.Adapter<EventPickerAdapter.ViewHolder> {

        private ArrayList<EventData> eventList;
        private GlideRequests glideApp;

        EventPickerAdapter(ArrayList<EventData> eventList, GlideRequests glideApp) {
            this.eventList = eventList;
            this.glideApp = glideApp;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final EventData eventData = eventList.get(position);
            holder.eventMonthTv.setText(getTimeFromLong(eventData.getStartTimestamp(), SHORT_MONTH));
            holder.eventDateTv.setText(getTimeFromLong(eventData.getStartTimestamp(), DATE));
            holder.eventNameTv.setText(eventData.getTitle());
            holder.eventDescTv.setText(eventData.getDesc());
        }

        @Override
        public int getItemCount() {
            return eventList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            final TextView eventMonthTv;
            final TextView eventDateTv;
            final TextView eventNameTv;
            final TextView eventDescTv;

            ViewHolder(LayoutInflater inflater, ViewGroup parent) {
                super(inflater.inflate(R.layout.item_event_picker, parent, false));
                eventMonthTv = itemView.findViewById(R.id.tv_event_month);
                eventDateTv = itemView.findViewById(R.id.tv_event_date);
                eventNameTv = itemView.findViewById(R.id.tv_event_name);
                eventDescTv = itemView.findViewById(R.id.tv_event_desc);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Intent intent = new Intent();
                        intent.putExtra("event_id", eventList.get(getAdapterPosition()).getId());
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (valueEventListener != null) {
            query.removeEventListener(valueEventListener);
        }
    }
}
