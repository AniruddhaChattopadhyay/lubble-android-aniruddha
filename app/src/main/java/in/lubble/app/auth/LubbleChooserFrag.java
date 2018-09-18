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

import java.util.ArrayList;

import in.lubble.app.R;
import in.lubble.app.utils.DialogInterface;

import static in.lubble.app.utils.FragUtils.addFrag;

public class LubbleChooserFrag extends Fragment {

    private static final String ARG_IDP_RESPONSE = "ARG_IDP_RESPONSE";
    private static final String ARG_LOCATION_DATA = "ARG_LOCATION_DATA";

    private Parcelable idpResponse;
    private ArrayList<LocationsData> locationsDataList;
    private LocationsData chosenLubbleData;

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

        TextView lubbleNameTv = view.findViewById(R.id.tv_lubble_name);
        TextView changeLubbleTv = view.findViewById(R.id.tv_change_lubble);
        MapView mapView = view.findViewById(R.id.mapview);
        Button joinbtn = view.findViewById(R.id.btn_join);

        lubbleNameTv.setText(locationsDataList.get(0).getLubbleName());
        joinbtn.setText("Join " + locationsDataList.get(0).getLubbleName());

        changeLubbleTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LubbleChooserDialog(getContext(), locationsDataList, new DialogInterface() {
                    @Override
                    public void onClick(Object object) {
                        chosenLubbleData = (LocationsData) object;
                    }
                }).show();
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
