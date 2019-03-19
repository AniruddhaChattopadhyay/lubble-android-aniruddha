package in.lubble.app.quiz;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import in.lubble.app.*;
import in.lubble.app.BuildConfig;
import in.lubble.app.R;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.utils.FileUtils;
import in.lubble.app.utils.ReverseInterpolator;
import in.lubble.app.utils.RoundedCornersTransformation;
import in.lubble.app.utils.UiUtils;
import okhttp3.RequestBody;
import org.json.JSONException;
import org.json.JSONObject;
import permissions.dispatcher.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;

import static in.lubble.app.Constants.MEDIA_TYPE;
import static in.lubble.app.quiz.QuizResultCamActivPermissionsDispatcher.fetchLastKnownLocationWithPermissionCheck;
import static in.lubble.app.quiz.QuizResultCamActivPermissionsDispatcher.shareScreenshotWithPermissionCheck;
import static in.lubble.app.utils.FileUtils.showStoragePermRationale;
import static in.lubble.app.utils.RoundedCornersTransformation.CornerType.ALL;

@RuntimePermissions
public class QuizResultCamActiv extends BaseActivity implements RetryQuizBottomSheet.OnQuizRetryListener {

    private static final String TAG = "QuizResultCamActiv";

    private ProgressBar progressBar;
    private ConstraintLayout mainContentContainer;
    private ConstraintLayout placeContentContainer;
    private CardView cardView1;
    private CardView cardView2;
    private TextView cuisineNameTv;
    private TextView ambienceNameTv;
    private TextView cuisineEmojiTv;
    private TextView ambienceEmojiTv;
    private TextView ratingTv;
    private ImageView placePicIv;
    private ImageView closeIv;
    private View flashView;
    private TextView placeCaptionTv;
    private TextView placeNameTv;
    private ImageView retryIv;
    private ImageView cameraIv;
    private LinearLayout ratingContainer;
    //private ProgressBar shareProgressBar;
    //private LinearLayout shareItemsContainer;

