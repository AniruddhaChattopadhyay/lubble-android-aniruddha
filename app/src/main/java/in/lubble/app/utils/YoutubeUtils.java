package in.lubble.app.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

import static in.lubble.app.utils.StringUtils.extractFirstLink;
import static in.lubble.app.utils.StringUtils.extractYoutubeId;

public class YoutubeUtils {

    public static void openYoutubeAppOrWeb(Activity activity, String message) {
        String firstLink = extractFirstLink(message);
        String ytVideoId = extractYoutubeId(firstLink);
        if (activity != null && !activity.isFinishing() && ytVideoId != null) {
            Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(firstLink));
            Intent webIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(firstLink));
            try {
                activity.startActivity(appIntent);
            } catch (ActivityNotFoundException ex) {
                activity.startActivity(webIntent);
            }
        }
    }

}
