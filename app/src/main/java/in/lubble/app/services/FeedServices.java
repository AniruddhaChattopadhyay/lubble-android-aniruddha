package in.lubble.app.services;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;

import in.lubble.app.BuildConfig;
import in.lubble.app.LubbleApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.models.FeedPostData;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import io.getstream.cloud.CloudClient;
import io.getstream.core.Region;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static in.lubble.app.Constants.MEDIA_TYPE;

public class FeedServices {
    public static CloudClient client = null;
    private static CloudClient timelineClient = null;

    public static void init(String apiKey, String userToken) throws MalformedURLException {
        CloudClient.Builder builder = CloudClient
                .builder(apiKey, userToken, FirebaseAuth.getInstance().getUid());
        if (!BuildConfig.DEBUG) {
            builder.region(Region.SINGAPORE);
        }
        client = builder
                .build();
    }

    public static CloudClient initTimelineClient(String apiKey, String userToken) throws MalformedURLException {
        timelineClient = CloudClient
                .builder(apiKey, userToken, FirebaseAuth.getInstance().getUid())
                .build();
        LubbleSharedPrefs.getInstance().setFeedApiKey(apiKey);
        LubbleSharedPrefs.getInstance().setFeedUserToken(userToken);
        return timelineClient;
    }

    public static CloudClient getTimelineClient() {
        if (timelineClient == null) {
            String feedUserToken = LubbleSharedPrefs.getInstance().getFeedUserToken();
            String feedApiKey = LubbleSharedPrefs.getInstance().getFeedApiKey();
            if (!TextUtils.isEmpty(feedUserToken) && !TextUtils.isEmpty(feedApiKey)) {
                recreateTimelineClient(feedUserToken, feedApiKey);
            } else {
                throw new IllegalStateException(FeedServices.class.getCanonicalName() +
                        ":timelineClient is not initialized, call initTimelineClient(..) method first.");
            }
        }
        return timelineClient;
    }

    public static void recreateTimelineClient(String feedUserToken, String feedApiKey) {
        try {
            CloudClient.Builder builder = CloudClient
                    .builder(feedApiKey, feedUserToken, FirebaseAuth.getInstance().getUid());
            if (!BuildConfig.DEBUG) {
                builder.region(Region.SINGAPORE);
            }
            timelineClient = builder
                    .build();
        } catch (MalformedURLException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            e.printStackTrace();
        }
    }

    public static void post(FeedPostData feedPostData, String groupName, String feedName, @Nullable String imgUrl, float aspectRatio, @Nullable Callback<Void> callback) {
        Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("message", feedPostData.getText());
            jsonObject.put("groupName", groupName);
            jsonObject.put("feedName", feedName);
            jsonObject.put("linkTitle", feedPostData.getLinkTitle());
            jsonObject.put("linkDesc", feedPostData.getLinkDesc());
            jsonObject.put("linkPicUrl", feedPostData.getLinkImageUrl());
            jsonObject.put("linkUrl", feedPostData.getLinkUrl());
            jsonObject.put("aspectRatio", Float.valueOf(aspectRatio));
            if (imgUrl != null) {
                jsonObject.put("photoLink", imgUrl);
            }
            RequestBody body = RequestBody.create(MEDIA_TYPE, jsonObject.toString());
            Call<Void> call = endpoints.addFeedPost(body);
            if (callback != null) {
                call.enqueue(callback);
            } else {
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {

                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {

                    }
                });
            }
            Bundle bundle = new Bundle();
            bundle.putString("group", groupName);
            Analytics.triggerEvent(AnalyticsEvents.FEED_SEND_POST, bundle, LubbleApp.getAppContext());
        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    public static void clearAll() {
        client = null;
        timelineClient = null;
    }

}