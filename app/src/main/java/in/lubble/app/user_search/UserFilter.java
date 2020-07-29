package in.lubble.app.user_search;

import android.widget.Filter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import in.lubble.app.models.ProfileInfo;

/**
 * Created by ishaan on 25/3/18.
 */

public class UserFilter extends Filter {

    private final UserAdapter adapter;
    private final List<ProfileInfo> originalList;
    private final List<ProfileInfo> filteredList;

    UserFilter(UserAdapter userAdapter, List<ProfileInfo> membersList) {
        super();
        this.adapter = userAdapter;
        this.originalList = new LinkedList<>(membersList);
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
            for (ProfileInfo profileInfo : originalList) {
                if (profileInfo.getName().toLowerCase().contains(filterPattern)) {
                    filteredList.add(profileInfo);
                }
            }
        }

        results.values = filteredList;
        results.count = filteredList.size();
        return results;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        adapter.clear();
        adapter.addAllMembers((ArrayList<ProfileInfo>) filterResults.values);
        adapter.notifyDataSetChanged();
    }
}
