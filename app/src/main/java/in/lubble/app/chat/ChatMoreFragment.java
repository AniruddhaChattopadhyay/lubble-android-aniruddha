package in.lubble.app.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.chat.books.BookFragment;
import in.lubble.app.chat.collections.AirtableCollectionData;
import in.lubble.app.chat.collections.CollectionsAdapter;
import in.lubble.app.models.EventData;
import in.lubble.app.models.ProfileData;
import in.lubble.app.network.AirtableData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.utils.FragUtils;
import in.lubble.app.utils.UiUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

import static in.lubble.app.firebase.RealtimeDbHelper.*;

public class ChatMoreFragment extends Fragment {
    private static final String TAG = "ChatMoreFragment";

    private static final String ARG_GROUP_ID = "ChatMoreFragment.ARG_GROUP_ID";
    private static final String ARG_PARAM2 = "param2";

    private String groupId;
    private String mParam2;

    private EditText flairEt;
    private TextView updateFlairTv;
    private ProgressBar flairProgressbar;
    private TextView collectionTitleTv;
    private RecyclerView collectionsRecyclerView;
    private RecyclerView eventsRecyclerView;
    private LinearLayout noCollectionsContainer;
    private LinearLayout noEventsContainer;
    private ProgressBar progressBar;
    private ProgressBar eventProgressBar;
    private FrameLayout frameLayout;
    private ValueEventListener flairListener;
    private ValueEventListener eventsListener;

    public ChatMoreFragment() {
        // Required empty public constructor
    }

