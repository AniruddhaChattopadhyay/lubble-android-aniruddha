package in.lubble.app.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.network.AirtableData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatMoreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatMoreFragment extends Fragment {
    private static final String TAG = "ChatMoreFragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView collectionTitleTv;
    private RecyclerView collectionsRecyclerView;

    public ChatMoreFragment() {
        // Required empty public constructor
    }

    public static ChatMoreFragment newInstance(String param1, String param2) {
        ChatMoreFragment fragment = new ChatMoreFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_chat_more, container, false);

        collectionTitleTv = view.findViewById(R.id.tv_collection_title);
        collectionsRecyclerView = view.findViewById(R.id.rv_1);

        collectionsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));

        fetchMore();
        return view;
    }


    private void fetchMore() {
        //todo progressbar.setVisibility(View.VISIBLE);
        final Endpoints endpoints = ServiceGenerator.createAirtableService(Endpoints.class);
        endpoints.fetchMore().enqueue(new Callback<AirtableData>() {
            @Override
            public void onResponse(Call<AirtableData> call, Response<AirtableData> response) {
                final AirtableData airtableData = response.body();
                if (response.isSuccessful() && airtableData != null && isAdded() && isVisible()) {
                    //todo progressbar.setVisibility(View.GONE);
                    //exploreGroupAdapter.updateList(exploreGroupDataList);
                    final ChatMoreData chatMoreData = airtableData.getRecords().get(0).getChatMoreData();
                    collectionTitleTv.setText(chatMoreData.getCollectionTitle());
                    final List<String> entries1List = chatMoreData.getCollectionList();
                    fetchEntries(entries1List);
                } else {
                    if (isAdded() && isVisible()) {
                        //todo progressbar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<AirtableData> call, Throwable t) {
                if (isAdded() && isVisible()) {
                    Toast.makeText(getContext(), R.string.check_internet, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onFailure: ");
                    //todoprogressbar.setVisibility(View.GONE);
                }
            }
        });
    }

    private void fetchEntries(List<String> entries1List) {
        //todo progressbar.setVisibility(View.VISIBLE);

        String filterByFormula = "";
        for (String recordId : entries1List) {
            filterByFormula = filterByFormula.concat("RECORD_ID()=\'" + recordId + "\',");
        }

        String url = "https://api.airtable.com/v0/appbhSWmy7ZS6UeTy/Collections?filterByFormula=\"OR(" + filterByFormula + ")\"&view=Grid%20view";

        final Endpoints endpoints = ServiceGenerator.createAirtableService(Endpoints.class);
        endpoints.fetchEntries(url).enqueue(new Callback<AirtableCollectionData>() {
            @Override
            public void onResponse(Call<AirtableCollectionData> call, Response<AirtableCollectionData> response) {
                final AirtableCollectionData airtableData = response.body();
                if (response.isSuccessful() && airtableData != null && isAdded() && isVisible()) {
                    //todo progressbar.setVisibility(View.GONE);
                    //exploreGroupAdapter.updateList(exploreGroupDataList);
                    collectionsRecyclerView.setAdapter(new CollectionsAdapter(GlideApp.with(requireContext()), airtableData.getRecords()));
                } else {
                    if (isAdded() && isVisible()) {
                        //todo progressbar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<AirtableCollectionData> call, Throwable t) {
                if (isAdded() && isVisible()) {
                    Toast.makeText(getContext(), R.string.check_internet, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onFailure: ");
                    //todoprogressbar.setVisibility(View.GONE);
                }
            }
        });
    }


}
