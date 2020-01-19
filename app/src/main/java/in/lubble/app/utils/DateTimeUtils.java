package in.lubble.app.utils;

import android.text.format.DateUtils;

import androidx.annotation.NonNull;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
import static android.text.format.DateUtils.FORMAT_ABBREV_RELATIVE;

/**
 * Created by ishaangarg on 27/10/17.
 */

public class DateTimeUtils {

    public static final String SERVER_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'";
    public static final String APP_SHORT_TIME = "h:mm a";
    public static final String APP_DATE_YEAR = "MMM dd, yyyy";
    public static final String APP_DATE_NO_YEAR = "MMM dd";
    public static final String APP_NORMAL_DATE_YEAR = "dd MMM, yyyy";
    public static final String OFFICIAL_DATE_YEAR = "dd/MM/yyyy";
    public static final String EVENT_DATE_TIME = "dd MMM, h:mm a";
    public static final String SHORT_MONTH = "MMM";
    public static final String DATE = "dd";
    public static final long FAMILY_FUN_NIGHT_END_TIME = 1529753400000L;
    public static final String COUNTDOWN_TIME = "hh:mm:ss";

    public static String currTimestampInString() {
        Calendar c = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat(SERVER_DATE_TIME, Locale.ENGLISH);
        df.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        return df.format(c.getTimeInMillis());
    }

    public static String getCurrMonth() {
        Calendar c = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat(SHORT_MONTH, Locale.ENGLISH);
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
        Date resultDate = new Date(timeInMillis);
        final Calendar calendar = Calendar.getInstance();
        final Calendar currCalendar = Calendar.getInstance();
        calendar.setTime(resultDate);
        if (calendar.get(Calendar.YEAR) == currCalendar.get(Calendar.YEAR)) {
            SimpleDateFormat sdf = new SimpleDateFormat(APP_DATE_NO_YEAR);
            return sdf.format(resultDate);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat(APP_DATE_YEAR);
            return sdf.format(resultDate);
        }
    }

    @NonNull
    public static String getTimeFromLong(long timeInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat(APP_SHORT_TIME);
        Date resultDate = new Date(timeInMillis);
        return sdf.format(resultDate);
    }

    @NonNull
    public static String getTimeFromLong(long timeInMillis, String inputFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(inputFormat);
        Date resultDate = new Date(timeInMillis);
        return sdf.format(resultDate);
    }

    public static int getTimeBasedUniqueInt() {
        return (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
    }

    @NonNull
    public static String getHumanTimestamp(@NonNull String timestamp) {
        long time = convertStringDateTimeToLong(timestamp, SERVER_DATE_TIME, SERVER_DATE_TIME, true);
        return getHumanTimestamp(time);
    }

    @NonNull
    public static String getHumanTimestamp(long time) {
        String humanTime = DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, FORMAT_ABBREV_RELATIVE | FORMAT_ABBREV_MONTH ).toString();
        if (humanTime.equalsIgnoreCase("0 min. ago")
                || humanTime.equalsIgnoreCase("0 mins ago")
                || humanTime.equalsIgnoreCase("In 0 min.")
                || humanTime.equalsIgnoreCase("In 0 mins")
                ) {
            return "Just now";
        }
        return humanTime;
    }

    @NonNull
    /**
     * return "3 mins ago" if time is < 5mins from curr time
     *  otherwise returns readable date time
     */
    public static String getHumanTimestampWithTime(long time) {
        if (System.currentTimeMillis() - time < 5 * DateUtils.MINUTE_IN_MILLIS) {
            String humanTime = DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, FORMAT_ABBREV_RELATIVE | FORMAT_ABBREV_MONTH).toString();
            if (humanTime.equalsIgnoreCase("0 min. ago")
                    || humanTime.equalsIgnoreCase("0 mins ago")
                    || humanTime.equalsIgnoreCase("In 0 min.")
                    || humanTime.equalsIgnoreCase("In 0 mins")
                    ) {
                return "Just now";
            }
            return humanTime;
        } else {
            return getDateFromLong(time) + ", " + getTimeFromLong(time);
        }
    }

    public static int getAge(Calendar dob) {
        Calendar today = Calendar.getInstance();

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        Integer ageInt = new Integer(age);
        String ageS = ageInt.toString();

        return age;
    }

}
