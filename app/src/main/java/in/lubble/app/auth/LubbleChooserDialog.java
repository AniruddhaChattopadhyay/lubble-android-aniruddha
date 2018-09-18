package in.lubble.app.auth;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

import in.lubble.app.R;
import in.lubble.app.utils.DialogInterface;

public class LubbleChooserDialog extends Dialog {

    private static final String TAG = "LubbleChooserDialog";

    private ArrayList<LocationsData> locationsDataList;
    private DialogInterface dialogInterface;

    public LubbleChooserDialog(@NonNull Context context, ArrayList<LocationsData> locationsDataList, DialogInterface dialogInterface) {
        super(context);
        this.dialogInterface = dialogInterface;
        this.locationsDataList = locationsDataList;
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RecyclerView recyclerView = findViewById(R.id.rv_all_lubbles);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        final LubbleChooserAdapter adapter = new LubbleChooserAdapter(new DialogInterface() {
            @Override
            public void onClick(Object object) {
                dialogInterface.onClick(object);
            }
        });
        recyclerView.setAdapter(adapter);

        for (LocationsData lubbleData : locationsDataList) {
            adapter.addData(lubbleData);
        }

    }

}
