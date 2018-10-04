package in.lubble.app.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;

import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.auth.LoginActivity;
import io.branch.referral.Branch;

import static in.lubble.app.utils.StringUtils.isValidString;

/**
 * Created by ishaangarg on 12/11/17.
 */

public class UserUtils {

    public static boolean isNewUser(FirebaseUser currentUser) {
        FirebaseUserMetadata metadata = currentUser.getMetadata();
        return (metadata != null && (metadata.getCreationTimestamp() == metadata.getLastSignInTimestamp()))
                || !isValidString(currentUser.getDisplayName()
        );
    }

    public static void logout(@NonNull final FragmentActivity activity) {
        final ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage(activity.getString(R.string.logging_out));
        progressDialog.show();
        AuthUI.getInstance()
                .signOut(activity)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        // user is now signed out
                        LubbleSharedPrefs.getInstance().clearAll();
                        Branch.getInstance().logout();
                        Intent intent = new Intent(activity, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        activity.startActivity(intent);
                        activity.finishAffinity();
                    }
                });
    }

    public static void setProfilePic(@NonNull Context context,
                                     @NonNull ImageView imageView,
                                     @NonNull String profileUrl) {
        GlideApp.with(context).load(profileUrl)
                .placeholder(R.drawable.ic_account_circle_black_no_padding)
                .circleCrop()
                .into(imageView);
    }

    public static String getLubbleId() {
        return LubbleSharedPrefs.getInstance().getLubbleId();
    }

}
