package in.lubble.app.events.new_event;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.GroupData;
import in.lubble.app.utils.mapUtils.SphericalUtil;

import static in.lubble.app.Constants.SVR_LATI;
import static in.lubble.app.Constants.SVR_LONGI;
import static in.lubble.app.firebase.RealtimeDbHelper.getLubbleGroupsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserGroupsRef;
import static in.lubble.app.utils.DateTimeUtils.APP_NORMAL_DATE_YEAR;
import static in.lubble.app.utils.DateTimeUtils.APP_SHORT_TIME;

public class NewEventActivity extends AppCompatActivity {

    private static final int PLACE_PICKER_REQUEST = 828;

    private SupportMapFragment mapFragment;
    private LatLng defaultLatLng;
    private TextInputLayout dateTil;
    private TextInputLayout startTimeTil;
    private TextInputLayout endTimeTil;
    private TextInputLayout addressTil;
    private RadioGroup radioGroup;
    private RadioButton newGroupRadioBtn;
    private CompoundButton oldGroupRadioBtn;
    private TextView notAdminHintTv;
    private Spinner adminGroupsSpinner;
    private EventGroupSpinnerAdapter spinnerAdapter;
    private Calendar myCalendar;
    private ChildEventListener userGroupRef;
    private HashMap<Query, ValueEventListener> map = new HashMap<>();

