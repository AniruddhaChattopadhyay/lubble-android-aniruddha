package in.lubble.app.feed_user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingFormatArgumentException;

import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.models.FeedGroupData;
import in.lubble.app.models.FeedPostData;
import in.lubble.app.services.FeedServices;
import io.getstream.core.exceptions.StreamException;

import static android.app.Activity.RESULT_OK;

public class GroupSelectionFrag extends Fragment {

    private static final String ARG_POST_DATA = "LBL_ARG_POST_DATA";
    private static final String TAG = "GroupSelectionFrag";

    private RecyclerView groupsRv;
    private SearchView groupSv;
    private MaterialButton postSubmitBtn;
    private FeedPostData feedPostData;
    private List<FeedGroupData> dummyGroupList;

    public static GroupSelectionFrag newInstance(FeedPostData feedPostData) {
        GroupSelectionFrag groupSelectionFrag = new GroupSelectionFrag();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_POST_DATA, feedPostData);
        groupSelectionFrag.setArguments(bundle);
        return groupSelectionFrag;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_group_selection, container, false);

        groupsRv = view.findViewById(R.id.rv_groups);
        groupSv = view.findViewById(R.id.sv_group_selection);
        postSubmitBtn = view.findViewById(R.id.btn_post);

        groupsRv.setLayoutManager(new LinearLayoutManager(requireContext()));

        dummyGroupList = getDummyGroupList();
        GroupSelectionAdapter groupSelectionAdapter = new GroupSelectionAdapter(dummyGroupList);
        groupsRv.setAdapter(groupSelectionAdapter);

        if (getArguments() != null && getArguments().containsKey(ARG_POST_DATA)) {
            feedPostData = (FeedPostData) getArguments().getSerializable(ARG_POST_DATA);
        } else {
            throw new MissingFormatArgumentException("no ARG_POST_DATA passed while opening GroupSelectionFrag");
        }
        groupSv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                groupSelectionAdapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                groupSelectionAdapter.filter(newText);
                return true;
            }
        });

        postSubmitBtn.setOnClickListener(v -> {
            String text = feedPostData.getText();
            FeedGroupData selectedGroupData = dummyGroupList.get(groupSelectionAdapter.getLastCheckedPos());
            String groupNameText = selectedGroupData.getName();
            boolean result = true;
            if (text != null) {
                try {
                    result = FeedServices.post(text, groupNameText);
                } catch (StreamException e) {
                    e.printStackTrace();
                }
            }
            if (result)
                requireActivity().setResult(RESULT_OK);
            requireActivity().finish();
        });

        return view;
    }

    private List<FeedGroupData> getDummyGroupList() {
        List<FeedGroupData> stringList = new ArrayList<>();
        FeedGroupData feedGroupData = new FeedGroupData();

        feedGroupData.setId(0);
        feedGroupData.setName("Pikachu");
        feedGroupData.setFeedName("Pikachu");
        feedGroupData.setLubble(LubbleSharedPrefs.getInstance().getLubbleId());
        stringList.add(feedGroupData);

        for (int i = 1; i < 15; i++) {
            FeedGroupData groupData = new FeedGroupData();
            groupData.setId(i);
            groupData.setName(String.valueOf(i));
            stringList.add(groupData);
        }
        return stringList;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

}