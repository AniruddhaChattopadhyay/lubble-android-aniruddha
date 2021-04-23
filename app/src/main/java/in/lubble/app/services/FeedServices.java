package in.lubble.app.services;

import android.view.View;
import android.widget.Toast;

import com.google.common.collect.Lists;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;

import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.events.EventGroupJoinedActivity;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import io.getstream.cloud.CloudClient;
import io.getstream.cloud.CloudFlatFeed;
import io.getstream.core.exceptions.StreamException;
import io.getstream.core.models.Activity;
import io.getstream.core.models.FeedID;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static in.lubble.app.Constants.MEDIA_TYPE;

public class FeedServices {
    private static final String user = FirebaseAuth.getInstance().getUid();// "c4ZIgCriHdcU5avx70AgY0000jj1";
    public static CloudClient client = null;
    public static final String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    public static final String uid = FirebaseAuth.getInstance().getUid();

    public static void init(String apiKey, String userToken) throws MalformedURLException {
        client = CloudClient
                .builder(apiKey, userToken, user)
                .build();
    }

    public static boolean post(String postText, String groupName) throws StreamException {
        Endpoints endpoints;
        endpoints = ServiceGenerator.createService(Endpoints.class);
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("message", postText);
            jsonObject.put("groupName", groupName);
            RequestBody body = RequestBody.create(MEDIA_TYPE, jsonObject.toString());
            Call<Void> call = endpoints.addFeedPost(body);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {

                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {

                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
            FirebaseCrashlytics.getInstance().recordException(e);
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

