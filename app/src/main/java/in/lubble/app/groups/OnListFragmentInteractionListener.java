package in.lubble.app.groups;

/**
 * Created by ishaan on 28/1/18.
 */

public interface OnListFragmentInteractionListener {

    void onListFragmentInteraction(String groupId, boolean isJoining);

    void onDmClick(String dmId, String name, String thumbnailUrl);

}
