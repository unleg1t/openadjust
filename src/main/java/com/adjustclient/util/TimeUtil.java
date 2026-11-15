package com.adjustclient.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class TimeUtil {
    public static final SimpleDateFormat DATE_FORMAT;
    public static int SECOND = 1000;

    static {
        DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    }

    public static String formatTimestamp(long millis) {
        SimpleDateFormat weekday = new SimpleDateFormat("EEE", Locale.ENGLISH);
        SimpleDateFormat time = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
        SimpleDateFormat full = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.ENGLISH);
        Date date = new Date(millis);
        String day = weekday.format(date);
        String clock = time.format(date);
        long now = System.currentTimeMillis();
        if (now - date.getTime() >= 604800000L) return full.format(date);
        return day.equals(weekday.format(new Date(now))) ? clock : day + ", " + clock;
    }

    public static String timeLeft(long millis) {
        if (millis <= 0) return "ended";
        long minutes = millis / 1000 / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        StringBuilder text = new StringBuilder();
        if (days > 0) text.append(days).append(" days");
        if (hours % 24 > 0) {
            if (text.length() != 0) text.append(" and ");
            text.append(hours % 24).append(" hours");
        }
        if (minutes % 60 > 0 && days == 0) {
            if (text.length() != 0) text.append(" and ");
            text.append(minutes % 60).append(" minutes");
        }
        return text.length() == 0 ? "less than a minute left" : text.append(" left").toString().trim();
    }

    public static String formatDuration(long millis, boolean decimal) {
        if (millis <= 0) return decimal ? "0.0s" : "0s";
        if (millis < 60000) return decimal ? String.format("%.1fs", millis / 1000.0) : millis / 1000 + "s";
        long seconds = millis % 60000 / 1000;
        if (millis < 3600000) return millis / 60000 + "m " + seconds + "s";
        long minutes = millis % 3600000 / 60000;
        if (millis < 86400000) return millis / 3600000 + "h " + minutes + "m " + seconds + "s";
        return millis / 86400000 + "d " + millis % 86400000 / 3600000 + "h " + minutes + "m " + seconds + "s";
    }

    public static String timeAgo(long millis, String suffix) {
        long elapsed = System.currentTimeMillis() - millis;
        if (elapsed <= SECOND) return "just now";
        long count = elapsed / SECOND;
        String unit;
        if (count <= 60) unit = " second";
        else if ((count /= 60) <= 60) unit = " minute";
        else if ((count /= 60) <= 24) unit = " hour";
        else if ((count /= 24) < 31) {
            if (count >= 15) return count / 7 + " weeks" + suffix;
            unit = " day";
        } else if ((count /= 30) <= 12) unit = " month";
        else { count /= 12; unit = " year"; }
        return count + unit + (count == 1 ? "" : "s") + suffix;
    }
}
