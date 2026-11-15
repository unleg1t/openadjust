package com.adjustclient.logging;

import com.adjustclient.util.AnsiUtil;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Logger {
    public static boolean debug;
    public static final DateTimeFormatter TIME_FORMAT;
    public static final ZoneId TIME_ZONE;
    public static final LogFile FILE;

    static {
        TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm:ss a")
                .withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault());
        TIME_ZONE = ZoneId.systemDefault();
        FILE = new LogFile();
        Runnable closeLog = FILE::close;
        Runtime.getRuntime().addShutdownHook(new Thread(closeLog));
    }

    public static void debugStackTrace(Throwable throwable, Object context) {
        if (!debug) {
            return;
        }
        if (context != null) {
            log(LogLevel.ERROR, true, "Debugging stack trace: {}", context);
        }
        throwable.printStackTrace();
    }

    public static void log(LogLevel level, boolean toFile, String format, Object... args) {
        StringBuilder message = new StringBuilder();
        int argument = 0;
        for (int index = 0; index < format.length(); index++) {
            if (format.charAt(index) == '{' && index + 1 < format.length() && format.charAt(index + 1) == '}') {
                if (argument < args.length) {
                    message.append(args[argument++]);
                } else {
                    message.append("{}");
                }
                index++;
            } else {
                message.append(format.charAt(index));
            }
        }
        String time = TIME_FORMAT.format(ZonedDateTime.now(TIME_ZONE));
        String line = time + "\t" + level.label + "\t" + message;
        System.out.println(line);
        if (toFile) {
            FILE.write(AnsiUtil.stripAnsi(line));
        }
    }
}
