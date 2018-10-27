package in.lubble.app;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import in.lubble.app.firebase.RealtimeDbHelper;

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";
    private boolean isActive;
    private ValueEventListener minAppEventListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            checkMinAppVersion();
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActive = true;
    }

    private void checkMinAppVersion() {
        // orderByKey to ensure we always check for hard stop firstm then evaluate the soft block
        minAppEventListener = RealtimeDbHelper.getAppInfoRef().orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if (child.getKey().equalsIgnoreCase("minApp")) {
                        Integer minAppVersion = child.getValue(Integer.class);
                        minAppVersion = minAppVersion == null ? 27 : minAppVersion;

                        if (BuildConfig.VERSION_CODE < minAppVersion && !isFinishing()) {
                            // block app
                            final AlertDialog alertDialog = new AlertDialog.Builder(BaseActivity.this).create();
                            alertDialog.setTitle(getString(R.string.update_dialog_title));
                            alertDialog.setMessage(getString(R.string.update_dialog_msg));
                            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.all_update), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    alertDialog.dismiss();
                                    final String appPackageName = getPackageName();
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                    } catch (ActivityNotFoundException anfe) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                    }
                                }
                            });
                            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.update_recheck), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    alertDialog.dismiss();
                                    checkMinAppVersion();
                                }
                            });
                            alertDialog.setCancelable(false);
                            alertDialog.show();
                            return;
                        }
                    } else if (child.getKey().equalsIgnoreCase("softMinApp") && BaseActivity.this instanceof MainActivity) {
                        Integer minAppVersion = child.getValue(Integer.class);
                        minAppVersion = minAppVersion == null ? 27 : minAppVersion;

                        if (BuildConfig.VERSION_CODE < minAppVersion && !isFinishing()) {
                            // prompt to optionally update app
                            final AlertDialog alertDialog = new AlertDialog.Builder(BaseActivity.this).create();
                            alertDialog.setTitle(getString(R.string.soft_update_dialog_title));
                            alertDialog.setMessage(getString(R.string.soft_update_dialog_msg));
                            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.all_update), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    alertDialog.dismiss();
                                    final String appPackageName = getPackageName();
                                    try {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                    } catch (ActivityNotFoundException anfe) {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                    }
                                }
                            });
                            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.all_later), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    alertDialog.dismiss();
                                }
                            });
                            alertDialog.setCancelable(false);
                            alertDialog.show();
                            return;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActive = false;
        if (minAppEventListener != null) {
            RealtimeDbHelper.getAppInfoRef().orderByKey().removeEventListener(minAppEventListener);
        }
    }
}
