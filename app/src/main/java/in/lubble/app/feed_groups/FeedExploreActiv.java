package in.lubble.app.feed_groups;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import in.lubble.app.BaseActivity;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.models.FeedGroupData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.utils.FragUtils;
import in.lubble.app.utils.UiUtils;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static in.lubble.app.Constants.MEDIA_TYPE;
import static in.lubble.app.analytics.AnalyticsEvents.FEED_EXPLORE_CONTINUE_CLICKED;
import static in.lubble.app.analytics.AnalyticsEvents.FEED_EXPLORE_DIALOG_SHOWN;

public class FeedExploreActiv extends BaseActivity implements FeedGroupAdapter.OnListFragmentInteractionListener {

    private static final String IS_NEW_USER = "IS_NEW_USER";
    private static final String SHOW_MIGRATION_INFO = "SHOW_MIGRATION_INFO";

    private HashMap<String, Boolean> selectedGroupIdMap = new HashMap<>();
    private ImageView crossIv;
    private Button joinBtn;
    private boolean isNewUser, showMigrationInfo;

    public static Intent getIntent(Context context, boolean isNewUser, boolean showMigrationInfo) {
        final Intent intent = new Intent(context, FeedExploreActiv.class);
        intent.putExtra(IS_NEW_USER, isNewUser);
        intent.putExtra(SHOW_MIGRATION_INFO, showMigrationInfo);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        crossIv = findViewById(R.id.iv_cross);
        joinBtn = findViewById(R.id.btn_join);

        Analytics.triggerScreenEvent(this, this.getClass());

        isNewUser = getIntent().getBooleanExtra(IS_NEW_USER, false);
        showMigrationInfo = getIntent().getBooleanExtra(SHOW_MIGRATION_INFO, false);

        FragUtils.replaceFrag(getSupportFragmentManager(), FeedGroupsFrag.newInstance(), R.id.frag_container);

        if (showMigrationInfo) {
            UiUtils.showBottomSheetAlertLight(this, getLayoutInflater(),
                    FirebaseAuth.getInstance().getCurrentUser().getDisplayName()+", we've shifted a few things around here!",
                    "Welcome to Nearby Feed!" +
                            "\n\n\uD83C\uDFE1 You'll see posts from not just your neighbours, but from nearby neighbourhoods as well!" +
                            "\n\n\uD83D\uDCCD Groups in feed can also be joined by those in nearby neighbourhoods" +
                            "\n\nSo go ahead & join some groups. Posts from these groups will appear in your Feed!",
                    R.drawable.ic_namaste, "Continue", null
            );
        }

        joinBtn.setOnClickListener(v -> {
            joinBtn.setEnabled(false);
            Analytics.triggerEvent(FEED_EXPLORE_CONTINUE_CLICKED, FeedExploreActiv.this);

            ArrayList<String> groupNamesList = new ArrayList<>(selectedGroupIdMap.keySet());

            final ProgressDialog progressDialog = new ProgressDialog(FeedExploreActiv.this);
            progressDialog.setTitle("Joining " + selectedGroupIdMap.size() + " groups");
            progressDialog.setMessage("You're about to join a lovely & helpful community, please be respectful & enjoy :)");
            progressDialog.setCancelable(false);
            progressDialog.show();

            RequestBody body = RequestBody.create(MEDIA_TYPE, getParamsForGroupList(groupNamesList).toString());
            final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
            endpoints.batchFollowGroups(body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                    if (response.isSuccessful() && !isFinishing()) {
                        progressDialog.dismiss();
                        setResult(RESULT_OK);
                        finish();
                        overridePendingTransition(R.anim.none, R.anim.slide_to_bottom);
                        LubbleSharedPrefs.getInstance().setCheckIfFeedGroupJoined();
                    } else {
                        if (!isFinishing()) {
                            progressDialog.dismiss();
                            joinBtn.setEnabled(true);
                            Toast.makeText(FeedExploreActiv.this, response.message() == null ? getString(R.string.check_internet) : response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                    if (!isFinishing()) {
                        progressDialog.dismiss();
                        joinBtn.setEnabled(true);
                        Toast.makeText(FeedExploreActiv.this, t.getMessage() == null ? getString(R.string.check_internet) : t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        if (!isNewUser) {
            crossIv.setColorFilter(ContextCompat.getColor(this, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        crossIv.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.none, R.anim.slide_to_bottom);
        });

        LubbleSharedPrefs.getInstance().setIsExploreShown(true);
        Analytics.triggerEvent(FEED_EXPLORE_DIALOG_SHOWN, this);
    }

    @Override
    public void onListFragmentInteraction(String groupId, boolean isAdded) {
        if (isAdded) {
            selectedGroupIdMap.put(groupId, true);
            joinBtn.setVisibility(View.VISIBLE);

        } else {
            selectedGroupIdMap.remove(groupId);
            if (selectedGroupIdMap.size() == 0) {
                joinBtn.setVisibility(View.GONE);
            }
        }
    }

    private JSONObject getParamsForGroupList(ArrayList<String> groupIdList) {
        HashMap<String, Object> params = new HashMap<>();

        JSONArray groupIdArray = new JSONArray();
        for (String id : groupIdList) {
            groupIdArray.put(id);
        }

        params.put("group_list", groupIdArray);
        return new JSONObject(params);
    }

    @Override
    public void openGroup(FeedGroupData feedGroupData) {
        // no-op; already implemented in frag
    }

    @Override
    public void onBackPressed() {
        if (!isNewUser) {
            // allow old user to go back
            super.onBackPressed();
            overridePendingTransition(R.anim.none, R.anim.slide_to_bottom);
        }
    }
}
