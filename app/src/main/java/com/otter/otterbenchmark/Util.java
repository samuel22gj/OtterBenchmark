package com.otter.otterbenchmark;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Util {

    /** Return the readable size string. */
    public static String getSizeString(long bytes) {
        if (bytes < 0) {
            return "Wrong size";
        }

        if (bytes < 1024) {
            return bytes + " Byte";
        } else if (bytes < 1024 * 1024){
            bytes /= 1024;
            return bytes + " KB";
        } else if (bytes < 1024 * 1024 * 1024){
            bytes /= 1024 * 1024;
            return bytes + " MB";
        } else if (bytes < (double) 1024 * 1024 * 1024 * 1024){
            bytes /= 1024 * 1024 * 1024;
            return bytes + " GB";
        } else {
            bytes /= (double) 1024 * 1024 * 1024 * 1024;
            return bytes + " TB";
        }
    }

    /** Return the readable time string. */
    public static String convertMillisecondToTime(long milliseconds) {
        if (milliseconds < 0) {
            return "Wrong time";
        }

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());
        format.setTimeZone(TimeZone.getTimeZone("GMT")); // Remove time zone
        return format.format(new Date(milliseconds));
    }

    /** Return the readable date & time string. */
    public static String convertMillisecondToDateTime(long milliseconds) {
        if (milliseconds < 0) {
            return "Wrong time";
        }

        SimpleDateFormat format = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss Z", Locale.getDefault());
        return format.format(new Date(milliseconds));
    }
}
