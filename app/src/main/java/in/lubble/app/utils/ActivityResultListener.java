package in.lubble.app.utils;

import android.content.Intent;

public interface ActivityResultListener {
    void onActivityResultForFrag(int requestCode, int resultCode, Intent data);
}
