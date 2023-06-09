package in.lubble.app.groups;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ActionMode;

/**
 * Created by ishaan on 28/1/18.
 */

public interface OnListFragmentInteractionListener {

    void onListFragmentInteraction(String groupId, boolean isJoining);

    void onDmClick(String dmId, String name, String thumbnailUrl);

    void onSearched(int resultSize);

    ActionMode onActionModeEnabled(@NonNull ActionMode.Callback callback);

}
