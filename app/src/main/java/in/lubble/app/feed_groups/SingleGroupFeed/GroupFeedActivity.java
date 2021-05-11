package in.lubble.app.feed_groups.SingleGroupFeed;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.MissingFormatArgumentException;

import in.lubble.app.BaseActivity;
import in.lubble.app.R;
import in.lubble.app.models.FeedGroupData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static in.lubble.app.Constants.MEDIA_TYPE;

public class GroupFeedActivity extends BaseActivity {

    private static final String EXTRA_FEED_GROUP_DATA = "LBL_EXTRA_FEED_GROUP_DATA";
    private FeedGroupData feedGroupData;
    public static void open(Context context, FeedGroupData feedGroupData) {
        Intent intent = new Intent(context, GroupFeedActivity.class);
        intent.putExtra(EXTRA_FEED_GROUP_DATA, feedGroupData);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_feed);

        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent().hasExtra(EXTRA_FEED_GROUP_DATA)) {
            feedGroupData = (FeedGroupData) getIntent().getSerializableExtra(EXTRA_FEED_GROUP_DATA);
            setTitle(feedGroupData.getName());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, SingleGroupFeed.newInstance(feedGroupData.getFeedName()))
                    .commitNow();
        } else {
            throw new MissingFormatArgumentException("no EXTRA_FEED_NAME passed while opening GroupFeedActivity");
        }
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Respond to the action bar's Up/Home button
//        if (item.getItemId() == android.R.id.home) {
//            onBackPressed();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        int menuId = R.menu.chat_menu;
        getMenuInflater().inflate(menuId, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.block:
                blockAccount();
                return true;
            case R.id.report:
                //reportAccount();
                return true;
            case R.id.group_info:
//                if (targetFrag != null && groupData != null) {
//                    targetFrag.openGroupInfo();
//                }
        }
        return super.onOptionsItemSelected(item);
    }

    private void blockAccount() {
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
                    if (response.isSuccessful()) {
                        //todo
                        Snackbar.make(rootView, "Group Exited!", Snackbar.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Snackbar.make(rootView, "Failed to Exit Group. Please Try again", Snackbar.LENGTH_SHORT).show();
                    FirebaseCrashlytics.getInstance().recordException(t);
                    //todo
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
            //todo
        }

    }

}