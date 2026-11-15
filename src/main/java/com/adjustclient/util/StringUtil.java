package com.adjustclient.util;

import java.text.DecimalFormat;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringUtil {
    public static final Pattern UUID_PATTERN;
    public static final DecimalFormat TWO_DECIMALS;
    public static final DecimalFormat THREE_DECIMALS;

    static {
        UUID_PATTERN = Pattern.compile("\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b");
        TWO_DECIMALS = new DecimalFormat("#.##");
        THREE_DECIMALS = new DecimalFormat("#.###");
    }

    public static String formatBytes(long bytes) {
        if (bytes > -1000 && bytes < 1000) return bytes + " B";
        int unit = 0;
        while (bytes <= -999950 || bytes >= 999950) {
            bytes /= 1000;
            unit++;
        }
        return String.format("%.1f %cB", bytes / 1000.0, "kMGTPE".charAt(unit));
    }

    public static String firstGroup(String regex, String input) {
        Matcher matcher = Pattern.compile(regex).matcher(input);
        return matcher.find() ? matcher.group(1) : null;
    }

    public static boolean containsIdentity(String value, String[] array) {
        for (String item : array) {
            if (Objects.requireNonNull(value).equalsIgnoreCase(item)) return true;
        }
        return false;
    }

    public static boolean containsAny(String[] values, String[] array) {
        for (String value : values) {
            for (String item : array) {
                if (Objects.requireNonNull(value).equalsIgnoreCase(item)) return true;
            }
        }
        return false;
    }

    public static char randomCharacter() {
        return (char) (0x20 + RandomUtil.RANDOM.nextInt(95));
    }

    public static String replaceLastDot(String text) {
        int dot = text.lastIndexOf('.');
        return dot == -1 ? text : text.substring(0, dot) + "\u2025" + text.substring(dot + 1);
    }

    public static String toRoman(int number) {
        StringBuilder roman = new StringBuilder();
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        for (int i = 0; i < values.length; i++) {
            while (number >= values[i]) {
                roman.append(symbols[i]);
                number -= values[i];
            }
        }
        return roman.toString();
    }
}
