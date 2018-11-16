package in.lubble.app.utils;

import android.text.TextUtils;
import android.util.Patterns;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ishaangarg on 11/11/17.
 */

public class StringUtils {

    private static final String TAG = "StringUtils";

    public static boolean isValidString(String string) {
        return !TextUtils.isEmpty(string);
    }

    @Nullable
    public static String getStringFromTil(TextInputLayout textInputLayout) {
        return textInputLayout.getEditText().getText().toString();
    }

    public static String getTitleCase(String str) {

        if (str == null) {
            return null;
        }

        boolean space = true;
        StringBuilder builder = new StringBuilder(str);
        final int len = builder.length();

        for (int i = 0; i < len; ++i) {
            char c = builder.charAt(i);
            if (space) {
                if (!Character.isWhitespace(c)) {
                    // Convert to title case and switch out of whitespace mode.
                    builder.setCharAt(i, Character.toTitleCase(c));
                    space = false;
                }
            } else if (Character.isWhitespace(c)) {
                space = true;
            } else {
                builder.setCharAt(i, Character.toLowerCase(c));
            }
        }

        return builder.toString();
    }

    @Nullable
    public static String extractFirstLink(String text) {
        Matcher m = Patterns.WEB_URL.matcher(text);
        if (m.find()) {
            return m.group();
        }

        return null;
    }

    public static String extractYoutubeId(String ytUrl) {
        String vId = null;
        Pattern pattern = Pattern.compile(
                "^https?://.*(?:youtu.be/|v/|u/\\w/|embed/|watch?v=)([^#&?]*).*$",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(ytUrl);
        if (matcher.matches()) {
            vId = matcher.group(1);
        }
        return vId;
    }

}
