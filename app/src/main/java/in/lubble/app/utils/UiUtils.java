package in.lubble.app.utils;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;

import com.crashlytics.android.Crashlytics;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.io.FileOutputStream;

import in.lubble.app.R;

/**
 * Created by ishaangarg on 05/11/17.
 */

public class UiUtils {


    public static void hideKeyboard(final Context ctx) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) ctx
                        .getSystemService(Context.INPUT_METHOD_SERVICE);

                // check if no view has focus:
                View v = ((Activity) ctx).getCurrentFocus();
                if (v == null)
                    return;

                inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });
    }

    public static void showKeyboard(Context ctx, IBinder windowToken) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInputFromWindow(
                windowToken, InputMethodManager.SHOW_FORCED, 0);
    }

    public static void animateFadeHide(Context context, final View view) {
        if (view != null && view.getVisibility() == View.VISIBLE) {
            Animation animFadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out);
            animFadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    view.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            view.startAnimation(animFadeOut);
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

    public static BottomSheetDialog showBottomSheetAlertLight(Context context, LayoutInflater layoutInflater, String title, @Nullable String subtitle, @DrawableRes int iconId, String btnText, final View.OnClickListener listener) {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View sheetView = layoutInflater.inflate(R.layout.bottom_sheet_alert_light, null);
        bottomSheetDialog.setContentView(sheetView);
        bottomSheetDialog.show();

        final MaterialButton gotItBtn = sheetView.findViewById(R.id.btn_got_it);
        final ImageView iconIv = sheetView.findViewById(R.id.iv_bottom_sheet_icon);
        final TextView titleTv = sheetView.findViewById(R.id.tv_bottom_sheet_title);
        final TextView subtitleTv = sheetView.findViewById(R.id.tv_bottom_sheet_subtitle);

        iconIv.setImageResource(iconId);
        titleTv.setText(title);
        if (!TextUtils.isEmpty(subtitle)) {
            subtitleTv.setVisibility(View.VISIBLE);
            subtitleTv.setText(subtitle);
        } else {
            subtitleTv.setVisibility(View.GONE);
        }
        gotItBtn.setText(btnText);

        gotItBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
                listener.onClick(v);
            }
        });
        return bottomSheetDialog;
    }

    public static void animateRotate(Context context, View view, int degree) {
        if (context != null && view.getVisibility() == View.VISIBLE) {
            Animation slideUp = AnimationUtils.loadAnimation(context, R.anim.slide_down_hide);
            slideUp.setDuration(500);
            view.startAnimation(slideUp);
            view.setVisibility(View.GONE);
        }

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

    public static void animateFlipHide(Context context, View view) {
        if (context != null && view.getVisibility() == View.VISIBLE) {
            AnimatorSet shrinkSet = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.shrink_to_middle);
            shrinkSet.setTarget(view);
            shrinkSet.start();
            view.setVisibility(View.GONE);
        }
    }

    public static void animateFlipShow(Context context, View view) {
        if (context != null && view.getVisibility() != View.VISIBLE) {
            AnimatorSet growSet = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.grow_from_middle);
            growSet.setTarget(view);
            growSet.start();
            view.setVisibility(View.VISIBLE);
        }
    }

    public static File compressImage(String ogFilepath) {
        try {
            String exifOrientation = new ExifInterface(ogFilepath).getAttribute(ExifInterface.TAG_ORIENTATION);
            Bitmap bmp = BitmapFactory.decodeFile(ogFilepath);
            File outFile = new File(ogFilepath);
            if (outFile.length() / 1024 > 50) {
                FileOutputStream outStream = new FileOutputStream(outFile);
                bmp.compress(Bitmap.CompressFormat.JPEG, 75, outStream);
                outStream.flush();
                outStream.close();

                if (exifOrientation != null) {
                    ExifInterface newExif = new ExifInterface(outFile.getPath());
                    newExif.setAttribute(ExifInterface.TAG_ORIENTATION, exifOrientation);
                    newExif.saveAttributes();
                }

            }
            return outFile;
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            return new File(ogFilepath);
        }
    }

    public static void animateSlideUpShow(Context context, View view) {
        if (context != null && view.getVisibility() != View.VISIBLE) {
            Animation slideUp = AnimationUtils.loadAnimation(context, R.anim.slide_up_show);
            slideUp.setDuration(500);
            view.startAnimation(slideUp);
            view.setVisibility(View.VISIBLE);
        }
    }

    public static final Palette.Filter DEFAULT_FILTER = new Palette.Filter() {
        private static final float BLACK_MAX_LIGHTNESS = 0.05F;
        private static final float WHITE_MIN_LIGHTNESS = 0.95F;

        public boolean isAllowed(int rgb, float[] hsl) {
            return !this.isWhite(hsl) && !this.isBlack(hsl) && !this.isNearRedILine(hsl);
        }

        private boolean isBlack(float[] hslColor) {
            return hslColor[2] <= BLACK_MAX_LIGHTNESS;
        }

        private boolean isWhite(float[] hslColor) {
            return hslColor[2] >= WHITE_MIN_LIGHTNESS;
        }

        private boolean isNearRedILine(float[] hslColor) {
            return hslColor[0] >= 10.0F && hslColor[0] <= 37.0F && hslColor[1] <= 0.82F;
        }
    };

    public static void disableTabLayoutClicks(TabLayout tabLayout) {
        LinearLayout tabStrip = ((LinearLayout) tabLayout.getChildAt(0));
        for (int i = 0; i < tabStrip.getChildCount(); i++) {
            tabStrip.getChildAt(i).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
        }
    }

}
