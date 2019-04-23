package in.lubble.app.chat.books;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import in.lubble.app.FetchAddressIntentService;
import in.lubble.app.R;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.ProfileAddress;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static in.lubble.app.FetchAddressIntentService.*;
import static in.lubble.app.chat.books.AddressChooserActivPermissionsDispatcher.fetchLastKnownLocationWithPermissionCheck;

@RuntimePermissions
public class AddressChooserActiv extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "AddressChooserActiv";

    private GoogleMap mMap;
    private TextInputLayout locationTil;
    private ImageView mapMarkerIv;
    private TextInputLayout houseNumberTil;
    private TextInputLayout landmarkTil;
    private Button addrDoneBtn;
    private AddressResultReceiver resultReceiver;

    public static void open(Context context) {
        context.startActivity(new Intent(context, AddressChooserActiv.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activ_address_chooser);

        locationTil = findViewById(R.id.til_location);
        houseNumberTil = findViewById(R.id.til_flat_number);
        landmarkTil = findViewById(R.id.til_landmark);
        addrDoneBtn = findViewById(R.id.btn_addr_done);

        resultReceiver = new AddressResultReceiver(new Handler());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        addrDoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidAddress()) {
                    LatLng centerOfMap = mMap.getCameraPosition().target;
                    final ProfileAddress profileAddress = new ProfileAddress();
                    profileAddress.setLocation(locationTil.getEditText().toString());
                    profileAddress.setHouseNumber(houseNumberTil.getEditText().toString());
                    profileAddress.setLandmark(landmarkTil.getEditText().toString());
                    profileAddress.setLatitude(centerOfMap.latitude);
                    profileAddress.setLongitude(centerOfMap.longitude);

                    final ProgressDialog progressDialog = new ProgressDialog(AddressChooserActiv.this);
                    progressDialog.setTitle("Adding Address");
                    progressDialog.setMessage(getString(R.string.all_please_wait));
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    RealtimeDbHelper.getThisUserRef().child("profileAddress").setValue(profileAddress)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        progressDialog.dismiss();
                                        finish();
                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(AddressChooserActiv.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        fetchLastKnownLocationWithPermissionCheck(this);

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                // Get the center of the Map.
                LatLng centerOfMap = mMap.getCameraPosition().target;

                mMap.clear();
                final Location location = new Location("");
                location.setLatitude(centerOfMap.latitude);
                location.setLongitude(centerOfMap.longitude);
                fetchAddressFromLocation(location);
            }
        });
    }

    @SuppressLint("MissingPermission")
    @NeedsPermission(ACCESS_FINE_LOCATION)
    public void fetchLastKnownLocation() {
        LocationServices.getFusedLocationProviderClient(this).getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            if (mMap != null) {
                                updateMap(location);
                                fetchAddressFromLocation(location);
                            }
                        } else {
                            fetchLastKnownLocationWithPermissionCheck(AddressChooserActiv.this);
                        }
                    }
                });
    }

    private void fetchAddressFromLocation(Location location) {
        Intent intent = new Intent(AddressChooserActiv.this, FetchAddressIntentService.class);
        intent.putExtra(RECEIVER, resultReceiver);
        intent.putExtra(LOCATION_DATA_EXTRA, location);
        startService(intent);
    }

    @SuppressLint("MissingPermission")
    private void updateMap(Location location) {
        mMap.clear();
        // Add Marker
        final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        //mMap.addMarker(new MarkerOptions().position(latLng));
        mMap.setMyLocationEnabled(true);

        // Center map on the marker
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(latLng, 17.0f);
        mMap.animateCamera(yourLocation);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                AddressChooserActiv.this, R.raw.style_json));
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultData == null) {
                return;
            }

            // Display the address string
            // or an error message sent from the intent service.
            String addressOutput = resultData.getString(RESULT_DATA_KEY);
            if (addressOutput == null) {
                addressOutput = "";
            }
            locationTil.getEditText().setText(addressOutput);

        }
    }

    private boolean isValidAddress() {
        if (TextUtils.isEmpty(locationTil.getEditText().getText().toString())) {
            locationTil.setError("Please set Location on Map");
            return false;
        } else {
            locationTil.setError(null);
        }
        if (TextUtils.isEmpty(houseNumberTil.getEditText().getText().toString())) {
            houseNumberTil.setError("Please enter house no.");
            return false;
        } else {
            houseNumberTil.setError(null);
        }
        if (TextUtils.isEmpty(landmarkTil.getEditText().getText().toString())) {
            landmarkTil.setError("Please enter landmark");
            return false;
        } else {
            landmarkTil.setError(null);
        }
        return true;
    }

}
