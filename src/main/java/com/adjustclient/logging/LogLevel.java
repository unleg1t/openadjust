package com.adjustclient.logging;

public enum LogLevel {
    INFO(AnsiColor.CYAN, "INFO"), ERROR(AnsiColor.RED, "ERROR"),
    DEBUG(AnsiColor.MAGENTA, "DEBUG"), WARN(AnsiColor.YELLOW, "WARN");

    public final AnsiColor color;
    public final String label;
    public Object field24;

    LogLevel(AnsiColor color, String label) {
        this.color = color;
        this.label = label;
    }

}
