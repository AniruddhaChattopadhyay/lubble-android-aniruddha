package in.lubble.app.events;

import static in.lubble.app.Constants.MEDIA_TYPE;
import static in.lubble.app.chat.ChatActivity.EXTRA_GROUP_ID;
import static in.lubble.app.chat.ChatActivity.EXTRA_IS_JOINING;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import in.lubble.app.BaseActivity;
import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.chat.ChatActivity;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.EventData;
import in.lubble.app.models.GroupInfoData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventGroupJoinedActivity extends BaseActivity {

    private static final String TAG = "EventGroupJoinedActiv";
    private static final String STATUS = "STATUS";
    private static final String EVENT_ID = "EVENT_ID";

    private RelativeLayout rootLayout;
    private ImageView cancelIcon;
    private TextView titleTv;
    private TextView finalGuestCountTv;
    private Button confirmGuestsBtn;
    private ElegantNumberButton guestCounter;
    private LinearLayout ticketShareLayout;
    private Endpoints endpoints;
    private int status = 1;
    private String eventId;
    private ValueEventListener listener;

    public static void open(Context context, int status, String eventId) {
        final Intent intent = new Intent(context, EventGroupJoinedActivity.class);
        intent.putExtra(STATUS, status);
        intent.putExtra(EVENT_ID, eventId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_group_joined);

        rootLayout = findViewById(R.id.root);
        cancelIcon = findViewById(R.id.iv_cancel);
        titleTv = findViewById(R.id.tv_title);
        finalGuestCountTv = findViewById(R.id.tv_final_guest_count);
        guestCounter = findViewById(R.id.guest_counter);
        confirmGuestsBtn = findViewById(R.id.btn_confirm_guests);
        ticketShareLayout = findViewById(R.id.ticket_share_layout);

        status = getIntent().getIntExtra(STATUS, 1);
        eventId = getIntent().getStringExtra(EVENT_ID);

        changeLayoutFor(status);
        Analytics.triggerScreenEvent(this, getClass());

        endpoints = ServiceGenerator.createService(Endpoints.class);
        //endpoints = retrofit.create(Endpoints.class);
        guestCounter.setOnClickListener((ElegantNumberButton.OnClickListener) view -> {
            if (confirmGuestsBtn.getVisibility() != View.VISIBLE) {
                confirmGuestsBtn.setVisibility(View.VISIBLE);
            }
        });

        confirmGuestsBtn.setOnClickListener(v -> {
            final String guestCount = guestCounter.getNumber();

            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("event_id", eventId);
                jsonObject.put("uid", FirebaseAuth.getInstance().getUid());
                jsonObject.put("guests", guestCount);
                jsonObject.put("admin", "false");
                RequestBody body = RequestBody.create(MEDIA_TYPE, jsonObject.toString());

                //Call<List<EmptyPostResponse>> call = endpoints.uploadattendee("ayush_django_backend_token","ayush_django_backend",body);
                Call<Void> call = endpoints.uploadattendee(body);
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful() && !isFinishing()) {
                            guestCounter.setVisibility(View.GONE);
                            confirmGuestsBtn.setVisibility(View.GONE);
                            finalGuestCountTv.setVisibility(View.VISIBLE);
                            finalGuestCountTv.setText(" + " + guestCount + " guests");
                        } else if (!isFinishing()) {
                            Toast.makeText(EventGroupJoinedActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (!isFinishing()) {
                            Toast.makeText(EventGroupJoinedActivity.this, "Please check your internet connection & try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        });

        ticketShareLayout.setVisibility(View.GONE);

        cancelIcon.setOnClickListener(v -> finish());
    }


    private void changeLayoutFor(int status) {
        if (status == EventData.GOING) {
            rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_green));
            titleTv.setText(R.string.event_going_confirm_title);
        } else if (status == EventData.MAYBE) {
            rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.dk_colorAccent));
            titleTv.setText(R.string.event_maybe_confirm_title);
        }
    }
}
