package org.lwjgl;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

/**
 * LWJGL 2 {@code Sys} bridge backed by JVM monotonic timing, AWT/OS URL launch,
 * TinyFileDialogs, and GLFW clipboard on LWJGL 3.
 */
public final class Sys {
    private static final long TIMER_RESOLUTION = 1000L;
    private static final long START_NANOS = System.nanoTime();
    private static final long EPOCH_OFFSET_MS = 1_000_000_000L;
    private static final boolean IS_64_BIT = check64Bit();

    private Sys() {
    }

    private static boolean check64Bit() {
        String model = System.getProperty("sun.arch.data.model");
        if (model != null) {
            return "64".equals(model);
        }
        String arch = System.getProperty("os.arch", "");
        return arch.contains("64");
    }

    public static String getVersion() {
        return Version.getVersion();
    }

    public static void initialize() {
        // Native initialization is managed by LWJGL 3 / Display.
    }

    public static boolean is64Bit() {
        return IS_64_BIT;
    }

    public static long getTimerResolution() {
        return TIMER_RESOLUTION;
    }

    public static long getTime() {
        return ((System.nanoTime() - START_NANOS) / 1_000_000L) + EPOCH_OFFSET_MS;
    }

    public static void alert(String title, String message) {
        String safeTitle = title != null ? title : "";
        String safeMsg = message != null ? message : "";
        try {
            TinyFileDialogs.tinyfd_messageBox(safeTitle, safeMsg, "ok", "info", true);
        } catch (Throwable ignored) {
            System.err.println("[" + safeTitle + "] " + safeMsg);
        }
    }

    public static boolean openURL(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(URI.create(url));
                    return true;
                }
            }
        } catch (Throwable ignored) {
            // Fall through to OS launcher.
        }
        String os = System.getProperty("os.name", "").toLowerCase();
        try {
            if (os.contains("win")) {
                new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url).start();
                return true;
            }
            if (os.contains("mac")) {
                new ProcessBuilder("open", url).start();
                return true;
            }
            new ProcessBuilder("xdg-open", url).start();
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    public static String getClipboard() {
        long window = Display.getWindowHandle();
        if (window == 0L) {
            return "";
        }
        try {
            String value = GLFW.glfwGetClipboardString(window);
            return value != null ? value : "";
        } catch (Throwable ignored) {
            return "";
        }
    }
}
