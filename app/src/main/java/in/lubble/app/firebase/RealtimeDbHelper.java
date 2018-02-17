package in.lubble.app.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static in.lubble.app.Constants.DEFAULT_LUBBLE;

/**
 * Created by ishaan on 30/1/18.
 */

public class RealtimeDbHelper {

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

    public static DatabaseReference getUserGroupsRef() {
        return getUserLubbleRef().child("groups");
    }

    public static DatabaseReference getLubbleGroupsRef() {
        return FirebaseDatabase.getInstance().getReference("lubbles/" + DEFAULT_LUBBLE + "/groups");
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

}
