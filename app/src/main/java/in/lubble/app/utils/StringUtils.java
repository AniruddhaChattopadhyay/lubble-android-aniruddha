package in.lubble.app.utils;

import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;

/**
 * Created by ishaangarg on 11/11/17.
 */

public class StringUtils {

    public static boolean isValidString(String string) {
        return string != null && !string.equalsIgnoreCase("") && !string.equalsIgnoreCase("null");
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

}
