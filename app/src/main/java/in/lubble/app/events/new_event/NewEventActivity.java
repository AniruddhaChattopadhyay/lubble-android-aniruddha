package in.lubble.app.events.new_event;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.signature.ObjectKey;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.UploadFileService;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.EventData;
import in.lubble.app.models.GroupData;
import in.lubble.app.utils.mapUtils.SphericalUtil;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import static in.lubble.app.Constants.SVR_LATI;
import static in.lubble.app.Constants.SVR_LONGI;
import static in.lubble.app.firebase.RealtimeDbHelper.getEventsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getLubbleGroupsRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserGroupsRef;
import static in.lubble.app.models.EventData.GOING;
import static in.lubble.app.utils.DateTimeUtils.APP_NORMAL_DATE_YEAR;
import static in.lubble.app.utils.DateTimeUtils.APP_SHORT_TIME;
import static in.lubble.app.utils.FileUtils.createImageFile;
import static in.lubble.app.utils.FileUtils.getFileFromInputStreamUri;
import static in.lubble.app.utils.FileUtils.getPickImageIntent;
import static in.lubble.app.utils.FileUtils.showStoragePermRationale;
import static in.lubble.app.utils.StringUtils.isValidString;
import static in.lubble.app.utils.UserUtils.getLubbleId;

@RuntimePermissions
public class NewEventActivity extends AppCompatActivity {

    private static final String TAG = "NewEventActivity";
    private static final int PLACE_PICKER_REQUEST = 828;
    private static final int REQUEST_CODE_EVENT_PIC = 626;

    private String currentPhotoPath;
    private Uri picUri = null;

    private SupportMapFragment mapFragment;
    private LatLng defaultLatLng;
    private ScrollView parentScrollView;
    private ImageView headerImage;
    private TextInputLayout titleTil;
    private TextInputLayout descTil;
    private TextInputLayout organizerTil;
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
    private Button submitBtn;
    private Place place;

