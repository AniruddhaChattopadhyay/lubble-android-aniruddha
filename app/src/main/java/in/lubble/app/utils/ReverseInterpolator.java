package in.lubble.app.utils;

import android.view.animation.Interpolator;

public class ReverseInterpolator implements Interpolator {
    @Override
    public float getInterpolation(float paramFloat) {
        return Math.abs(paramFloat - 1f);
    }
}
