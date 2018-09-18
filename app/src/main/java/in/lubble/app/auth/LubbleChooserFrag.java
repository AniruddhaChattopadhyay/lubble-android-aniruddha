package in.lubble.app.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import in.lubble.app.R;

import static android.app.Activity.RESULT_OK;
import static in.lubble.app.auth.LubbleChooserDialogFrag.ARG_CHOSEN_LOCATION;
import static in.lubble.app.utils.FragUtils.addFrag;
import static in.lubble.app.utils.UiUtils.dpToPx;

public class LubbleChooserFrag extends Fragment implements OnMapReadyCallback {

    private static final String ARG_IDP_RESPONSE = "ARG_IDP_RESPONSE";
    private static final String ARG_LOCATION_DATA = "ARG_LOCATION_DATA";

    static final int REQUEST_CODE_CHOOSE = 665;

    private Parcelable idpResponse;
    private ArrayList<LocationsData> locationsDataList;
    private LocationsData chosenLubbleData;
    private TextView lubbleNameTv;
    private Button joinbtn;
    private GoogleMap map;

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
                UserNameFrag userNameFrag = UserNameFrag.newInstance(idpResponse, chosenLubbleData);
                addFrag(getFragmentManager(), R.id.frame_fragContainer, userNameFrag);
            }
        });

        return view;
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
