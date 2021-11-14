package in.lubble.app.feed_groups.SingleGroupFeed;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.MissingFormatArgumentException;

import in.lubble.app.BaseActivity;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.feed_bottom_sheet.FeedUserShareBottomSheetFrag;
import in.lubble.app.feed_groups.FeedExploreActiv;
import in.lubble.app.models.FeedGroupData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.services.FeedServices;
import in.lubble.app.utils.UiUtils;
import io.getstream.client.Feed;
import io.getstream.cloud.CloudFlatFeed;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static in.lubble.app.Constants.MEDIA_TYPE;

public class GroupFeedActivity extends BaseActivity {

    private static final String TAG = "GroupFeedActivity";
    private static final String EXTRA_FEED_GROUP_DATA = "LBL_EXTRA_FEED_GROUP_DATA";
    private FeedGroupData feedGroupData;
    private boolean isJoined;
    private SingleGroupFeed singleGroupFeed;
    private ProgressDialog sharingProgressDialog;
    private String sharingUrl;
    private MaterialButton joinInviteBtn;

    public static void open(Context context,String feedGroupName){
        Log.d(TAG, "2nd Open function called");
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        endpoints.getFeedGroupInfo(feedGroupName).enqueue(new Callback<FeedGroupData>() {
            @Override
            public void onResponse(Call<FeedGroupData> call, Response<FeedGroupData> response) {
                FeedGroupData feedGroupData = response.body();
                Intent intent = new Intent(context, GroupFeedActivity.class);
                intent.putExtra(EXTRA_FEED_GROUP_DATA, feedGroupData);
                context.startActivity(intent);
            }

            @Override
            public void onFailure(Call<FeedGroupData> call, Throwable t) {

            }
        });
    }
    public static void open(Context context, FeedGroupData feedGroupData) {
        Intent intent = new Intent(context, GroupFeedActivity.class);
        intent.putExtra(EXTRA_FEED_GROUP_DATA, feedGroupData);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_feed);
        Toolbar toolbar = findViewById(R.id.toolbar);
        joinInviteBtn = findViewById(R.id.btn_join_invite);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("");

        if (getIntent().hasExtra(EXTRA_FEED_GROUP_DATA)) {
            feedGroupData = (FeedGroupData) getIntent().getSerializableExtra(EXTRA_FEED_GROUP_DATA);
            CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar_feed_group);
            collapsingToolbarLayout.setTitle(feedGroupData.getName());
            ImageView imageView = findViewById(R.id.collapsing_toolbar_feed_group_background);
            GlideApp.with(this)
                    .asBitmap()
                    .load(feedGroupData.getPhotoUrl())
                    .error(R.drawable.ic_circle_group_24dp)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            Palette.from(resource)
                                    .maximumColorCount(8)
                                    .addFilter(UiUtils.DEFAULT_FILTER)
                                    .generate(p -> {
                                        // Use generated instance
                                        Drawable normalDrawable = ContextCompat.getDrawable(GroupFeedActivity.this, R.drawable.rounded_rect_gray);
                                        if (normalDrawable != null && p != null) {
                                            Drawable wrapDrawable = DrawableCompat.wrap(normalDrawable);
                                            DrawableCompat.setTint(wrapDrawable, p.getDominantColor(ContextCompat.getColor(GroupFeedActivity.this, R.color.fb_color)));
                                            DrawableCompat.setTintMode(wrapDrawable, PorterDuff.Mode.MULTIPLY);
                                            imageView.setBackground(wrapDrawable);
                                        }
                                        imageView.setImageBitmap(resource);
                                    });
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
            singleGroupFeed = SingleGroupFeed.newInstance(feedGroupData.getFeedName());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, singleGroupFeed)
                    .commitNow();
        } else {
            throw new MissingFormatArgumentException("no EXTRA_FEED_NAME passed while opening GroupFeedActivity");
        }
    }

    void toggleContextMenu(boolean isJoined) {
        this.isJoined = isJoined;
        joinInviteBtn.setVisibility(View.VISIBLE);
        String joinText = isJoined ? "Invite" : "Join Group";
        Drawable icon = isJoined ? ContextCompat.getDrawable(this, R.drawable.ic_person_add_24dp) : ContextCompat.getDrawable(this, R.drawable.ic_add_circle_black_24dp);
        joinInviteBtn.setIcon(icon);
        joinInviteBtn.setText(joinText);
        joinInviteBtn.setOnClickListener(v -> {
            if (isJoined) {
                FeedUserShareBottomSheetFrag feedUserShareBottomSheetFrag = new FeedUserShareBottomSheetFrag(feedGroupData.getFeedName(), feedGroupData);
                feedUserShareBottomSheetFrag.show(getSupportFragmentManager(), feedUserShareBottomSheetFrag.getTag());
                Bundle bundle = new Bundle();
                bundle.putInt("group_id", feedGroupData.getId());
                bundle.putString("group_name", feedGroupData.getName());
                Analytics.triggerEvent(AnalyticsEvents.FEED_GROUP_INVITE_CLICKED, bundle, this);
            } else {
                CloudFlatFeed groupFeed = FeedServices.client.flatFeed("group", feedGroupData.getFeedName());
                singleGroupFeed.joinGroup(groupFeed);
            }
        });
        if (isJoined)
            invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int menuId = R.menu.feed_group_menu;
        getMenuInflater().inflate(menuId, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (isJoined) {
            menu.add(0, R.id.leave, 0, "Leave Group");
        } else {
            menu.removeItem(R.id.leave);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            // Respond to the action bar's Up/Home button
            onBackPressed();
            return true;
        } else if (itemId == R.id.leave) {
            leaveGroup();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void leaveGroup() {
        leaveGroupConfirmation((dialog, which) -> {
            final JSONObject jsonObject = new JSONObject();
            View rootView = findViewById(R.id.container);
            try {
                jsonObject.put("groupFeedId", feedGroupData.getFeedName());
                RequestBody body = RequestBody.create(MEDIA_TYPE, jsonObject.toString());
                Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
                Call<Void> call = endpoints.deleteGroupForUser(body);
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful() && !isFinishing()) {
                            Toast.makeText(GroupFeedActivity.this, "Left Group!", Toast.LENGTH_SHORT).show();
                            onBackPressed();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        if (!isFinishing())
                            Snackbar.make(rootView, "Failed: " + t.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
                FirebaseCrashlytics.getInstance().recordException(e);
                Snackbar.make(rootView, "Failed: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void leaveGroupConfirmation(DialogInterface.OnClickListener listener) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Leave " + feedGroupData.getName() + "?")
                .setMessage("Are you sure you want to leave this group?\n\nPosts from this group will no longer appear in your home feed")
                .setIcon(ContextCompat.getDrawable(this, R.drawable.ic_warning_yellow_24dp))
                .setPositiveButton(R.string.leave_group_title, listener)
                .setNegativeButton(R.string.all_cancel, (dialog, which) -> dialog.cancel()).show();
    }


}