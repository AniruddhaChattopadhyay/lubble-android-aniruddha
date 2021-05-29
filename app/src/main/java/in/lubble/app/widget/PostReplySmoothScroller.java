package in.lubble.app.widget;

import android.content.Context;

import androidx.recyclerview.widget.LinearSmoothScroller;

public class PostReplySmoothScroller extends LinearSmoothScroller {

    public PostReplySmoothScroller(Context context) {
        super(context);
    }

    @Override
    public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
        return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + ((viewEnd - viewStart) * 2 / 3));
    }
}