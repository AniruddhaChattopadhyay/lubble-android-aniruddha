package in.lubble.app.summer_camp.new_camp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

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

import in.lubble.app.R;
import in.lubble.app.utils.mapUtils.SphericalUtil;

import static in.lubble.app.Constants.SVR_LATI;
import static in.lubble.app.Constants.SVR_LONGI;

public class NewCampActivity extends AppCompatActivity {

    private static final int PLACE_PICKER_REQUEST = 828;

    private SupportMapFragment mapFragment;
    private LatLng defaultLatLng;

    public static void open(Context context) {
        context.startActivity(new Intent(context, NewCampActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_camp);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        defaultLatLng = new LatLng(SVR_LATI, SVR_LONGI);
        loadMapAt(defaultLatLng);

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
                        NewCampActivity.this, R.raw.style_json));
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
                }
            }
        }
    }

}
