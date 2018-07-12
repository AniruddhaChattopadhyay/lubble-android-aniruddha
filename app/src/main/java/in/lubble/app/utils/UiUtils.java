package in.lubble.app.utils;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.design.widget.BottomSheetDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import in.lubble.app.R;

/**
 * Created by ishaangarg on 05/11/17.
 */

public class UiUtils {


    public static void hideKeyboard(Context ctx) {
        InputMethodManager inputManager = (InputMethodManager) ctx
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View v = ((Activity) ctx).getCurrentFocus();
        if (v == null)
            return;

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public static void animateFadeHide(Context context, View view) {
        if (view != null && view.getVisibility() == View.VISIBLE) {
            Animation animFadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out);

            view.startAnimation(animFadeOut);
            view.setVisibility(View.GONE);
        }
    }

    public static void animateFadeShow(Context context, View view) {
        if (view.getVisibility() != View.VISIBLE) {
            Animation animFadeIn = AnimationUtils.loadAnimation(context, R.anim.fade_in);

            view.startAnimation(animFadeIn);
            view.setVisibility(View.VISIBLE);
        }
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static void animateColor(final View view, @ColorInt int colorFrom, @ColorInt int colorTo) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(2000); // milliseconds
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                view.setBackgroundColor((int) animator.getAnimatedValue());
            }

        });
        colorAnimation.start();
    }

    public static BottomSheetDialog showBottomSheetAlert(Context context, LayoutInflater layoutInflater, String title, String subTitle, @DrawableRes int iconId, final View.OnClickListener listener) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View sheetView = layoutInflater.inflate(R.layout.bottom_sheet_info, null);
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.setCancelable(false);
        bottomSheetDialog.setCanceledOnTouchOutside(false);
        bottomSheetDialog.show();

        final TextView gotItTv = sheetView.findViewById(R.id.tv_got_it);
        final ImageView iconIv = sheetView.findViewById(R.id.iv_bottom_sheet_icon);
        final TextView titleTv = sheetView.findViewById(R.id.tv_bottom_sheet_title);
        final TextView subTitleTv = sheetView.findViewById(R.id.tv_bottom_sheet_subtitle);

        iconIv.setImageResource(iconId);
        titleTv.setText(title);
        subTitleTv.setText(subTitle);

        gotItTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                listener.onClick(v);
            }
        });
        return bottomSheetDialog;
    }

    public static void animateSlideDownHide(Context context, View view) {
        if (context != null && view.getVisibility() == View.VISIBLE) {
            Animation slideUp = AnimationUtils.loadAnimation(context, R.anim.slide_down_hide);
            slideUp.setDuration(500);
            view.startAnimation(slideUp);
            view.setVisibility(View.GONE);
        }
    }

    public static void animateSlideDownShow(Context context, View view) {
        if (context != null && view.getVisibility() != View.VISIBLE) {
            Animation slideDown = AnimationUtils.loadAnimation(context, R.anim.slide_down_show);
            slideDown.setDuration(500);
            view.startAnimation(slideDown);
            view.setVisibility(View.VISIBLE);
        }
    }


}
