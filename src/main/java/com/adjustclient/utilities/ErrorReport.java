package com.adjustclient.utilities;

public final class ErrorReport {
    public String message;
    public Object[] arguments;
    public ErrorReport(String message, Object... arguments) { this.message = message; this.arguments = arguments; }
    public com.adjustclient.core.gui.hud.notification.NotificationBuilder notification(String title) {
        com.adjustclient.core.gui.hud.notification.NotificationBuilder builder = new com.adjustclient.core.gui.hud.notification.NotificationBuilder();
        builder.title = title;
        builder.parts = new java.util.ArrayList<>();
        builder.actions = new java.util.ArrayList<>();
        return builder.format(message, arguments);
    }
}
