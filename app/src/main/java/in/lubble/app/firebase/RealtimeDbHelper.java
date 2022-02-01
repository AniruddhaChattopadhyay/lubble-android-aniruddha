package in.lubble.app.firebase;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import in.lubble.app.LubbleSharedPrefs;

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
                + "/lubbles/" + LubbleSharedPrefs.getInstance().requireLubbleId());
    }

    public static DatabaseReference getUserLubbleRef(String uid) {
        return FirebaseDatabase.getInstance().getReference("users/" + uid + "/lubbles/" + LubbleSharedPrefs.getInstance().requireLubbleId());
    }

    public static String getUserGroupPath() {
        return "users/" + FirebaseAuth.getInstance().getUid() + "/lubbles/" + LubbleSharedPrefs.getInstance().requireLubbleId() + "/groups";
    }

    public static String getLubbleGroupPath() {
        return "lubbles/" + LubbleSharedPrefs.getInstance().requireLubbleId() + "/groups";
    }

    public static DatabaseReference getUserGroupsRef() {
        return getUserLubbleRef().child("groups");
    }

    /**
     * DON'T USE getLubbleRef() -> LEADS TO WHOLE NODE BEING DOWNLOADED
     * VERY IMP TO NEVER USE IT, EVER.
     * Might as well break the whole app.
     * <p>
     * Instead, use child nodes like lubbleInfo, events, etc.
     */
    @Deprecated()
    public static DatabaseReference getLubbleRef() {
        return FirebaseDatabase.getInstance().getReference("lubbles/" + LubbleSharedPrefs.getInstance().requireLubbleId());
    }

    public static DatabaseReference getLubbleInfoRef() {
        return FirebaseDatabase.getInstance().getReference("lubbles/" + LubbleSharedPrefs.getInstance().requireLubbleId() + "/lubbleInfo");
    }

    public static DatabaseReference getLubbleInfoRef(String lubbleId) {
        return FirebaseDatabase.getInstance().getReference("lubbles/" + lubbleId + "/lubbleInfo");
    }

    public static DatabaseReference getLubbleDomesticRef() {
        return FirebaseDatabase.getInstance().getReference("lubbles/" + LubbleSharedPrefs.getInstance().requireLubbleId() + "/domesticDirectory");
    }

    /*
    Be Careful! getLubbleGroupsRef() will download the whole group node along with 1000s of members!
    User GroupInfo node instead to get just meta data of group.
    */
    @Deprecated
    public static DatabaseReference getLubbleGroupsRef() {
        return FirebaseDatabase.getInstance().getReference("lubbles/" + LubbleSharedPrefs.getInstance().requireLubbleId() + "/groups");
    }

    public static DatabaseReference getLubbleGroupInfoRef(String groupId) {
        return FirebaseDatabase.getInstance().getReference("lubbles/" + LubbleSharedPrefs.getInstance().requireLubbleId() + "/groups")
                .child(groupId).child("groupInfo");
    }

    public static DatabaseReference getLubbleBlocksRef() {
        return FirebaseDatabase.getInstance().getReference("lubbles/" + LubbleSharedPrefs.getInstance().requireLubbleId() + "/blocks");
    }

    public static DatabaseReference bulkJoinGroupV2Ref() {
        return FirebaseDatabase.getInstance().getReference("bulk_join_group/lubbles/" + LubbleSharedPrefs.getInstance().requireLubbleId()
                + "/users/" + FirebaseAuth.getInstance().getUid() + "/bulkJoin");
    }

    public static DatabaseReference getCreateOrJoinGroupRef() {
        return FirebaseDatabase.getInstance().getReference("create_join_group/lubbles/" + LubbleSharedPrefs.getInstance().requireLubbleId()
                + "/users/" + FirebaseAuth.getInstance().getUid());
    }

    public static DatabaseReference getMessagesRef() {
        return FirebaseDatabase.getInstance().getReference("messages/lubbles/" + LubbleSharedPrefs.getInstance().requireLubbleId() + "/groups");
    }

    public static DatabaseReference getAnnouncementsRef() {
        return FirebaseDatabase.getInstance().getReference("messages/lubbles/" + LubbleSharedPrefs.getInstance().requireLubbleId() + "/announcements");
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

    public static DatabaseReference getStoriesRef(String groupId) {
        return FirebaseDatabase.getInstance().getReference("stories/" + LubbleSharedPrefs.getInstance().requireLubbleId()).child(groupId);
    }

    public static DatabaseReference getAppInfoRef() {
        return FirebaseDatabase.getInstance().getReference("appInfo");
    }

    public static DatabaseReference getDevRef() {
        return FirebaseDatabase.getInstance().getReference("devs");
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

    public static DatabaseReference getUserDmsRef(String uid) {
        return getUserRef(uid).child("dms");
    }

    public static DatabaseReference getSellerRef() {
        return FirebaseDatabase.getInstance().getReference("sellers");
    }

    public static DatabaseReference getSellerRef(String uid) {
        return FirebaseDatabase.getInstance().getReference("sellers").child(uid);
    }

    public static DatabaseReference getSellerDmsRef() {
        return FirebaseDatabase.getInstance().getReference("sellers")
                .child(String.valueOf(LubbleSharedPrefs.getInstance().getSellerId())).child("dms");
    }

    public static DatabaseReference getSellerInfoRef(String uid) {
        return FirebaseDatabase.getInstance().getReference("sellers").child(uid).child("info");
    }

    public static DatabaseReference getQuizRefForThisUser(String quizName) {
        return FirebaseDatabase.getInstance().getReference("quiz").child(quizName).child(FirebaseAuth.getInstance().getUid());
    }

    public static DatabaseReference getSearchQueryRef() {
        return FirebaseDatabase.getInstance().getReference("search").child("queries");
    }

    public static DatabaseReference getSearchResultRef() {
        return FirebaseDatabase.getInstance().getReference("search").child("results");
    }

    public static DatabaseReference getThisUserFeedIntroRef() {
        return FirebaseDatabase.getInstance().getReference("feed_user").child(FirebaseAuth.getInstance().getUid()).child("is_intro_done");
    }

    @Nullable
    public static DatabaseReference getGroupTypingRef(String groupId, String dmId) {
        String chatId;
        chatId = groupId != null ? groupId : dmId;
        if (!TextUtils.isEmpty(chatId)) {
            return FirebaseDatabase.getInstance().getReference("typing").child(LubbleSharedPrefs.getInstance().getLubbleId()).child(chatId);
        } else {
            return null;
        }
    }

}
