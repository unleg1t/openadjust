package com.adjustclient.util;

import net.minecraft.util.ChatColor;

public final class AnsiUtil {
    public static final java.util.regex.Pattern ANSI_PATTERN;
    public static final java.util.Map<Integer, net.minecraft.util.ChatColor> ANSI_FORMATS;
    static {
        ANSI_PATTERN = java.util.regex.Pattern.compile("\\u001B\\[[0-?]*[ -/]*[@-~]");
        ANSI_FORMATS = java.util.Map.of(0, ChatColor.RESET, 1, ChatColor.BOLD,
                3, ChatColor.ITALIC, 23, ChatColor.RESET, 39, ChatColor.WHITE,
                21, ChatColor.UNDERLINE, 33, net.minecraft.util.ChatColor.YELLOW);
    }
    public static String stripAnsi(String text) { return ANSI_PATTERN.matcher(text).replaceAll(""); }
}
