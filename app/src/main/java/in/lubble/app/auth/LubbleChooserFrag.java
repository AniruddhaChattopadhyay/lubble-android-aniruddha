package in.lubble.app.auth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.MainActivity;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.models.ProfileData;
import in.lubble.app.models.ProfileInfo;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static in.lubble.app.Constants.MEDIA_TYPE;
import static in.lubble.app.auth.LubbleChooserDialogFrag.ARG_CHOSEN_LOCATION;
import static in.lubble.app.firebase.RealtimeDbHelper.getThisUserRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserLubbleRef;
import static in.lubble.app.utils.UiUtils.dpToPx;

public class LubbleChooserFrag extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "LubbleChooserFrag";

    private static final String ARG_IDP_RESPONSE = "ARG_IDP_RESPONSE";
    private static final String ARG_LOCATION_DATA = "ARG_LOCATION_DATA";

    static final int REQUEST_CODE_CHOOSE = 665;

    private Parcelable idpResponse;
    private ArrayList<LocationsData> locationsDataList;
    private LocationsData chosenLubbleData;
    private TextView lubbleNameTv;
    private Button joinbtn;
    private GoogleMap map;
    private ProgressDialog progressDialog;

    public LubbleChooserFrag() {
        // Required empty public constructor
    }

    public static LubbleChooserFrag newInstance(Parcelable idpResponse, ArrayList<LocationsData> locationsDataList) {
        LubbleChooserFrag fragment = new LubbleChooserFrag();
        Bundle args = new Bundle();
        args.putParcelable(ARG_IDP_RESPONSE, idpResponse);
        args.putSerializable(ARG_LOCATION_DATA, locationsDataList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            idpResponse = getArguments().getParcelable(ARG_IDP_RESPONSE);
            this.locationsDataList = (ArrayList<LocationsData>) getArguments().getSerializable(ARG_LOCATION_DATA);
            chosenLubbleData = locationsDataList.get(0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_lubble_chooser, container, false);

        lubbleNameTv = view.findViewById(R.id.tv_lubble_name);
        TextView changeLubbleTv = view.findViewById(R.id.tv_change_lubble);
        MapView mapView = view.findViewById(R.id.mapview);
        joinbtn = view.findViewById(R.id.btn_join);

        mapView.onCreate(savedInstanceState);
        mapView.onResume(); // needed to get the map to display immediately
        mapView.getMapAsync(this);

        lubbleNameTv.setText(chosenLubbleData.getLubbleName());
        joinbtn.setText("Join " + chosenLubbleData.getLubbleName());

        if (locationsDataList.size() > 1) {
            changeLubbleTv.setVisibility(View.VISIBLE);
            changeLubbleTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final LubbleChooserDialogFrag lubbleChooserDialogFrag = LubbleChooserDialogFrag.newInstance(locationsDataList);
                    lubbleChooserDialogFrag.setTargetFragment(LubbleChooserFrag.this, REQUEST_CODE_CHOOSE);
                    lubbleChooserDialogFrag.show(getFragmentManager(), null);
                }
            });
        } else {
            changeLubbleTv.setVisibility(View.GONE);
        }

        joinbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(getContext());
                progressDialog.setMessage(getString(R.string.all_updating));
                progressDialog.show();

                LubbleSharedPrefs.getInstance().setLubbleId(chosenLubbleData.getLubbleFirebaseId());

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                ProfileData profileData = new ProfileData();
                final ProfileInfo profileInfo = new ProfileInfo();
                profileInfo.setId(user.getUid());
                profileInfo.setName(LubbleSharedPrefs.getInstance().getFullName());
                profileData.setInfo(profileInfo);
                profileData.setToken(FirebaseInstanceId.getInstance().getToken());

                getThisUserRef().setValue(profileData);
                getUserLubbleRef().setValue("true");

                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(LubbleSharedPrefs.getInstance().getFullName())
                        .build();

                user.updateProfile(profileUpdates)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User profile updated.");
                                    completeSignup();
                                } else {
                                    Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }
        });

        return view;
    }

    private void completeSignup() {

        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("lubble", chosenLubbleData.getId());

            RequestBody body = RequestBody.create(MEDIA_TYPE, jsonObject.toString());

            final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
            endpoints.uploadSignUpComplete(body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful() && isAdded() && isVisible()) {
                        progressDialog.dismiss();
                        Analytics.triggerSignUpEvent(getContext());
                        startActivity(MainActivity.createIntent(getContext(), ((IdpResponse) idpResponse)));
                        getActivity().finishAffinity();
                    } else if (isAdded() && isVisible()) {
                        Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "onFailure: ");
                    if (isAdded() && isVisible()) {
                        Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE) {
            if (resultCode == RESULT_OK && data != null && data.hasExtra(ARG_CHOSEN_LOCATION)) {
                chosenLubbleData = (LocationsData) data.getSerializableExtra(ARG_CHOSEN_LOCATION);
                lubbleNameTv.setText(chosenLubbleData.getLubbleName());
                joinbtn.setText("Join " + chosenLubbleData.getLubbleName());
                map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(chosenLubbleData.getCenterLati(), chosenLubbleData.getCenterLongi())));
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMapToolbarEnabled(false);
        map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(chosenLubbleData.getCenterLati(), chosenLubbleData.getCenterLongi())));
        map.setPadding(0, dpToPx(140), 0, dpToPx(40));
        map.setOnMapClickListener(null);
    }

}
