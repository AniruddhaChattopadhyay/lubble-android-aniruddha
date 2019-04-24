package in.lubble.app.quiz;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
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
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.FileProvider;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import in.lubble.app.*;
import in.lubble.app.BuildConfig;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.utils.FileUtils;
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
import static in.lubble.app.quiz.QuizResultActivPermissionsDispatcher.fetchLastKnownLocationWithPermissionCheck;
import static in.lubble.app.quiz.QuizResultActivPermissionsDispatcher.shareScreenshotWithPermissionCheck;
import static in.lubble.app.utils.RoundedCornersTransformation.CornerType.ALL;

@RuntimePermissions
public class QuizResultActiv extends BaseActivity implements RetryQuizBottomSheet.OnQuizRetryListener {

    private static final String TAG = "QuizResultActiv";
    private static final int REQUEST_CODE_RETRY = 605;

    private ProgressBar progressBar;
    private Group group;
    private CardView cardView1;
    private CardView cardView2;
    private TextView cuisineNameTv;
    private TextView ambienceNameTv;
    private TextView cuisineEmojiTv;
    private TextView ambienceEmojiTv;
    private TextView ratingTv;
    private ImageView placePicIv;
    private ImageView closeIv;
    private TextView placeCaptionTv;
    private TextView placeNameTv;
    private LinearLayout retryContainer;
    private LinearLayout shareContainer;
    private LinearLayout ratingContainer;
    private ProgressBar shareProgressBar;
    private LinearLayout shareItemsContainer;

    public static void open(Context context) {
        context.startActivity(new Intent(context, QuizResultActiv.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        Analytics.triggerScreenEvent(this, this.getClass());

        progressBar = findViewById(R.id.progressbar_quiz_result);
        group = findViewById(R.id.group);
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
        retryContainer = findViewById(R.id.container_retry);
        shareContainer = findViewById(R.id.container_quiz_share);
        shareProgressBar = findViewById(R.id.progressbar_quiz_share);
        shareItemsContainer = findViewById(R.id.container_share_items);
        ratingContainer = findViewById(R.id.container_rating);
        closeIv = findViewById(R.id.iv_quiz_close);

        progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);

        fetchLastKnownLocationWithPermissionCheck(this);

        retryContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPlayAgainDialog();
            }
        });

        shareContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Analytics.triggerEvent(AnalyticsEvents.QUIZ_SHARE_CLICKED, QuizResultActiv.this);
                shareScreenshotWithPermissionCheck(QuizResultActiv.this);
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
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        final Bitmap screenShot = getScreenShot(rootView);
        final String path = FileUtils.saveImageInGallery(screenShot, "quiz_screenie_" + System.currentTimeMillis(), QuizResultActiv.this);
        Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", new File(path));
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");

        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, "");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        try {
            startActivity(Intent.createChooser(intent, "Share Screenshot"));
            shareProgressBar.setVisibility(View.VISIBLE);
            shareItemsContainer.setVisibility(View.GONE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No App Available", Toast.LENGTH_SHORT).show();
        }
    }

    public static Bitmap getScreenShot(View view) {
        View screenView = view.getRootView();
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);
        return bitmap;
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
                        GlideApp.with(QuizResultActiv.this)
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
                        group.setVisibility(View.VISIBLE);

                        animateCards();

                        RealtimeDbHelper.getQuizRefForThisUser("whereTonight").child("lastPlayedTime").setValue(System.currentTimeMillis());
                    } else if (!isFinishing()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(QuizResultActiv.this, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<PlaceData> call, Throwable t) {
                    Log.e(TAG, "onFailure: ");
                    if (!isFinishing()) {
                        Toast.makeText(QuizResultActiv.this, R.string.check_internet, Toast.LENGTH_SHORT).show();
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
        Analytics.triggerEvent(AnalyticsEvents.QUIZ_RETRY_CLICKED, this);
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
        shareProgressBar.setVisibility(View.GONE);
        shareItemsContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        QuizResultActivPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    void showRationaleForExtStorage(final PermissionRequest request) {

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage(getString(R.string.loc_perm_rationale_quiz));
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
