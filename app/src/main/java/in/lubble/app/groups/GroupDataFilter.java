package in.lubble.app.groups;

import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

import in.lubble.app.models.GroupData;

public class GroupDataFilter extends Filter {

    private final GroupRecyclerAdapter adapter;
    private final List<GroupData> originalList;
    private final List<GroupData> filteredList;

    public GroupDataFilter(GroupRecyclerAdapter adapter, List<GroupData> groupDataListst) {
        this.adapter = adapter;
        this.originalList = new ArrayList<>(groupDataListst);
        this.filteredList = new ArrayList<>();
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        filteredList.clear();
        final FilterResults results = new FilterResults();

        if (charSequence.length() == 0) {
            filteredList.addAll(originalList);
        } else {
            final String filterPattern = charSequence.toString().toLowerCase().trim();
            for (GroupData groupData : originalList) {
                if (groupData != null && groupData.getTitle().toLowerCase().contains(filterPattern)) {
                    filteredList.add(groupData);
                }
            }
        }

        results.values = filteredList;
        results.count = filteredList.size();
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        //adapter.clearGroups();
        if (filteredList.size() == 0) {
            return;
        }
        adapter.replaceAll(filteredList);
        /*adapter.clearGroups();
        for (GroupData groupData : filteredList) {
            adapter.addGroupToTop(groupData);
        }*/
    }

}
