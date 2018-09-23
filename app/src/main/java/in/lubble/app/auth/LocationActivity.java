package in.lubble.app.auth;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static in.lubble.app.Constants.MEDIA_TYPE;
import static in.lubble.app.utils.ReferralUtils.generateBranchUrl;
import static in.lubble.app.utils.ReferralUtils.getReferralIntent;

public class LocationActivity extends AppCompatActivity {

    private static final String TAG = "LocationActivity";
    private static final int REQUEST_SYSTEM_LOCATION = 859;
    private static final int REQUEST_LOCATION_ON = 150;

    private static final float LOCATION_ACCURACY_THRESHOLD = 150;
    private FusedLocationProviderClient fusedLocationClient;
    private RelativeLayout invalidLocContainer;
    private ImageView pulseIv;
    private ImageView locIv;
    private TextView locHintTv;
    private Button shareBtn;
    private Parcelable idpResponse;
    private Location currLocation;
    private int retryCount = 0;
    private String sharingUrl;
    private ProgressDialog sharingProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        invalidLocContainer = findViewById(R.id.invalid_loc_container);
        pulseIv = findViewById(R.id.iv_pulse);
        locIv = findViewById(R.id.iv_loc);
        locHintTv = findViewById(R.id.tv_loc_hint);
        shareBtn = findViewById(R.id.btn_action);

