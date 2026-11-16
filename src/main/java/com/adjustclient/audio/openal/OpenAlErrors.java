package com.adjustclient.audio.openal;

import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import com.adjustclient.logging.Logger;
import com.adjustclient.logging.LogLevel;
public final class OpenAlErrors {
    public static boolean checkAlError() {
        int error = AL10.alGetError();
        if (error == 0) return false;
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        StackTraceElement site = stack.length > 2 ? stack[2] : (stack.length > 0 ? stack[stack.length - 1] : null);
        String file = site == null || site.getFileName() == null ? "Unknown" : site.getFileName().replace(".java", "");
        String method = site == null ? "unknown" : site.getMethodName();
        int line = stack.length > 3 ? stack[3].getLineNumber() : (site == null ? -1 : site.getLineNumber());
        String description = switch (error) {
            case 40961 -> "Invalid name";
            case 40962 -> "Invalid enum";
            case 40963 -> "Invalid value";
            case 40964 -> "Invalid operation";
            case 40965 -> "Out of memory";
            default -> "Unknown error";
        };
        Logger.log(LogLevel.ERROR, true, "OpenAL error: " + description + " at " + file + "." + method + "(" + file + ":" + line + ")");
        return true;
    }
    public static boolean checkAlcError(long device) {
        int error = ALC10.alcGetError(device);
        if (error == 0) return false;
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        StackTraceElement site = stack.length > 2 ? stack[2] : (stack.length > 0 ? stack[stack.length - 1] : null);
        String file = site == null || site.getFileName() == null ? "Unknown" : site.getFileName().replace(".java", "");
        String method = site == null ? "unknown" : site.getMethodName();
        int line = stack.length > 3 ? stack[3].getLineNumber() : (site == null ? -1 : site.getLineNumber());
        String description = switch (error) {
            case 40961 -> "Invalid device";
            case 40962 -> "Invalid context";
            case 40963 -> "Invalid enum";
            case 40964 -> "Invalid value";
            case 40965 -> "Out of memory";
            default -> "Unknown error";
        };
        Logger.log(LogLevel.ERROR, true, "OpenALC error: " + description + " at " + file + "." + method + "(" + file + ":" + line + ")");
        return true;
    }
}
