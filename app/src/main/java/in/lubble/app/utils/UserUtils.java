package in.lubble.app.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import com.crashlytics.android.Crashlytics;
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

import static in.lubble.app.firebase.RealtimeDbHelper.getThisUserRef;

/**
 * Created by ishaangarg on 12/11/17.
 */

public class UserUtils {

    public static boolean isNewUser(FirebaseUser currentUser) {
        FirebaseUserMetadata metadata = currentUser.getMetadata();
        return (metadata != null &&
                (metadata.getCreationTimestamp() <= metadata.getLastSignInTimestamp() + 100
                        || metadata.getCreationTimestamp() >= metadata.getLastSignInTimestamp() - 100)
        );
    }

    public static void logout(@NonNull final FragmentActivity activity) {
        final ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage(activity.getString(R.string.logging_out));
        progressDialog.show();
        try {
            getThisUserRef().child("token").setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
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
                    } else {
                        Toast.makeText(activity, R.string.check_internet, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    public static void logout(@NonNull final Context context) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(R.string.logging_out));
        progressDialog.show();
        try {
            getThisUserRef().child("token").setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        AuthUI.getInstance()
                                .signOut(context)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progressDialog.dismiss();
                                        // user is now signed out
                                        LubbleSharedPrefs.getInstance().clearAll();
                                        Branch.getInstance().logout();
                                        Intent intent = new Intent(context, LoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        context.startActivity(intent);
                                    }
                                });
                    } else {
                        Toast.makeText(context, R.string.check_internet, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    public static void setProfilePic(@NonNull Context context,
                                     @NonNull ImageView imageView,
                                     @NonNull String profileUrl) {
        GlideApp.with(context).load(profileUrl)
                .placeholder(R.drawable.ic_account_circle_black_no_padding)
                .circleCrop()
                .into(imageView);
    }

}
