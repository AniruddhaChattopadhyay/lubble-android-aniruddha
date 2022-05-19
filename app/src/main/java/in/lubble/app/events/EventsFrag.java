package in.lubble.app.events;

import static android.app.Activity.RESULT_OK;
import static in.lubble.app.Constants.EVENTS_MAINTENANCE_IMG;
import static in.lubble.app.Constants.EVENTS_MAINTENANCE_TEXT;
import static in.lubble.app.events.EventsFragPermissionsDispatcher.fetchLastKnownLocationWithPermissionCheck;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.List;

import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.events.new_event.NewEventActivity;
import in.lubble.app.models.EventData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.utils.ActivityResultListener;
import in.lubble.app.utils.UiUtils;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@RuntimePermissions
public class EventsFrag extends Fragment implements ActivityResultListener {
    private static final String TAG = "EventsFrag";
    private static final int REQUEST_LOCATION_ON = 150;
    private RecyclerView recyclerView;
    private TextView maintenanceTv;
    private LottieAnimationView maintenanceAnim;
    private ExtendedFloatingActionButton fab;
    private EventsAdapter adapter;
    private ChildEventListener childEventListener;
    private ProgressBar progressBar;

    public EventsFrag() {
        // Required empty public constructor
    }

    public static EventsFrag newInstance() {
        return new EventsFrag();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_events, container, false);

        progressBar = view.findViewById(R.id.progressBar_events);
        maintenanceTv = view.findViewById(R.id.tv_maintenance_text);
        maintenanceAnim = view.findViewById(R.id.anim_maintenance);
        recyclerView = view.findViewById(R.id.rv_events);
        fab = view.findViewById(R.id.fab_new_event);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) requireActivity()).setOnActivResultForFrag(this);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventsAdapter(getContext());
        adapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);

        recyclerView.setAdapter(adapter);
        Analytics.triggerScreenEvent(getContext(), this.getClass());

        fab.setOnClickListener(v -> NewEventActivity.open(getContext()));

        LubbleSharedPrefs.getInstance().setEventSet(null);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    UiUtils.animateSlideDownHide(getContext(), fab);
                } else {
                    UiUtils.animateSlideUpShow(getContext(), fab);
                }
            }
        });
        checkLocationSettings();
        return view;
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setNumUpdates(1);
        return locationRequest;
    }

    private void checkLocationSettings() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(getLocationRequest());

        SettingsClient client = LocationServices.getSettingsClient(getActivity());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(locationSettingsResponse -> fetchLastKnownLocationWithPermissionCheck(EventsFrag.this));

        task.addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(requireActivity(), REQUEST_LOCATION_ON);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        String maintenanceText = FirebaseRemoteConfig.getInstance().getString(EVENTS_MAINTENANCE_TEXT);
        String maintenanceImageUrl = FirebaseRemoteConfig.getInstance().getString(EVENTS_MAINTENANCE_IMG);
        if (!TextUtils.isEmpty(maintenanceText)) {
            maintenanceTv.setVisibility(View.VISIBLE);
            maintenanceAnim.setVisibility(View.VISIBLE);
            maintenanceTv.setText(maintenanceText.replace("\\n", "\n"));
            maintenanceAnim.setAnimationFromUrl(maintenanceImageUrl);
            recyclerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        } else {
            maintenanceTv.setVisibility(View.GONE);
            maintenanceAnim.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void fetchLastKnownLocation() {
        LocationServices.getFusedLocationProviderClient(requireContext()).getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                // Logic to handle location object
                getEvents(location);
            } else {
                getEventsWithoutLoc();
            }
        });
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).toggleSearchInToolbar(false);
        }
    }

    private void getEventsWithoutLoc() {
        final Location dummyLoc = new Location("dummy");
        dummyLoc.setLatitude(LubbleSharedPrefs.getInstance().getCenterLati());
        dummyLoc.setLongitude(LubbleSharedPrefs.getInstance().getCenterLongi());
        getEvents(dummyLoc);
    }

    private void getEvents(Location location) {
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.getEvents(location.getLatitude(), location.getLongitude()).enqueue(new Callback<List<EventData>>() {
            @Override
            public void onResponse(@NonNull Call<List<EventData>> call, @NonNull Response<List<EventData>> response) {
                if (response.isSuccessful() && isAdded()) {
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                    adapter.clear();
                    List<EventData> dataList = response.body();
                    if (dataList != null & !dataList.isEmpty()) {
                        adapter.addEvents(dataList);
                        maintenanceTv.setVisibility(View.GONE);
                        maintenanceAnim.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    } else {
                        maintenanceTv.setVisibility(View.VISIBLE);
                        maintenanceAnim.setVisibility(View.VISIBLE);
                        maintenanceTv.setText("Be the first to add events!");
                        maintenanceAnim.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_undraw_events_2p66));
                        recyclerView.setVisibility(View.GONE);
                    }
                } else {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Failed: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<EventData>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Failed:" + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onActivityResultForFrag(int requestCode, int resultCode, Intent data) {
        onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOCATION_ON) {
            if (resultCode == RESULT_OK) {
                fetchLastKnownLocationWithPermissionCheck(EventsFrag.this);
            } else {
                final AlertDialog alertDialog = new AlertDialog.Builder(requireContext()).create();
                alertDialog.setMessage(getString(R.string.loc_perm_rationale_events));
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.all_ok), (dialog, which) -> checkLocationSettings());
                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.all_cancel), (dialog, which) -> getEventsWithoutLoc());
                alertDialog.setCancelable(false);
                alertDialog.show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        EventsFragPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    void showRationaleForEventLoc(final PermissionRequest request) {
        final AlertDialog alertDialog = new AlertDialog.Builder(requireContext()).create();
        alertDialog.setMessage(getString(R.string.loc_perm_rationale_events));
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.all_ok), (dialog, which) -> request.proceed());
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.all_cancel), (dialog, which) -> request.cancel());
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
    void showDeniedForEventLoc() {
        getEventsWithoutLoc();
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_FINE_LOCATION)
    void showNeverAskForEventLoc() {
        getEventsWithoutLoc();
    }
}
