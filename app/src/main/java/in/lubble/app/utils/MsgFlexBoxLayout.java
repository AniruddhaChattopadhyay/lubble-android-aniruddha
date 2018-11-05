package in.lubble.app.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import in.lubble.app.R;

/**
 * Created by ishaan on 22/4/18.
 */

public class MsgFlexBoxLayout extends RelativeLayout {
    private View viewPartMain;
    private View viewPartSlave;

    private TypedArray a;

    private RelativeLayout.LayoutParams viewPartMainLayoutParams;
    private int viewPartMainWidth;
    private int viewPartMainHeight;

    private RelativeLayout.LayoutParams viewPartSlaveLayoutParams;
    private int viewPartSlaveWidth;
    private int viewPartSlaveHeight;


    public MsgFlexBoxLayout(Context context) {
        super(context);
    }

    public MsgFlexBoxLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        a = context.obtainStyledAttributes(attrs, R.styleable.MsgFlexboxLayout, 0, 0);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        try {
            viewPartMain = this.findViewById(a.getResourceId(R.styleable.MsgFlexboxLayout_viewPartMain, -1));
            viewPartSlave = this.findViewById(a.getResourceId(R.styleable.MsgFlexboxLayout_viewPartSlave, -1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (viewPartMain == null || viewPartSlave == null || widthSize <= 0) {
            return;
        }

        int availableWidth = widthSize - getPaddingLeft() - getPaddingRight();
        int availableHeight = heightSize - getPaddingTop() - getPaddingBottom();

        viewPartMainLayoutParams = (LayoutParams) viewPartMain.getLayoutParams();
        viewPartMainWidth = viewPartMain.getMeasuredWidth() + viewPartMainLayoutParams.leftMargin + viewPartMainLayoutParams.rightMargin;
        viewPartMainHeight = viewPartMain.getMeasuredHeight() + viewPartMainLayoutParams.topMargin + viewPartMainLayoutParams.bottomMargin;

        viewPartSlaveLayoutParams = (LayoutParams) viewPartSlave.getLayoutParams();
        viewPartSlaveWidth = viewPartSlave.getMeasuredWidth() + viewPartSlaveLayoutParams.leftMargin + viewPartSlaveLayoutParams.rightMargin;
        viewPartSlaveHeight = viewPartSlave.getMeasuredHeight() + viewPartSlaveLayoutParams.topMargin + viewPartSlaveLayoutParams.bottomMargin;

        final TextView textView = viewPartMain.findViewById(R.id.tv_message);
        int viewPartMainLineCount = textView.getLineCount();
        float viewPartMainLastLineWitdh = viewPartMainLineCount > 0 ? textView.getLayout().getLineWidth(viewPartMainLineCount - 1) : 0;

        widthSize = getPaddingLeft() + getPaddingRight();
        heightSize = getPaddingTop() + getPaddingBottom();

        if (viewPartMainLineCount > 1 && !(viewPartMainLastLineWitdh + viewPartSlaveWidth >= viewPartMain.getMeasuredWidth())) {
            widthSize += viewPartMainWidth;
            heightSize += viewPartMainHeight;
        } else if (viewPartMainLineCount > 1 && (viewPartMainLastLineWitdh + viewPartSlaveWidth >= availableWidth)) {
            widthSize += viewPartMainWidth;
            heightSize += viewPartMainHeight + viewPartSlaveHeight;
        } else if (viewPartMainLineCount == 1 && (viewPartMainWidth + viewPartSlaveWidth >= availableWidth)) {
            widthSize += viewPartMain.getMeasuredWidth();
            heightSize += viewPartMainHeight + viewPartSlaveHeight;
        } else {
            if (viewPartMainWidth + viewPartSlaveWidth >= availableWidth) {
                widthSize += viewPartMain.getMeasuredWidth();
                heightSize += viewPartMainHeight + viewPartSlaveHeight;
            } else {
                widthSize += viewPartMainWidth + viewPartSlaveWidth;
                heightSize += viewPartMainHeight;
            }
        }

        this.setMeasuredDimension(widthSize, heightSize);
        super.onMeasure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (viewPartMain == null || viewPartSlave == null) {
            return;
        }

        viewPartMain.layout(
                getPaddingLeft(),
                getPaddingTop(),
                viewPartMain.getWidth() + getPaddingLeft(),
                viewPartMain.getHeight() + getPaddingTop());

        viewPartSlave.layout(
                right - left - viewPartSlaveWidth - getPaddingRight(),
                bottom - top - getPaddingBottom() - viewPartSlaveHeight,
                right - left - getPaddingRight(),
                bottom - top - getPaddingBottom());
    }
}