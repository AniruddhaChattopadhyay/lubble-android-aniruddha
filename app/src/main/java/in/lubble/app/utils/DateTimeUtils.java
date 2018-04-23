package in.lubble.app.utils;

import android.support.annotation.NonNull;
import android.text.format.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by ishaangarg on 27/10/17.
 */

public class DateTimeUtils {

    public static final String SERVER_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'";
    public static final String APP_SHORT_TIME = "h:mm a";
    public static final String APP_DATE_YEAR = "MMM dd, yyyy";

    public static String currTimestampInString() {
        Calendar c = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat(SERVER_DATE_TIME, Locale.ENGLISH);
        df.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        return df.format(c.getTimeInMillis());
    }

    public static long convertStringDateTimeToLong(String timeToConvert, String inputFormat, String outputFormat, boolean shouldConvertTimezone) {

        DateFormat inputDateFormat = new SimpleDateFormat(inputFormat, Locale.ENGLISH);
        DateFormat outputDateFormat = new SimpleDateFormat(outputFormat, Locale.ENGLISH);
        if (shouldConvertTimezone) {
            inputDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            outputDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        }
        String readableOrderDateTime = "";
        long dateTimeInMilliSec = 0;
        try {
            readableOrderDateTime = outputDateFormat.format(inputDateFormat.parse(timeToConvert));
            SimpleDateFormat f = new SimpleDateFormat(outputFormat, Locale.ENGLISH);
            Date d = f.parse(readableOrderDateTime);
            dateTimeInMilliSec = d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateTimeInMilliSec;
    }

    @NonNull
    public static String getDateFromLong(long timeInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat(APP_DATE_YEAR);
        Date resultDate = new Date(timeInMillis);
        return sdf.format(resultDate);
    }

    @NonNull
    public static String getTimeFromLong(long timeInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat(APP_SHORT_TIME);
        Date resultDate = new Date(timeInMillis);
        return sdf.format(resultDate);
    }

    @NonNull
    public static String getHumanTimestamp(@NonNull String timestamp) {
        long time = convertStringDateTimeToLong(timestamp, SERVER_DATE_TIME, SERVER_DATE_TIME, true);
        return getHumanTimestamp(time);
    }

    @NonNull
    public static String getHumanTimestamp(long time) {
        String humanTime = DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString();
        if (humanTime.equalsIgnoreCase("0 minutes ago") || humanTime.equalsIgnoreCase("In 0 minutes")) {
            return "Just now";
        }
        return humanTime;
    }

}