    public static ChatMoreFragment newInstance(String groupId, String param2) {
        ChatMoreFragment fragment = new ChatMoreFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID, groupId);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            groupId = getArguments().getString(ARG_GROUP_ID);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_chat_more, container, false);

        flairEt = view.findViewById(R.id.et_flair);
        updateFlairTv = view.findViewById(R.id.tv_update_flair);
        flairProgressbar = view.findViewById(R.id.progressbar_flair);
        collectionTitleTv = view.findViewById(R.id.tv_collection_title);
        progressBar = view.findViewById(R.id.progressbar_chat_more);
        eventProgressBar = view.findViewById(R.id.progressbar_events);
        noCollectionsContainer = view.findViewById(R.id.container_no_collections);
        noEventsContainer = view.findViewById(R.id.container_no_events);
        collectionsRecyclerView = view.findViewById(R.id.rv_1);
        eventsRecyclerView = view.findViewById(R.id.rv_events);
        frameLayout = view.findViewById(R.id.framelayout_container);

        collectionsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));

        fetchMore();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        syncFlair();
        syncEvents();
    }

    private void syncEvents() {
        eventProgressBar.setVisibility(View.VISIBLE);
        eventsListener = getEventsRef().orderByChild("startTimestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (eventProgressBar != null) {
                    eventProgressBar.setVisibility(View.GONE);
                }
                eventsRecyclerView.setVisibility(View.VISIBLE);
                noEventsContainer.setVisibility(View.GONE);
                if (dataSnapshot.getChildrenCount() > 0) {
                    final ArrayList<EventData> eventDataList = new ArrayList<>();
                    for (DataSnapshot dataSnapshotChild : dataSnapshot.getChildren()) {
                        final EventData eventData = dataSnapshotChild.getValue(EventData.class);
                        if (eventData != null && System.currentTimeMillis() < eventData.getStartTimestamp() && eventData.getRelatedGroupsList().contains(groupId)) {
                            eventData.setId(dataSnapshotChild.getKey());
                            eventDataList.add(eventData);
                        }
                    }
                    if (eventDataList.size() > 0) {
                        eventsRecyclerView.setAdapter(new ChatEventsAdapter(requireContext(), eventDataList));
                    } else {
                        eventsRecyclerView.setVisibility(View.GONE);
                        noEventsContainer.setVisibility(View.VISIBLE);
                    }
                } else {
                    // no events
                    if (eventProgressBar != null) {
                        eventProgressBar.setVisibility(View.GONE);
                    }
                    eventsRecyclerView.setVisibility(View.GONE);
                    noEventsContainer.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void syncFlair() {
        flairEt.setEnabled(false);
        flairListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final ProfileData profileData = dataSnapshot.getValue(ProfileData.class);
                if (profileData != null) {
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        if (childSnapshot.getKey().equalsIgnoreCase("lubbles")) {
                            final DataSnapshot userGroupsSnapshot = childSnapshot.child(LubbleSharedPrefs.getInstance().requireLubbleId()).child("groups");

                            if (userGroupsSnapshot.hasChild(groupId) && (boolean) userGroupsSnapshot.child(groupId).child("joined").getValue()) {
                                //is joined
                                flairEt.setEnabled(true);
                                final String flair = userGroupsSnapshot.child(groupId).child("flair").getValue(String.class);
                                profileData.setGroupFlair(flair);
                                flairEt.setText(flair);
                                flairEt.setCursorVisible(true);
                                flairEt.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        flairEt.setCursorVisible(true);
                                    }
                                });

                                updateFlairTv.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        flairProgressbar.setVisibility(View.VISIBLE);
                                        updateFlairTv.setText("");
                                        flairEt.setCursorVisible(false);
                                        getUserGroupsRef().child(groupId).child("flair").setValue(flairEt.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                flairProgressbar.setVisibility(View.GONE);
                                                updateFlairTv.setText("UPDATE");
                                                if (!task.isSuccessful()) {
                                                    Toast.makeText(requireContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(requireContext(), "Updated!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                        UiUtils.hideKeyboard(requireContext());
                                    }
                                });
                            } else {
                                flairEt.setEnabled(false);
                                updateFlairTv.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Toast.makeText(requireContext(), "Please join the group first", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        getThisUserRef().addValueEventListener(flairListener);
    }

    private void fetchMore() {
        String formula = "Lubble=\'" + LubbleSharedPrefs.getInstance().getLubbleId() + "\', GroupID=\'" + groupId + "\'";

        String url = "https://api.airtable.com/v0/appbhSWmy7ZS6UeTy/GroupMapping?view=Grid%20view&filterByFormula=AND(" + formula + ")";

        final Endpoints endpoints = ServiceGenerator.createAirtableService(Endpoints.class);
        endpoints.fetchMore(url).enqueue(new Callback<AirtableData>() {
            @Override
            public void onResponse(Call<AirtableData> call, Response<AirtableData> response) {
                final AirtableData airtableData = response.body();
                if (response.isSuccessful() && airtableData != null && isAdded() && isVisible()) {
                    if (airtableData.getRecords().size() > 0) {
                        noCollectionsContainer.setVisibility(View.GONE);
                        final ChatMoreData chatMoreData = airtableData.getRecords().get(0).getChatMoreData();
                        collectionTitleTv.setText(chatMoreData.getCollectionTitle());
                        if (chatMoreData.getIsBooksGroup()) {
                            FragUtils.addFrag(getChildFragmentManager(), frameLayout.getId(), BookFragment.newInstance());
                        }
                        final List<String> entries1List = chatMoreData.getCollectionList();
                        fetchEntries(entries1List);
                    } else {
                        noCollectionsContainer.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                    }
                } else {
                    if (isAdded() && isVisible()) {
                        Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<AirtableData> call, Throwable t) {
                if (isAdded() && isVisible()) {
                    Toast.makeText(getContext(), R.string.check_internet, Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "onFailure: ");
                }
            }
        });
    }

    private void fetchEntries(List<String> entries1List) {
        progressBar.setVisibility(View.VISIBLE);
        String filterByFormula = "";
        boolean first = true;
        for (String recordId : entries1List) {
            if (first) {
                filterByFormula = filterByFormula.concat("RECORD_ID()=\'" + recordId + "\'");
                first = false;
            } else {
                filterByFormula = filterByFormula.concat(",RECORD_ID()=\'" + recordId + "\'");
            }
        }

        String url = "https://api.airtable.com/v0/appbhSWmy7ZS6UeTy/Collections?filterByFormula=OR(" + filterByFormula + ")&view=Grid%20view";

        final Endpoints endpoints = ServiceGenerator.createAirtableService(Endpoints.class);
        endpoints.fetchEntries(url).enqueue(new Callback<AirtableCollectionData>() {
            @Override
            public void onResponse(Call<AirtableCollectionData> call, Response<AirtableCollectionData> response) {
                final AirtableCollectionData airtableData = response.body();
                if (response.isSuccessful() && airtableData != null && isAdded() && isVisible()) {
                    progressBar.setVisibility(View.GONE);
                    //exploreGroupAdapter.updateList(exploreGroupDataList);
                    collectionsRecyclerView.setAdapter(new CollectionsAdapter(GlideApp.with(requireContext()), airtableData.getRecords()));
                } else {
                    if (isAdded() && isVisible()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<AirtableCollectionData> call, Throwable t) {
                if (isAdded() && isVisible()) {
                    Toast.makeText(getContext(), R.string.check_internet, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onFailure: ");
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (flairListener != null) {
            getThisUserRef().removeEventListener(flairListener);
        }
        if (eventsListener != null) {
            getEventsRef().orderByChild("startTimestamp").removeEventListener(eventsListener);
        }
    }
}
