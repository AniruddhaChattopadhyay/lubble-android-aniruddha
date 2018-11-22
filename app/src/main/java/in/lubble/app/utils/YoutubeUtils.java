package in.lubble.app.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static in.lubble.app.utils.StringUtils.extractFirstLink;

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

    @Nullable
    public static String extractYoutubeId(String ytUrl) {
        String video_id = null;
        if (ytUrl != null && ytUrl.trim().length() > 0 && ytUrl.startsWith("http")) {

            String expression = "^.*((youtu.be" + "\\/)" + "|(v\\/)|(\\/u\\/w\\/)|(embed\\/)|(watch\\?))\\??v?=?([^#\\&\\?]*).*"; // var regExp = /^.*((youtu.be\/)|(v\/)|(\/u\/\w\/)|(embed\/)|(watch\?))\??v?=?([^#\&\?]*).*/;
            CharSequence input = ytUrl;
            Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(input);
            if (matcher.matches()) {
                String groupIndex1 = matcher.group(7);
                if (groupIndex1 != null && groupIndex1.length() == 11)
                    video_id = groupIndex1;
            }
        }
        return video_id;
    }

}
