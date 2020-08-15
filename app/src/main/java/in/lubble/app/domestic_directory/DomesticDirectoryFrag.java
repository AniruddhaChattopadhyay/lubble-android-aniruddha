package in.lubble.app.domestic_directory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.models.DomesticHelpData;

import static in.lubble.app.firebase.RealtimeDbHelper.getLubbleDomesticRef;

public class DomesticDirectoryFrag extends Fragment {

    private static final String TAG = "DomesticDirectoryFrag";

    private RecyclerView domesticHelpRecyclerView;
    private DomesticAdapter domesticAdapter;
    private ValueEventListener valueEventListener;
    private ProgressBar progressBar;

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

        progressBar = view.findViewById(R.id.progressBar_directory);
        domesticHelpRecyclerView = view.findViewById(R.id.rv_domestic_help);
        domesticHelpRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        domesticAdapter = new DomesticAdapter(getContext());
        domesticHelpRecyclerView.setAdapter(domesticAdapter);
        /*DividerItemDecoration itemDecor = new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL);
        domesticHelpRecyclerView.addItemDecoration(itemDecor);*/
        Analytics.triggerScreenEvent(getContext(), this.getClass());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        valueEventListener = getLubbleDomesticRef().orderByChild("category").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(View.GONE);
                }
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
        getLubbleDomesticRef().orderByChild("category").removeEventListener(valueEventListener);
    }
}
