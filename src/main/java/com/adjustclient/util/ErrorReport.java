package com.adjustclient.util;

public final class ErrorReport {
    public String message;
    public Object[] arguments;
    public ErrorReport(String message, Object... arguments) { this.message = message; this.arguments = arguments; }
    public com.adjustclient.ui.hud.notification.NotificationBuilder notification(String title) {
        com.adjustclient.ui.hud.notification.NotificationBuilder builder = new com.adjustclient.ui.hud.notification.NotificationBuilder();
        builder.title = title;
        builder.parts = new java.util.ArrayList<>();
        builder.actions = new java.util.ArrayList<>();
        return builder.format(message, arguments);
    }
}
