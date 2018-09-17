package in.lubble.app.auth;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.MapView;

import in.lubble.app.R;

import static in.lubble.app.utils.FragUtils.addFrag;

public class LubbleChooserFrag extends Fragment {

    private static final String ARG_IDP_RESPONSE = "ARG_IDP_RESPONSE";
    private static final String ARG_LOCATION_DATA = "ARG_LOCATION_DATA";

    private Parcelable idpResponse;
    private LocationsData locationsData;
    private LocationsData.LubbleData chosenLubbleData;

    public LubbleChooserFrag() {
        // Required empty public constructor
    }

    public static LubbleChooserFrag newInstance(Parcelable idpResponse, LocationsData locationsData) {
        LubbleChooserFrag fragment = new LubbleChooserFrag();
        Bundle args = new Bundle();
        args.putParcelable(ARG_IDP_RESPONSE, idpResponse);
        args.putSerializable(ARG_LOCATION_DATA, locationsData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            idpResponse = getArguments().getParcelable(ARG_IDP_RESPONSE);
            locationsData = (LocationsData) getArguments().getSerializable(ARG_LOCATION_DATA);
            chosenLubbleData = locationsData.getDefaultLubble();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_lubble_chooser, container, false);

        TextView lubbleNameTv = view.findViewById(R.id.tv_lubble_name);
        TextView changeLubbleTv = view.findViewById(R.id.tv_change_lubble);
        MapView mapView = view.findViewById(R.id.mapview);
        Button joinbtn = view.findViewById(R.id.btn_join);

        lubbleNameTv.setText(locationsData.getDefaultLubble().getLubbleName());
        joinbtn.setText("Join " + locationsData.getDefaultLubble().getLubbleName());

        changeLubbleTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        joinbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserNameFrag userNameFrag = UserNameFrag.newInstance(idpResponse);
                addFrag(getFragmentManager(), R.id.frame_fragContainer, userNameFrag);
            }
        });

        return view;
    }

}
