package in.lubble.app.events;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.List;

import in.lubble.app.BaseActivity;
import in.lubble.app.GlideApp;
import in.lubble.app.GlideRequests;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.events.new_event.NewEventActivity;
import in.lubble.app.models.EventData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static in.lubble.app.utils.DateTimeUtils.DATE;
import static in.lubble.app.utils.DateTimeUtils.SHORT_MONTH;
import static in.lubble.app.utils.DateTimeUtils.getTimeFromLong;

public class EventPickerActiv extends BaseActivity {

    private static final String TAG = "EventPickerActiv";

    private ProgressBar progressbar;
    private LinearLayout emptyEventsContainer;
    private RecyclerView recyclerView;
    private Query query;
    private ValueEventListener valueEventListener;
    private Endpoints endpoints;

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
        endpoints = ServiceGenerator.createService(Endpoints.class);
        //endpoints = retrofit.create(Endpoints.class);
        String lubble_id = LubbleSharedPrefs.getInstance().getLubbleId();
        //Call<List<EventData>> call = endpoints.getEvents("ayush_django_backend_token","ayush_django_backend", LubbleSharedPrefs.getInstance().getLubbleId());
        Call<List<EventData>> call = endpoints.getEvents(LubbleSharedPrefs.getInstance().getLubbleId());
        call.enqueue(new Callback<List<EventData>>() {
            @Override
            public void onResponse(Call<List<EventData>> call, Response<List<EventData>> response) {
                if (!response.isSuccessful()) {
                    Log.e(TAG, response.code() + "");
                    Toast.makeText(getApplicationContext(), "Failed to load events! please try again.", Toast.LENGTH_SHORT).show();
                    EventPickerActiv.this.finish();
                    return;
                }
                final ArrayList<EventData> eventDataList = new ArrayList<>();
                List<EventData> data = response.body();
                for (EventData eventData : data) {
                    if (eventData != null && eventData.getStartTimestamp() > System.currentTimeMillis()) {
                        eventData.setId(eventData.getEvent_id());
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
                    recyclerView.setAdapter(new EventPickerAdapter(eventDataList, GlideApp.with(EventPickerActiv.this)));
                }
            }

            @Override
            public void onFailure(Call<List<EventData>> call, Throwable t) {
                Log.e(TAG, "failed to get response from django");
                Toast.makeText(getApplicationContext(), "Failed to load events! please try again.", Toast.LENGTH_SHORT).show();
                EventPickerActiv.this.finish();
            }
        });


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
            holder.eventDescTv.setText(Jsoup.parse(eventData.getDesc()).text());
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
                        Log.d(TAG, "inside on click" + eventList.get(getAdapterPosition()).getId());
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
