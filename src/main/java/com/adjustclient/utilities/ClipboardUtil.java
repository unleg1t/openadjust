package com.adjustclient.utilities;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import com.adjustclient.core.logging.Logger;
import com.adjustclient.core.logging.LogLevel;
import org.lwjgl.glfw.GLFW;

public final class ClipboardUtil {
    public static long sharedWindowHandle = -1L;
    public static String getClipboard() {
        long window = sharedWindowHandle;
        if (window == -1L) return "";
        try {
            String value = GLFW.glfwGetClipboardString(window);
            return value == null ? "" : value;
        } catch (Exception exception) {
            Logger.log(LogLevel.ERROR, true, "Failed to read clipboard", exception);
            return "";
        }
    }

    public static void copyImage(BufferedImage image) {
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new ImageTransferable(image), null);
        } catch (Exception exception) {
            Logger.log(LogLevel.ERROR, true, "Failed to set image clipboard with '{}'", exception);
        }
    }

    public static void copyToClipboard(String text) {
        long window = sharedWindowHandle;
        if (window == -1L) return;
        try {
            GLFW.glfwSetClipboardString(window, text);
        } catch (Exception exception) {
            Logger.log(LogLevel.ERROR, true, "Failed to set clipboard with '{}'", text);
        }
    }
    public static String[] splitThreeFields(String text) {
        if (text.contains(":")) {
            String[] fields = text.split(":");
            if (fields.length == 3) return fields;
        }
        if (text.contains("\\|")) {
            String[] fields = text.split("\\|");
            if (fields.length == 3) return fields;
        }
        return null;
    }

    public static org.apache.commons.lang3.tuple.Pair<String, String> splitPair(String text) {
        text = stripDecorations(text);
        if (text.contains(":")) {
            String[] fields = text.split(":");
            if (fields.length >= 2) return new org.apache.commons.lang3.tuple.ImmutablePair<>(fields[0], fields[1]);
        }
        if (text.contains("|")) {
            String[] fields = text.split("\\|");
            if (fields.length >= 2) return new org.apache.commons.lang3.tuple.ImmutablePair<>(fields[0], fields[1]);
        }
        return null;
    }

    public static String stripDecorations(String text) {
        text = text.trim();
        while (text.startsWith("[")) {
            int end = text.indexOf(']');
            if (end == -1) break;
            text = text.substring(end + 1);
        }
        int suffix = text.lastIndexOf(" |");
        return suffix == -1 ? text : text.substring(0, suffix);
    }
}
