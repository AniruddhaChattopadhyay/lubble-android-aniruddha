package in.lubble.app.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import in.lubble.app.R;
import in.lubble.app.utils.DialogInterface;

import static android.app.Activity.RESULT_OK;
import static in.lubble.app.auth.LubbleChooserFrag.REQUEST_CODE_CHOOSE;

public class LubbleChooserDialogFrag extends DialogFragment {

    private static final String TAG = "LubbleChooserDialogFrag";
    private static final String ARG_LOCATION_DATA_LIST = "ARG_LOCATION_DATA_LIST";
    static final String ARG_CHOSEN_LOCATION = "chosen_location";

    private ArrayList<LocationsData> locationsDataList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static LubbleChooserDialogFrag newInstance(ArrayList<LocationsData> locationsDataList) {

        Bundle args = new Bundle();
        args.putSerializable(ARG_LOCATION_DATA_LIST, locationsDataList);

        LubbleChooserDialogFrag fragment = new LubbleChooserDialogFrag();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_lubble_chooser, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.rv_all_lubbles);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        final LubbleChooserAdapter adapter = new LubbleChooserAdapter(new DialogInterface() {
            @Override
            public void onClick(Object object) {
                final Intent intent = new Intent();
                intent.putExtra(ARG_CHOSEN_LOCATION, (LocationsData) object);
                getTargetFragment().onActivityResult(REQUEST_CODE_CHOOSE, RESULT_OK, intent);
                dismissAllowingStateLoss();
            }
        });
        DividerItemDecoration itemDecor = new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(itemDecor);
        recyclerView.setAdapter(adapter);

        locationsDataList = (ArrayList<LocationsData>) getArguments().getSerializable(ARG_LOCATION_DATA_LIST);

        for (LocationsData lubbleData : locationsDataList) {
            adapter.addData(lubbleData);
        }

        return view;
    }

}
