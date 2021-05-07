package in.lubble.app.widget;


import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;

import in.lubble.app.feed_user.ReplyBottomSheetDialogFrag;

public class ReplyEditText extends AppCompatEditText {

    Fragment fragment;

    public ReplyEditText(Context context) {
        super(context);
    }

    public ReplyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ReplyEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setFrag(Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
            if (fragment != null && fragment instanceof ReplyBottomSheetDialogFrag) {
                ((ReplyBottomSheetDialogFrag) fragment).onBackPressed();
            }
            return true;
        }
        return super.onKeyPreIme(keyCode, event);
    }

}
