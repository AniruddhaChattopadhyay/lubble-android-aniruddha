package in.lubble.app.utils;

import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

/**
 * Created by ishaangarg on 11/11/17.
 */

public class FragUtils {

    /*public static void replaceFrag(AppCompatActivity appCompatActivity, Fragment targetFrag) {
        appCompatActivity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, targetFrag)
                .commit();
    }*/

    public static void addFrag(FragmentManager fragmentManager, @IdRes int containerViewId, Fragment targetFrag) {
        fragmentManager
                .beginTransaction()
                .replace(containerViewId, targetFrag)
                .addToBackStack(null)
                .commit();
    }

}
