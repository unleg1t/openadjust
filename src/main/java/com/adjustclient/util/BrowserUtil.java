package com.adjustclient.util;

import com.adjustclient.logging.Logger;
import com.adjustclient.logging.LogLevel;

public final class BrowserUtil {
    public static boolean openUrl(String url) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder builder;
            if (os.contains("win")) builder = new ProcessBuilder("cmd", "/c", "start", url);
            else if (os.contains("mac")) builder = new ProcessBuilder("open", url);
            else if (os.contains("nix") || os.contains("nux")) builder = new ProcessBuilder("xdg-open", url);
            else {
                Logger.log(LogLevel.ERROR, true, "Unsupported operating system: {}", os);
                return false;
            }
            builder.start();
            return true;
        } catch (Exception exception) {
            Logger.log(LogLevel.ERROR, true, "Unable to open webpage '{}': {}", url, exception);
            return false;
        }
    }
}
