package in.lubble.app.services;

import com.google.common.collect.Lists;
import com.google.firebase.auth.FirebaseAuth;

import java.net.MalformedURLException;

import in.lubble.app.LubbleSharedPrefs;
import io.getstream.cloud.CloudClient;
import io.getstream.cloud.CloudFlatFeed;
import io.getstream.core.exceptions.StreamException;
import io.getstream.core.models.Activity;
import io.getstream.core.models.FeedID;

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

    public static boolean post(String postText,String groupName) throws StreamException {
        if (client != null) {
            CloudFlatFeed userFeed = client.flatFeed("user",uid);
            String locality = LubbleSharedPrefs.getInstance().getLubbleName();
            CloudFlatFeed groupFeed = client.flatFeed("group",groupName+"_"+locality);
            CloudFlatFeed localityFeed = client.flatFeed("locality",locality);
//            userFeed.follow(groupFeed);
//            userFeed.follow(localityFeed);
            try {
                userFeed.addActivity(
                        Activity
                                .builder()
                                .actor("user:" + uid)
                                .verb("post")
                                .object("picture:10")
                                .extraField("message", postText)
                                .extraField("photoLink", "https://www.planetware.com/wpimages/2020/02/france-in-pictures-beautiful-places-to-photograph-eiffel-tower.jpg")
                                .extraField("authorName",userName)
                                .to(Lists.newArrayList(groupFeed.getID(), localityFeed.getID()))
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
