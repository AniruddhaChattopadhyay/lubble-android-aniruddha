package in.lubble.app.feed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import in.lubble.app.R;

public class GroupSelectionFrag extends Fragment {

    private RecyclerView groupsRv;

    public static GroupSelectionFrag newInstance() {
        return new GroupSelectionFrag();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_group_selection, container, false);

        groupsRv = view.findViewById(R.id.rv_groups);

        groupsRv.setLayoutManager(new LinearLayoutManager(requireContext()));
        groupsRv.setAdapter(new GroupSelectionAdapter(getDummyStrList()));

        return view;
    }

    private List<String> getDummyStrList() {
        List<String> stringList = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            stringList.add(String.valueOf(i));
        }
        return stringList;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

}