    public static void open(Context context) {
        context.startActivity(new Intent(context, NewEventActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_camp);

        dateTil = findViewById(R.id.til_event_date);
        startTimeTil = findViewById(R.id.til_event_start_time);
        endTimeTil = findViewById(R.id.til_event_end_time);
        addressTil = findViewById(R.id.till_event_address);
        radioGroup = findViewById(R.id.radioGroup_group_link);
        newGroupRadioBtn = findViewById(R.id.radiobtn_new_group);
        oldGroupRadioBtn = findViewById(R.id.radiobtn_old_group);
        notAdminHintTv = findViewById(R.id.tv_not_admin_hint);
        adminGroupsSpinner = findViewById(R.id.spinner_admin_groups);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        myCalendar = Calendar.getInstance();

        defaultLatLng = new LatLng(SVR_LATI, SVR_LONGI);
        loadMapAt(defaultLatLng);

        dateTil.setOnClickListener(getOnDateClickListener());
        dateTil.getEditText().setOnClickListener(getOnDateClickListener());
        dateTil.getEditText().setCursorVisible(false);
        dateTil.getEditText().setKeyListener(null);

        startTimeTil.setOnClickListener(getOnStartTimeClickListener());
        startTimeTil.getEditText().setOnClickListener(getOnStartTimeClickListener());
        startTimeTil.getEditText().setCursorVisible(false);
        startTimeTil.getEditText().setKeyListener(null);

        endTimeTil.setOnClickListener(getOnEndTimeClickListener());
        endTimeTil.getEditText().setOnClickListener(getOnEndTimeClickListener());
        endTimeTil.getEditText().setCursorVisible(false);
        endTimeTil.getEditText().setKeyListener(null);

        spinnerAdapter = new EventGroupSpinnerAdapter(this, R.layout.item_privacy_spinner, new ArrayList<GroupData>(), GlideApp.with(this));
        adminGroupsSpinner.setAdapter(spinnerAdapter);

        newGroupRadioBtn.setChecked(true);
        oldGroupRadioBtn.setEnabled(false);
        oldGroupRadioBtn.setTextColor(ContextCompat.getColor(this, R.color.dark_gray));
        notAdminHintTv.setVisibility(View.VISIBLE);

        oldGroupRadioBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                adminGroupsSpinner.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchUserGroups();
    }

    private void fetchUserGroups() {
        spinnerAdapter.clear();
        userGroupRef = getUserGroupsRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                fetchGroupInfo(dataSnapshot.getKey());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private static final String TAG = "NewEventActivity";

    private void fetchGroupInfo(String groupId) {
        final ValueEventListener joinedGroupRef = RealtimeDbHelper.getLubbleGroupsRef().child(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final GroupData groupData = dataSnapshot.getValue(GroupData.class);
                final Set<Map.Entry<String, Object>> entries = groupData.getMembers().entrySet();
                for (Map.Entry<String, Object> entry : entries) {
                    final HashMap map = (HashMap) entry.getValue();
                    if (entry.getKey().equalsIgnoreCase(FirebaseAuth.getInstance().getUid())
                            && map.get("admin") == Boolean.TRUE) {
                        spinnerAdapter.add(groupData);
                        // enable 2nd radio btn
                        oldGroupRadioBtn.setEnabled(true);
                        oldGroupRadioBtn.setTextColor(ContextCompat.getColor(NewEventActivity.this, R.color.black));
                        notAdminHintTv.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        map.put(getLubbleGroupsRef().child(groupId), joinedGroupRef);
    }

    @NonNull
    private View.OnClickListener getOnDateClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(NewEventActivity.this, R.style.datepicker, getDateListener(), myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        };
    }

    @NonNull
    private View.OnClickListener getOnStartTimeClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(NewEventActivity.this, R.style.timeTheme, getStartTimeSetListener(), myCalendar
                        .get(Calendar.HOUR_OF_DAY), myCalendar.get(Calendar.MINUTE), false).show();
            }
        };
    }

    @NonNull
    private View.OnClickListener getOnEndTimeClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(NewEventActivity.this, R.style.timeTheme, getEndTimeSetListener(), myCalendar
                        .get(Calendar.HOUR_OF_DAY), myCalendar.get(Calendar.MINUTE), false).show();
            }
        };
    }

    private TimePickerDialog.OnTimeSetListener getStartTimeSetListener() {
        return new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Calendar timeCal = Calendar.getInstance();
                timeCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                timeCal.set(Calendar.MINUTE, minute);

                String myFormat = APP_SHORT_TIME;
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ENGLISH);

                startTimeTil.getEditText().setText(sdf.format(timeCal.getTime()));
            }
        };
    }

    private TimePickerDialog.OnTimeSetListener getEndTimeSetListener() {
        return new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Calendar timeCal = Calendar.getInstance();
                timeCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                timeCal.set(Calendar.MINUTE, minute);

                String myFormat = APP_SHORT_TIME;
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ENGLISH);

                endTimeTil.getEditText().setText(sdf.format(timeCal.getTime()));
            }
        };
    }

    private DatePickerDialog.OnDateSetListener getDateListener() {
        return new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String myFormat = APP_NORMAL_DATE_YEAR;
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ENGLISH);

                dateTil.getEditText().setText(sdf.format(myCalendar.getTime()));
            }

        };
    }

    private void loadMapAt(final LatLng defaultLatLng) {
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                // Add Marker
                googleMap.addMarker(new MarkerOptions().position(defaultLatLng));
                // Center map on the marker
                CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(defaultLatLng, 16.0f);
                googleMap.animateCamera(yourLocation);
                googleMap.getUiSettings().setMapToolbarEnabled(false);
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                        NewEventActivity.this, R.raw.style_json));
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        launchLocationPicker(latLng);
                    }
                });
            }
        });
    }

    private void launchLocationPicker(LatLng latLng) {
        if (latLng == null) {
            latLng = defaultLatLng;
        }
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        LatLng northEast = SphericalUtil.computeOffset(latLng, 800, 45); // Shift 800m to the north-east
        LatLng southWest = SphericalUtil.computeOffset(latLng, 800, 225); // Shift 800m to the north-east
        builder.setLatLngBounds(new LatLngBounds(southWest, northEast));
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                if (place != null) {
                    loadMapAt(place.getLatLng());
                    addressTil.getEditText().setText(place.getAddress());
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getUserGroupsRef().removeEventListener(userGroupRef);
        for (Query query : map.keySet()) {
            query.removeEventListener(map.get(query));
        }
    }
}
