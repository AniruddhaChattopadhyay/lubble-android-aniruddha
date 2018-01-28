package in.lubble.app.groups;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import in.lubble.app.R;
import in.lubble.app.chat.ChatActivity;
import in.lubble.app.models.GroupData;

public class GroupListFragment extends Fragment implements OnListFragmentInteractionListener {

    private OnListFragmentInteractionListener mListener;

    public GroupListFragment() {
    }

    public static GroupListFragment newInstance() {
        return new GroupListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_item, container, false);
        Context context = view.getContext();

        RecyclerView groupsRecyclerView = view.findViewById(R.id.rv_groups);

        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        groupsRecyclerView.setAdapter(new GroupRecyclerAdapter(getDummyGroupData(), mListener));

        return view;
    }

    private ArrayList<GroupData> getDummyGroupData() {
        ArrayList<GroupData> groupDataList = new ArrayList<>();
        final GroupData groupData = new GroupData();
        groupData.setIconUrl("http://icons.iconarchive.com/icons/hopstarter/orb/256/Counter-Strike-icon.png");
        groupData.setTitle("Counter Strike");
        groupData.setSubTitle("Do you have what it takes?");
        groupDataList.add(groupData);
        groupDataList.add(groupData);
        groupDataList.add(groupData);
        groupDataList.add(groupData);
        return groupDataList;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = this;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListFragmentInteraction(GroupData groupData) {
        final Intent intent = new Intent(getContext(), ChatActivity.class);
        startActivity(intent);
    }

}
