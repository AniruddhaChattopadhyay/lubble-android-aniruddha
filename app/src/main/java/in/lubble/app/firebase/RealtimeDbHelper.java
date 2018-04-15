package in.lubble.app.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static in.lubble.app.Constants.DEFAULT_LUBBLE;

/**
 * Created by ishaan on 30/1/18.
 */

public class RealtimeDbHelper {

    /**
     * ArrayList<String> userList = new ArrayList<>();
     * for (DataSnapshot child : dataSnapshot.getChildren()) {
     * userList.add(child.getKey());
     * }
     */

    public static DatabaseReference getThisUserRef() {
        return FirebaseDatabase.getInstance().getReference("users/" + FirebaseAuth.getInstance().getUid());
    }

    public static DatabaseReference getUserRef(String userId) {
        return FirebaseDatabase.getInstance().getReference("users").child(userId);
    }

    public static DatabaseReference getUserInfoRef(String userId) {
        return FirebaseDatabase.getInstance().getReference("users/" + userId + "/info");
    }

    public static DatabaseReference getUserLubbleRef() {
        return FirebaseDatabase.getInstance().getReference("users/" + FirebaseAuth.getInstance().getUid()
                + "/lubbles/" + DEFAULT_LUBBLE);
    }

    public static String getUserGroupPath() {
        return "users/" + FirebaseAuth.getInstance().getUid() + "/lubbles/" + DEFAULT_LUBBLE + "/groups";
    }

    public static String getLubbleGroupPath() {
        return "lubbles/" + DEFAULT_LUBBLE + "/groups";
    }

    public static DatabaseReference getUserGroupsRef() {
        return getUserLubbleRef().child("groups");
    }

    public static DatabaseReference getLubbleMembersRef() {
        return FirebaseDatabase.getInstance().getReference("lubbles/" + DEFAULT_LUBBLE + "/members");
    }

    public static DatabaseReference getLubbleDomesticRef() {
        return FirebaseDatabase.getInstance().getReference("lubbles/" + DEFAULT_LUBBLE + "/domesticDirectory");
    }

    public static DatabaseReference getLubbleGroupsRef() {
        return FirebaseDatabase.getInstance().getReference("lubbles/" + DEFAULT_LUBBLE + "/groups");
    }

    public static DatabaseReference getLubbleBlocksRef() {
        return FirebaseDatabase.getInstance().getReference("lubbles/" + DEFAULT_LUBBLE + "/blocks");
    }

    public static DatabaseReference getCreateOrJoinGroupRef() {
        return FirebaseDatabase.getInstance().getReference("create_join_group/lubbles/" + DEFAULT_LUBBLE
                + "/users/" + FirebaseAuth.getInstance().getUid());
    }

    public static DatabaseReference getMessagesRef() {
        return FirebaseDatabase.getInstance().getReference("messages/lubbles/" + DEFAULT_LUBBLE + "/groups");
    }

    public static DatabaseReference getAnnouncementsRef() {
        return FirebaseDatabase.getInstance().getReference("messages/lubbles/" + DEFAULT_LUBBLE + "/announcements");
    }

    public static DatabaseReference getConnectedInfoRef() {
        return FirebaseDatabase.getInstance().getReference(".info/connected");
    }

    public static DatabaseReference getPresenceRef() {
        return FirebaseDatabase.getInstance().getReference("presence");
    }

}