        idpResponse = getIntent().getParcelableExtra("idpResponse");

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onInviteClicked();
            }
        });
        pulseIv.setVisibility(View.GONE);
        locIv.setVisibility(View.GONE);
        checkSystemLocPerm();

        sharingProgressDialog = new ProgressDialog(this);
        generateBranchUrl(this, linkCreateListener);
    }

    private void onInviteClicked() {
        final Intent referralIntent = getReferralIntent(this, sharingUrl, sharingProgressDialog, linkCreateListener);
        if (referralIntent != null) {
            startActivity(Intent.createChooser(referralIntent, getString(R.string.refer_share_title)));
            Analytics.triggerEvent(AnalyticsEvents.REFERRAL_PROFILE_SHARE, this);
        }
    }

    final Branch.BranchLinkCreateListener linkCreateListener = new Branch.BranchLinkCreateListener() {
        @Override
        public void onLinkCreate(String url, BranchError error) {
            if (url != null) {
                Log.d(TAG, "got my Branch link to share: " + url);
                sharingUrl = url;
                if (sharingProgressDialog != null && sharingProgressDialog.isShowing()) {
                    sharingProgressDialog.dismiss();
                }
            } else {
                Log.e(TAG, "Branch onLinkCreate: " + error.getMessage());
                Crashlytics.logException(new IllegalStateException(error.getMessage()));
                if (!isFinishing()) {
                    Toast.makeText(LocationActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private void startAnims() {
        ObjectAnimator scaleAnim = ObjectAnimator.ofPropertyValuesHolder(
                pulseIv,
                PropertyValuesHolder.ofFloat("scaleX", 1.5f),
                PropertyValuesHolder.ofFloat("alpha", 0.1f),
                PropertyValuesHolder.ofFloat("scaleY", 1.5f));
        scaleAnim.setDuration(1000);

        scaleAnim.setRepeatCount(ObjectAnimator.INFINITE);
        scaleAnim.setRepeatMode(ObjectAnimator.RESTART);

        scaleAnim.start();
    }

    private void checkSystemLocPerm() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setMessage(getString(R.string.loc_perm_rationale));
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.all_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(LocationActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_SYSTEM_LOCATION);
                    }
                });
                alertDialog.setCancelable(false);
                alertDialog.show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_SYSTEM_LOCATION);
            }
        } else {
            // Permission has already been granted
            // check if location is enabled
            checkLocationSettings();
        }
    }

    private void checkLocationSettings() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(getLocationRequest());

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                getLocation();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(LocationActivity.this, REQUEST_LOCATION_ON);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_SYSTEM_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    checkLocationSettings();
                } else {
                    // permission denied, boo!
                    checkSystemLocPerm();
                }
                return;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOCATION_ON) {
            if (resultCode == Activity.RESULT_OK) {
                //fetch current location
                getLocation();
            } else {
                checkLocationSettings();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        pulseIv.setVisibility(View.VISIBLE);
        locIv.setVisibility(View.VISIBLE);
        startAnims();
        // get fresh loc
        fusedLocationClient.requestLocationUpdates(getLocationRequest().setNumUpdates(1), new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    getLocation();
                    return;
                }
                validateUserLocation(locationResult.getLastLocation());
            }
        }, null);
    }

    private void validateUserLocation(final Location location) {

        if ((!location.hasAccuracy() || location.getAccuracy() == 0.0 || location.getAccuracy() > LOCATION_ACCURACY_THRESHOLD)
                && retryCount < 10) {
            // bad location, refresh
            Log.d(TAG, "location bad accuracy of " + location.getAccuracy() + " @ retry: " + retryCount);
            retryCount++;
            getLocation();
            return;
        }
        currLocation = location;

        HashMap<String, Object> params = new HashMap<>();

        try {
            final JSONObject locationObject = new JSONObject();
            locationObject.put("loc_lati", location.getLatitude());
            locationObject.put("loc_longi", location.getLongitude());
            locationObject.put("loc_accuracy", location.getAccuracy());
            locationObject.put("loc_time", location.getTime());
            locationObject.put("retry_count", retryCount);
            params.put("location", locationObject);
            if (!TextUtils.isEmpty(LubbleSharedPrefs.getInstance().getReferrerUid())) {
                final JSONObject referralObject = new JSONObject();
                referralObject.put("referrer_uid", LubbleSharedPrefs.getInstance().getReferrerUid());
                params.put("referral", referralObject);
            } else if (!TextUtils.isEmpty(LubbleSharedPrefs.getInstance().getReferralCode())) {
                final JSONObject referralObject = new JSONObject();
                referralObject.put("referral_code", LubbleSharedPrefs.getInstance().getReferralCode());
                params.put("referral", referralObject);
            }
            final JSONObject userInfoObject = new JSONObject();
            userInfoObject.put("name", LubbleSharedPrefs.getInstance().getFullName());
            params.put("user_info", userInfoObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JSONObject jsonObject = new JSONObject(params);

        RequestBody body = RequestBody.create(MEDIA_TYPE, jsonObject.toString());

        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.uploadSignUp(body).enqueue(new Callback<ArrayList<LocationsData>>() {
            @Override
            public void onResponse(Call<ArrayList<LocationsData>> call, Response<ArrayList<LocationsData>> response) {
                if (response.isSuccessful() && !isFinishing()) {
                    final ArrayList<LocationsData> locationsDataList = response.body();
                    if (locationsDataList != null && !locationsDataList.isEmpty()) {
                        locationCheckSuccess(locationsDataList);
                    } else {
                        locationCheckFailed();
                    }
                } else {
                    locationCheckFailed();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<LocationsData>> call, Throwable t) {
                Log.e(TAG, "onFailure: ");
                locationCheckFailed();
            }
        });
    }

    private void locationCheckSuccess(ArrayList<LocationsData> locationsData) {
        Intent intent = new Intent();
        intent.putExtra("idpResponse", idpResponse);
        intent.putExtra("lubbleDataList", locationsData);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void locationCheckFailed() {
        pulseIv.setVisibility(View.GONE);
        locIv.setVisibility(View.GONE);
        locHintTv.setVisibility(View.GONE);
        invalidLocContainer.setVisibility(View.VISIBLE);
        final Bundle bundle = new Bundle();
        if (currLocation != null) {
            bundle.putDouble("lati", currLocation.getLatitude());
            bundle.putDouble("longi", currLocation.getLongitude());
            bundle.putDouble("accuracy", currLocation.getAccuracy());
        }
        Analytics.triggerEvent(AnalyticsEvents.LOC_CHECK_FAILED, bundle, this);
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

}
