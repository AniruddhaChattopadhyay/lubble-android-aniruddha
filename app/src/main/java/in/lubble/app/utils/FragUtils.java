package in.lubble.app.utils;

import android.content.Context;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

/**
 * Created by ishaangarg on 11/11/17.
 */

public class FragUtils {

    public static void replaceFrag(FragmentManager fragmentManager, Fragment targetFrag, int viewId) {
        fragmentManager
                .beginTransaction()
                .replace(viewId, targetFrag)
                .commitAllowingStateLoss();
    }
    public static void replaceFrag(FragmentManager fragmentManager, Fragment targetFrag, int viewId, String tag) {
        fragmentManager
                .beginTransaction()
                .replace(viewId, targetFrag, tag)
                .commitAllowingStateLoss();
    }

    public static void replaceStack(Context context, Fragment fragment, int viewId) {
        if (context != null) {
            FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack(fragmentManager.getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(viewId, fragment);
            transaction.commitAllowingStateLoss();
        }
    }

    public static void addFrag(FragmentManager fragmentManager, @IdRes int containerViewId, Fragment targetFrag) {
        fragmentManager
                .beginTransaction()
                .replace(containerViewId, targetFrag)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

}
