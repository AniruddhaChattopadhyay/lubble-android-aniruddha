package in.lubble.app.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TouchableYoutubeFragment extends FrameLayout {

    private OnTouchedListener onTouchedListener;

    public TouchableYoutubeFragment(@NonNull Context context) {
        super(context);
    }

    public TouchableYoutubeFragment(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchableYoutubeFragment(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TouchableYoutubeFragment(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            onTouchedListener.onTouched();
        }
        return super.onInterceptTouchEvent(ev);
    }

    public OnTouchedListener getOnTouchedListener() {
        return onTouchedListener;
    }

    public void setOnTouchedListener(OnTouchedListener onTouchedListener) {
        this.onTouchedListener = onTouchedListener;
    }

    public interface OnTouchedListener {
        void onTouched();
    }

}
