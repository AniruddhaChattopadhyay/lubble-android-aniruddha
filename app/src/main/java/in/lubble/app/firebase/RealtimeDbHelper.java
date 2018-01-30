package in.lubble.app.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import static in.lubble.app.Constants.DEFAULT_LUBBLE;

/**
 * Created by ishaan on 30/1/18.
 */

public class RealtimeDbHelper {

    public static void foo() {
        FirebaseDatabase.getInstance().getReference("users/" + FirebaseAuth.getInstance().getUid()
                + "/lubbles/" + DEFAULT_LUBBLE + "/groups");
    }

}
