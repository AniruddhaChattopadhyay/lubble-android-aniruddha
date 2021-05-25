package in.lubble.app.services;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;

import in.lubble.app.BuildConfig;
import in.lubble.app.LubbleSharedPrefs;
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
    private static final String user = FirebaseAuth.getInstance().getUid();// "c4ZIgCriHdcU5avx70AgY0000jj1";
    public static CloudClient client = null;
    private static CloudClient timelineClient = null;
    public static final String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    public static final String uid = FirebaseAuth.getInstance().getUid();

    public static void init(String apiKey, String userToken) throws MalformedURLException {
        CloudClient.Builder builder = CloudClient
                .builder(apiKey, userToken, user);
        if (!BuildConfig.DEBUG) {
            builder.region(Region.SINGAPORE);
        }
        client = builder
                .build();
    }

    public static CloudClient initTimelineClient(String apiKey, String userToken) throws MalformedURLException {
        timelineClient = CloudClient
                .builder(apiKey, userToken, user)
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
            timelineClient = CloudClient
                    .builder(feedApiKey, feedUserToken, user)
                    .build();
        } catch (MalformedURLException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            e.printStackTrace();
        }
    }

    public static boolean post(FeedPostData feedPostData, String groupName, @Nullable String imgUrl, float aspectRatio, @Nullable Callback<Void> callback) {
        Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("message", feedPostData.getText());
            jsonObject.put("groupName", groupName);
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

        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
            //todo
            return false;
        }
        return true;
    }
}


//        if (client != null) {
//            CloudFlatFeed userFeed = client.flatFeed("user",uid);
//            String locality = LubbleSharedPrefs.getInstance().getLubbleName();
//            CloudFlatFeed groupFeed = client.flatFeed("group",groupName+"_"+locality);
//            CloudFlatFeed localityFeed = client.flatFeed("locality",locality);
////            userFeed.follow(groupFeed);
////            userFeed.follow(localityFeed);
//            try {
//                userFeed.addActivity(
//                        Activity
//                                .builder()
//                                .actor("user:" + uid)
//                                .verb("post")
//                                .object("picture:10")
//                                .extraField("message", postText)
//                                .extraField("photoLink", "https://www.planetware.com/wpimages/2020/02/france-in-pictures-beautiful-places-to-photograph-eiffel-tower.jpg")
//                                .extraField("authorName",userName)
//                                .to(Lists.newArrayList(groupFeed.getID(), localityFeed.getID()))
//                                .build()
//                ).join();
//                return true;
//            } catch (StreamException e) {
//                e.printStackTrace();
//                return false;
//            }
//        }
//        return false;
//    }

