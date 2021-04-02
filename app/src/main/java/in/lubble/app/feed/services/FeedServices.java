package in.lubble.app.feed.services;

import com.google.firebase.auth.FirebaseAuth;

import java.net.MalformedURLException;

import in.lubble.app.models.ProfileInfo;
import io.getstream.cloud.CloudClient;
import io.getstream.cloud.CloudFlatFeed;
import io.getstream.core.exceptions.StreamException;
import io.getstream.core.models.Activity;

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

    public static boolean post(String postText) {
        if (client != null) {
            CloudFlatFeed feed = client.flatFeed("user");
            try {
                feed.addActivity(
                        Activity
                                .builder()
                                .actor("SU:" + uid)
                                .verb("post")
                                .object("picture:10")
                                .extraField("message", postText)
                                .extraField("photoLink", "https://www.planetware.com/wpimages/2020/02/france-in-pictures-beautiful-places-to-photograph-eiffel-tower.jpg")
                                .extraField("authorName",userName)
                                .build()
                ).join();
                return true;
            } catch (StreamException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
}
