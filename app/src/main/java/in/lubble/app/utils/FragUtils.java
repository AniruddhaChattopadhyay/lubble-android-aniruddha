package in.lubble.app.utils;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by ishaangarg on 11/11/17.
 */

public class FragUtils {

    public static void replaceFrag(FragmentManager fragmentManager, Fragment targetFrag, int viewId) {
        fragmentManager
                .beginTransaction()
                .replace(viewId, targetFrag)
                .commit();
    }

    public static void replaceStack(Context context, Fragment fragment, int viewId) {
        if (context != null) {
            FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack(fragmentManager.getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
            android.support.v4.app.FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(viewId, fragment);
            transaction.commit();
        }
    }

    public static void addFrag(FragmentManager fragmentManager, @IdRes int containerViewId, Fragment targetFrag) {
        fragmentManager
                .beginTransaction()
                .replace(containerViewId, targetFrag)
                .addToBackStack(null)
                .commit();
    }

}