    public static void open(Context context) {
        context.startActivity(new Intent(context, QuizResultCamActiv.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result_cam);

        progressBar = findViewById(R.id.progressbar_quiz_result);
        mainContentContainer = findViewById(R.id.container_quiz_main);
        placeContentContainer = findViewById(R.id.container_place_result);
        cardView1 = findViewById(R.id.cardView);
        cardView2 = findViewById(R.id.cardview_2);
        cuisineNameTv = findViewById(R.id.tv_cuisine_name);
        ambienceNameTv = findViewById(R.id.tv_ambience_name);
        cuisineEmojiTv = findViewById(R.id.tv_cuisine_emoji);
        ambienceEmojiTv = findViewById(R.id.tv_ambience_emoji);
        placePicIv = findViewById(R.id.iv_place_pic);
        ratingTv = findViewById(R.id.tv_rating);
        placeNameTv = findViewById(R.id.tv_name);
        placeCaptionTv = findViewById(R.id.tv_caption);
        retryIv = findViewById(R.id.iv_quiz_retry);
        cameraIv = findViewById(R.id.iv_camera);
        //shareProgressBar = findViewById(R.id.progressbar_quiz_share);
        //shareItemsContainer = findViewById(R.id.container_share_items);
        ratingContainer = findViewById(R.id.container_rating);
        closeIv = findViewById(R.id.iv_quiz_close);
        flashView = findViewById(R.id.view_flash);
        mainContentContainer.setDrawingCacheEnabled(true);

        progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);

        fetchLastKnownLocationWithPermissionCheck(this);

        retryIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPlayAgainDialog();
            }
        });

        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void shareScreenshot() {
        animateFlash();
    }

    private void animateFlash() {
        Animation animFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale);
        animFadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Animation animFadeOut = AnimationUtils.loadAnimation(QuizResultCamActiv.this, R.anim.fade_in_scale);
                animFadeOut.setInterpolator(new ReverseInterpolator());
                animFadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mainContentContainer.setBackground(ContextCompat.getDrawable(QuizResultCamActiv.this, R.drawable.gradient_purple));
                        final Bitmap screenShot = mainContentContainer.getDrawingCache();
                        final String path = FileUtils.saveImageInGallery(screenShot, "quiz_screenie_" + System.currentTimeMillis(), QuizResultCamActiv.this);
                        Uri uri = FileProvider.getUriForFile(QuizResultCamActiv.this, BuildConfig.APPLICATION_ID + ".fileprovider", new File(path));

                        final QuizSharePicDialogFrag quizSharePicDialogFrag = QuizSharePicDialogFrag.newInstance(uri);
                        quizSharePicDialogFrag.show(getSupportFragmentManager(), null);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                flashView.startAnimation(animFadeOut);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        flashView.startAnimation(animFadeIn);
        flashView.setVisibility(View.VISIBLE);
    }

    @SuppressLint("MissingPermission")
    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void fetchLastKnownLocation() {
        LocationServices.getFusedLocationProviderClient(this).getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            fetchResult(location);
                        } else {
                            fetchResultWithoutLocation();
                        }
                    }
                });
    }

    private void fetchResult(Location location) {
        final AnswerSharedPrefs prefs = AnswerSharedPrefs.getInstance();
        final int cuisineAnswerId = prefs.getPreferences().getInt("0", 0);
        String cuisineAnswerName = prefs.getPreferences().getString("0_name", "Drinks");
        cuisineEmojiTv.setText(cuisineAnswerName.substring(cuisineAnswerName.lastIndexOf(" ") + 1));
        cuisineAnswerName = cuisineAnswerName.substring(0, cuisineAnswerName.lastIndexOf(" "));
        cuisineNameTv.setText(cuisineAnswerName.trim().replaceAll(" ", "\n"));

        final int ambienceAnswerId = prefs.getPreferences().getInt("1", 0);
        String ambienceAnswerName = prefs.getPreferences().getString("1_name", "Soft Music");
        ambienceEmojiTv.setText(ambienceAnswerName.substring(ambienceAnswerName.lastIndexOf(" ") + 1));
        ambienceAnswerName = ambienceAnswerName.substring(0, ambienceAnswerName.lastIndexOf(" "));
        ambienceNameTv.setText(ambienceAnswerName.trim().replaceAll(" ", "\n"));

        final CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(this);
        circularProgressDrawable.setStyle(CircularProgressDrawable.DEFAULT);
        circularProgressDrawable.start();

        final int budgetAnswerId = prefs.getPreferences().getInt("2", 0);
        prefs.clearAll();

        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("cuisine", cuisineAnswerId);
            jsonObject.put("ambience", ambienceAnswerId);
            jsonObject.put("budget", budgetAnswerId);
            jsonObject.put("lat", location.getLatitude());
            jsonObject.put("long", location.getLongitude());

            RequestBody body = RequestBody.create(MEDIA_TYPE, jsonObject.toString());

            final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
            endpoints.getQuizResult(body).enqueue(new Callback<PlaceData>() {
                @Override
                public void onResponse(Call<PlaceData> call, Response<PlaceData> response) {
                    if (response.isSuccessful() && !isFinishing()) {
                        progressBar.setVisibility(View.GONE);
                        final PlaceData placeData = response.body();
                        placeNameTv.setText(placeData.getName());
                        ratingTv.setText(String.valueOf(placeData.getRating()));

                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCornersTransformation(UiUtils.dpToPx(8), 0, ALL));
                        GlideApp.with(QuizResultCamActiv.this)
                                .load(placeData.getPic())
                                .placeholder(circularProgressDrawable)
                                .apply(requestOptions)
                                .into(placePicIv);

                        String caption = "";
                        if (!TextUtils.isEmpty(placeData.getType())) {
                            caption += placeData.getType();
                        }
                        if (placeData.getDistance() != 0 && placeData.getDistance() < 1000 * 10) {
                            caption += " · " + placeData.getDistanceString();
                        }
                        placeCaptionTv.setText(caption);
                        placeContentContainer.setVisibility(View.VISIBLE);

                        cameraIv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                shareScreenshotWithPermissionCheck(QuizResultCamActiv.this);
                            }
                        });

                        animateCards();

                        RealtimeDbHelper.getQuizRefForThisUser("whereTonight").child("lastPlayedTime").setValue(System.currentTimeMillis());
                    } else if (!isFinishing()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(QuizResultCamActiv.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<PlaceData> call, Throwable t) {
                    Log.e(TAG, "onFailure: ");
                    if (!isFinishing()) {
                        Toast.makeText(QuizResultCamActiv.this, R.string.check_internet, Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void animateCards() {
        RotateAnimation rotate = new RotateAnimation(0, -10, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        RotateAnimation rotate2 = new RotateAnimation(0, 10, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(500);
        rotate.setFillAfter(true);
        rotate2.setDuration(500);
        rotate2.setFillAfter(true);
        cardView1.startAnimation(rotate);
        cardView2.startAnimation(rotate2);

        final ScaleAnimation scaleAnimation = new ScaleAnimation(0, 1, 0, 1, 0.5f, 0.5f);
        scaleAnimation.setDuration(500);
        scaleAnimation.setFillAfter(true);
        ratingContainer.startAnimation(scaleAnimation);
    }

    private void fetchResultWithoutLocation() {
        final Location dummyLoc = new Location("dummy");
        dummyLoc.setLatitude(Constants.SVR_LATI);
        dummyLoc.setLongitude(Constants.SVR_LONGI);
        fetchResult(dummyLoc);
    }

    private void openPlayAgainDialog() {
        final RetryQuizBottomSheet retryQuizBottomSheet = RetryQuizBottomSheet.newInstance();
        retryQuizBottomSheet.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onSheetInteraction(int id) {
        if (id == RESULT_OK) {
            QuizOptionsActiv.open(this);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //shareProgressBar.setVisibility(View.GONE);
        //shareItemsContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        QuizResultCamActivPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    void showRationaleForExtStorage(final PermissionRequest request) {

        showStoragePermRationale(this, request);

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(getString(R.string.loc_perm_rationale));
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.all_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                request.proceed();
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.all_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                request.cancel();
            }
        });
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
    void showDeniedForQuizLoc() {
        fetchResultWithoutLocation();
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_FINE_LOCATION)
    void showNeverAskForQuizLoc() {
        Toast.makeText(this, R.string.quiz_loc_perm_never_text, Toast.LENGTH_LONG).show();
    }

}
