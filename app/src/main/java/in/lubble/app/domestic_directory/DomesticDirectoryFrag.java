package in.lubble.app.domestic_directory;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import in.lubble.app.R;
import in.lubble.app.models.DomesticHelpData;

import static in.lubble.app.firebase.RealtimeDbHelper.getLubbleDomesticRef;

public class DomesticDirectoryFrag extends Fragment {

    private static final String TAG = "DomesticDirectoryFrag";

    private RecyclerView domesticHelpRecyclerView;
    private DomesticAdapter domesticAdapter;

    public DomesticDirectoryFrag() {
        // Required empty public constructor
    }

    public static DomesticDirectoryFrag newInstance() {
        return new DomesticDirectoryFrag();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_domestic_directory, container, false);

        domesticHelpRecyclerView = view.findViewById(R.id.rv_domestic_help);
        domesticHelpRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        domesticAdapter = new DomesticAdapter(getContext());
        domesticHelpRecyclerView.setAdapter(domesticAdapter);
        /*DividerItemDecoration itemDecor = new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL);
        domesticHelpRecyclerView.addItemDecoration(itemDecor);*/
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getLubbleDomesticRef().orderByChild("category").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<DomesticHelpData> domesticHelpList = new ArrayList<>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    domesticHelpList.add(child.getValue(DomesticHelpData.class));
                }
                domesticAdapter.addAll(prepareList(domesticHelpList));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private ArrayList<DomesticHelpData> prepareList(ArrayList<DomesticHelpData> domesticHelpList) {
        final ArrayList<DomesticHelpData> completeList = new ArrayList<>();
        String prevCategory = "";
        for (DomesticHelpData domesticHelpData : domesticHelpList) {
            if (!domesticHelpData.getCategory().equalsIgnoreCase(prevCategory)) {
                DomesticHelpData categoryData = new DomesticHelpData();
                categoryData.setName(domesticHelpData.getCategory());
                completeList.add(categoryData);
                prevCategory = domesticHelpData.getCategory();
            }
            completeList.add(domesticHelpData);
        }
        return completeList;
    }

    @Override
    public void onPause() {
        super.onPause();

    }
}
