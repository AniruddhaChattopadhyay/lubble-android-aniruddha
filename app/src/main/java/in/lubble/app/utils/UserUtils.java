package in.lubble.app.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.auth.LoginActivity;
import in.lubble.app.chat.GroupPromptSharedPrefs;
import in.lubble.app.models.ProfileData;
import in.lubble.app.notifications.GroupMappingSharedPrefs;
import in.lubble.app.notifications.KeyMappingSharedPrefs;
import in.lubble.app.notifications.SnoozedGroupsSharedPrefs;
import in.lubble.app.notifications.UnreadChatsSharedPrefs;
import in.lubble.app.quiz.AnswerSharedPrefs;
import in.lubble.app.services.FeedServices;
import io.branch.referral.Branch;

import static in.lubble.app.firebase.RealtimeDbHelper.getThisUserRef;
import static in.lubble.app.groups.GroupListFragment.USER_INIT_LOGOUT_ACTION;

/**
 * Created by ishaangarg on 12/11/17.
 */

public class UserUtils {

    public static boolean isNewUser(FirebaseUser currentUser) {
        FirebaseUserMetadata metadata = currentUser.getMetadata();
        return (metadata != null &&
                metadata.getCreationTimestamp() <= metadata.getLastSignInTimestamp() + 100
                && metadata.getCreationTimestamp() >= metadata.getLastSignInTimestamp() - 100
        );
    }

    public static void logout(@NonNull final FragmentActivity activity) {
        try {
            Intent intent = new Intent(USER_INIT_LOGOUT_ACTION);
            LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);

            final ProgressDialog progressDialog = new ProgressDialog(activity);
            progressDialog.setMessage(activity.getString(R.string.logging_out));
            progressDialog.setCancelable(false);
            progressDialog.show();

            getThisUserRef().child("token").setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        AuthUI.getInstance()
                                .signOut(activity)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (!activity.isFinishing() && !activity.isDestroyed()) {
                                            progressDialog.dismiss();
                                            // user is now signed out
                                            LubbleSharedPrefs.getInstance().clearAll();
                                            GroupPromptSharedPrefs.getInstance().clearAll();
                                            UnreadChatsSharedPrefs.getInstance().clearAll();
                                            SnoozedGroupsSharedPrefs.getInstance().clearAll();
                                            KeyMappingSharedPrefs.getInstance().clearAll();
                                            GroupMappingSharedPrefs.getInstance().clearAll();
                                            AnswerSharedPrefs.getInstance().clearAll();
                                            FeedServices.clearAll();
                                            Branch.getInstance().logout();
                                            Intent intent = new Intent(activity, LoginActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            activity.startActivity(intent);
                                            activity.finishAffinity();
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(activity, R.string.check_internet, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    public static void logout(@NonNull final Context context) {
        try {
            final ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(context.getString(R.string.logging_out));
            progressDialog.setCancelable(false);
            progressDialog.show();
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
                                        GroupPromptSharedPrefs.getInstance().clearAll();
                                        UnreadChatsSharedPrefs.getInstance().clearAll();
                                        SnoozedGroupsSharedPrefs.getInstance().clearAll();
                                        KeyMappingSharedPrefs.getInstance().clearAll();
                                        GroupMappingSharedPrefs.getInstance().clearAll();
                                        AnswerSharedPrefs.getInstance().clearAll();
                                        FeedServices.clearAll();
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
            FirebaseCrashlytics.getInstance().recordException(e);
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

    @Nullable
    public static String getUserPhone(ProfileData profileData) {
        if (!TextUtils.isEmpty(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())) {
            return FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        } else {
            return profileData.getPhone();
        }
    }

    public static boolean isAdminAccount() {
        String supportUid = LubbleSharedPrefs.getInstance().getSupportUid();
        String currUid = FirebaseAuth.getInstance().getUid();
        return currUid != null & supportUid != null && currUid.equalsIgnoreCase(supportUid);
    }

}
