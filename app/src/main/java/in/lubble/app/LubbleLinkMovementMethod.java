package in.lubble.app;

import android.content.Context;
import android.text.Spannable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;

import me.saket.bettermovementmethod.BetterLinkMovementMethod;

public class LubbleLinkMovementMethod extends BetterLinkMovementMethod {

    private static LubbleLinkMovementMethod singleInstance;
    private OnDoubleClickListener onLinkDoubleClickListener;
    private Context context;

    /**
     * Return a new instance of LubbleLinkMovementMethod.
     */
    public static LubbleLinkMovementMethod newInstance(Context context) {
        return new LubbleLinkMovementMethod(context);
    }

    public static LubbleLinkMovementMethod getInstance(Context context) {
        if (singleInstance == null) {
            singleInstance = new LubbleLinkMovementMethod(context);
        }
        return singleInstance;
    }

    protected LubbleLinkMovementMethod(Context context) {
        this.context = context;
    }

    public interface OnDoubleClickListener {
        void onDoubleClick();
    }

    /**
     * Set a listener that will get called whenever any link is double-clicked on the TextView.
     */
    public LubbleLinkMovementMethod setOnLinkDoubleClickListener(OnDoubleClickListener doubleClickListener) {
        if (this == singleInstance) {
            throw new UnsupportedOperationException("Setting a double-click listener on the instance returned by getInstance() is not supported to avoid " +
                    "memory leaks. Please use newInstance() or any of the linkify() methods instead.");
        }

        this.onLinkDoubleClickListener = doubleClickListener;
        return this;
    }

    GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (onLinkDoubleClickListener != null) {
                onLinkDoubleClickListener.onDoubleClick();
            }
            return false;
        }
    });

    @Override
    public boolean onTouchEvent(TextView textView, Spannable text, MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(textView, text, event);
    }
}