    public static void open(Context context) {
        context.startActivity(new Intent(context, NewEventActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        parentScrollView = findViewById(R.id.scrollview_parent);
        headerImage = findViewById(R.id.iv_event_image);
        titleTil = findViewById(R.id.til_event_name);
        descTil = findViewById(R.id.til_event_desc);
        organizerTil = findViewById(R.id.til_event_organizer);
        dateTil = findViewById(R.id.til_event_date);
        startTimeTil = findViewById(R.id.til_event_start_time);
        endTimeTil = findViewById(R.id.til_event_end_time);
        addressTil = findViewById(R.id.till_event_address);
        radioGroup = findViewById(R.id.radioGroup_group_link);
        newGroupRadioBtn = findViewById(R.id.radiobtn_new_group);
        oldGroupRadioBtn = findViewById(R.id.radiobtn_old_group);
        notAdminHintTv = findViewById(R.id.tv_not_admin_hint);
        adminGroupsSpinner = findViewById(R.id.spinner_admin_groups);
        submitBtn = findViewById(R.id.btn_submit);

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

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isValidationPassed()) {
                    return;
                }
                final EventData eventData = new EventData();
                eventData.setTitle(titleTil.getEditText().getText().toString().trim());
                eventData.setDesc(descTil.getEditText().getText().toString().trim());
                eventData.setOrganizer(organizerTil.getEditText().getText().toString().trim());

                final String dateStr = dateTil.getEditText().getText().toString();
                final String startTimeStr = startTimeTil.getEditText().getText().toString();
                final String endTimeStr = endTimeTil.getEditText().getText().toString();

                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat(APP_NORMAL_DATE_YEAR + " " + APP_SHORT_TIME, Locale.ENGLISH);
                try {
                    cal.setTime(sdf.parse(dateStr + " " + startTimeStr));
                    eventData.setStartTimestamp(cal.getTimeInMillis());

                    if (isValidString(endTimeStr)) {
                        cal.setTime(sdf.parse(dateStr + " " + endTimeStr));
                        eventData.setEndTimestamp(cal.getTimeInMillis());
                    }
                    eventData.setLati(place.getLatLng().latitude);
                    eventData.setLongi(place.getLatLng().longitude);
                    eventData.setAddress(addressTil.getEditText().getText().toString());
                    if (oldGroupRadioBtn.isChecked()) {
                        final GroupData selectedGroupData = (GroupData) adminGroupsSpinner.getSelectedItem();
                        eventData.setGid(selectedGroupData.getId());
                    } else {
                        eventData.setGid("");
                    }

                    final HashMap<String, Object> membersMap = new HashMap<>();
                    final HashMap<String, Object> memberInfoMap = new HashMap<>();
                    memberInfoMap.put("response", GOING);
                    memberInfoMap.put("timestamp", System.currentTimeMillis());
                    memberInfoMap.put("isAdmin", true);
                    membersMap.put(FirebaseAuth.getInstance().getUid(), memberInfoMap);
                    eventData.setMembers(membersMap);

                    final DatabaseReference pushRef = getEventsRef().push();
                    pushRef.setValue(eventData);

                    if (picUri != null) {
                        startService(new Intent(NewEventActivity.this, UploadFileService.class)
                                .putExtra(UploadFileService.EXTRA_FILE_NAME, "profile_pic_" + System.currentTimeMillis() + ".jpg")
                                .putExtra(UploadFileService.EXTRA_FILE_URI, picUri)
                                .putExtra(UploadFileService.EXTRA_UPLOAD_PATH, "lubbles/" + getLubbleId() + "/events/" + pushRef.getKey())
                                .setAction(UploadFileService.ACTION_UPLOAD));
                    }

                    Toast.makeText(NewEventActivity.this, R.string.event_published, Toast.LENGTH_SHORT).show();
                    finish();

                } catch (ParseException e) {
                    e.printStackTrace();
                    Toast.makeText(NewEventActivity.this, R.string.invalid_date_time, Toast.LENGTH_SHORT).show();
                    Crashlytics.logException(e);
                }
            }
        });

        headerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewEventActivityPermissionsDispatcher
                        .startPhotoPickerWithPermissionCheck(NewEventActivity.this, REQUEST_CODE_EVENT_PIC);
            }
        });

    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    public void startPhotoPicker(int REQUEST_CODE) {
        try {
            File cameraPic = createImageFile(this);
            currentPhotoPath = cameraPic.getAbsolutePath();
            Intent pickImageIntent = getPickImageIntent(this, cameraPic);
            startActivityForResult(pickImageIntent, REQUEST_CODE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isValidationPassed() {
        if (!isValidString(titleTil.getEditText().getText().toString())) {
            titleTil.setError(getString(R.string.event_name_error));
            parentScrollView.smoothScrollTo(0, 0);
            return false;
        } else {
            titleTil.setError(null);
        }
        if (!isValidString(descTil.getEditText().getText().toString())) {
            descTil.setError(getString(R.string.event_desc_error));
            parentScrollView.smoothScrollTo(0, 0);
            return false;
        } else {
            descTil.setError(null);
        }
        if (!isValidString(organizerTil.getEditText().getText().toString())) {
            organizerTil.setError(getString(R.string.event_organizer_error));
            parentScrollView.smoothScrollTo(0, 0);
            return false;
        } else {
            organizerTil.setError(null);
        }
        if (!isValidString(dateTil.getEditText().getText().toString())) {
            dateTil.setError(getString(R.string.event_date_error));
            parentScrollView.smoothScrollTo(0, 0);
            return false;
        } else {
            dateTil.setError(null);
        }
        if (!isValidString(startTimeTil.getEditText().getText().toString())) {
            startTimeTil.setError(getString(R.string.event_time_error));
            parentScrollView.smoothScrollTo(0, 0);
            return false;
        } else {
            startTimeTil.setError(null);
        }
        if (place == null) {
            Toast.makeText(this, R.string.event_map_error, Toast.LENGTH_SHORT).show();
            parentScrollView.smoothScrollTo(0, parentScrollView.getHeight());
            return false;
        }
        if (!isValidString(addressTil.getEditText().getText().toString())) {
            addressTil.setError(getString(R.string.event_address_error));
            return false;
        } else {
            addressTil.setError(null);
        }
        return true;
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

    private void fetchGroupInfo(String groupId) {
        final ValueEventListener joinedGroupRef = RealtimeDbHelper.getLubbleGroupsRef().child(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final GroupData groupData = dataSnapshot.getValue(GroupData.class);
                if (groupData != null) {
                    final Set<Map.Entry<String, Object>> entries = groupData.getMembers().entrySet();
                    for (Map.Entry<String, Object> entry : entries) {
                        final HashMap map = (HashMap) entry.getValue();
                        if (entry.getKey().equalsIgnoreCase(FirebaseAuth.getInstance().getUid())
                                && map.get("admin") == Boolean.TRUE && !groupData.getIsPrivate()) {
                            spinnerAdapter.add(groupData);
                            // enable 2nd radio btn
                            oldGroupRadioBtn.setEnabled(true);
                            oldGroupRadioBtn.setTextColor(ContextCompat.getColor(NewEventActivity.this, R.color.black));
                            notAdminHintTv.setVisibility(View.GONE);
                        }
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
                googleMap.clear();
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
                place = PlacePicker.getPlace(this, data);
                if (place != null) {
                    loadMapAt(place.getLatLng());
                    addressTil.getEditText().setText(place.getAddress());
                }
            }
        } else if (requestCode == REQUEST_CODE_EVENT_PIC && resultCode == RESULT_OK) {
            File imageFile;
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                imageFile = getFileFromInputStreamUri(this, uri);
            } else {
                // from camera
                imageFile = new File(currentPhotoPath);
            }
            picUri = Uri.fromFile(imageFile);
            GlideApp.with(this)
                    .load(imageFile)
                    .signature(new ObjectKey(imageFile.length() + "@" + imageFile.lastModified()))
                    .into(headerImage);
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        NewEventActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showRationaleForCamera(final PermissionRequest request) {
        showStoragePermRationale(this, request);
    }

    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showDeniedForCamera() {
        Toast.makeText(this, R.string.storage_perm_denied_text, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showNeverAskForCamera() {
        Toast.makeText(this, R.string.storage_perm_never_text, Toast.LENGTH_LONG).show();
    }

}
