package in.lubble.app.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import in.lubble.app.LubbleSharedPrefs;

import static in.lubble.app.utils.UserUtils.getLubbleId;

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
                + "/lubbles/" + getLubbleId());
    }

    public static DatabaseReference getUserLubbleRef(String uid) {
        return FirebaseDatabase.getInstance().getReference("users/" + uid + "/lubbles/" + getLubbleId());
    }

    public static String getUserGroupPath() {
        return "users/" + FirebaseAuth.getInstance().getUid() + "/lubbles/" + getLubbleId() + "/groups";
    }

    public static String getLubbleGroupPath() {
        return "lubbles/" + getLubbleId() + "/groups";
    }

    public static DatabaseReference getUserGroupsRef() {
        return getUserLubbleRef().child("groups");
    }

    public static DatabaseReference getLubbleRef() {
        return FirebaseDatabase.getInstance().getReference("lubbles/" + getLubbleId());
    }

    public static DatabaseReference getLubbleMembersRef() {
        return FirebaseDatabase.getInstance().getReference("lubbles/" + getLubbleId() + "/members");
    }

    public static DatabaseReference getLubbleDomesticRef() {
        return FirebaseDatabase.getInstance().getReference("lubbles/" + getLubbleId() + "/domesticDirectory");
    }

    public static DatabaseReference getLubbleGroupsRef() {
        return FirebaseDatabase.getInstance().getReference("lubbles/" + getLubbleId() + "/groups");
    }

    public static DatabaseReference getLubbleBlocksRef() {
        return FirebaseDatabase.getInstance().getReference("lubbles/" + getLubbleId() + "/blocks");
    }

    public static DatabaseReference getCreateOrJoinGroupRef() {
        return FirebaseDatabase.getInstance().getReference("create_join_group/lubbles/" + getLubbleId()
                + "/users/" + FirebaseAuth.getInstance().getUid());
    }

    public static DatabaseReference getMessagesRef() {
        return FirebaseDatabase.getInstance().getReference("messages/lubbles/" + getLubbleId() + "/groups");
    }

    public static DatabaseReference getAnnouncementsRef() {
        return FirebaseDatabase.getInstance().getReference("messages/lubbles/" + getLubbleId() + "/announcements");
    }

    public static DatabaseReference getConnectedInfoRef() {
        return FirebaseDatabase.getInstance().getReference(".info/connected");
    }

    public static DatabaseReference getPresenceRef() {
        return FirebaseDatabase.getInstance().getReference("presence");
    }

    public static DatabaseReference getBackdoorRef() {
        return FirebaseDatabase.getInstance().getReference("backdoor");
    }

    public static DatabaseReference getAppInfoRef() {
        return FirebaseDatabase.getInstance().getReference("appInfo");
    }

    public static DatabaseReference getDevRef() {
        return FirebaseDatabase.getInstance().getReference("devs");
    }

    public static DatabaseReference getEventsRef() {
        return getLubbleRef().child("events");
    }

    /*
        For Marketplace
     */
    public static DatabaseReference getCreateDmRef() {
        return FirebaseDatabase.getInstance().getReference("create_dm");
    }

    public static DatabaseReference getDmMessagesRef() {
        return FirebaseDatabase.getInstance().getReference("messages/dms");
    }

    public static DatabaseReference getDmsRef() {
        return FirebaseDatabase.getInstance().getReference("dms");
    }

    public static DatabaseReference getUserDmsRef() {
        return getUserRef(FirebaseAuth.getInstance().getUid()).child("dms");
    }

    public static DatabaseReference getSellerRef() {
        return FirebaseDatabase.getInstance().getReference("sellers");
    }

    public static DatabaseReference getSellerDmsRef() {
        return FirebaseDatabase.getInstance().getReference("sellers")
                .child(String.valueOf(LubbleSharedPrefs.getInstance().getSellerId())).child("dms");
    }

    public static DatabaseReference getSellerInfoRef(String uid) {
        return FirebaseDatabase.getInstance().getReference("sellers").child(uid).child("info");
    }

}
