package com.adjustclient.backend.api.error;

import com.adjustclient.util.ChatMessage;
import com.adjustclient.util.ErrorReport;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import com.adjustclient.user.ClientUser;
import java.util.function.*;

public enum ConfigurationError {
    ALREADY_SENT, INVITE_SETTINGS, NOT_ONLINE, BLOCKED;
    public Function<String, ErrorReport> handler;
    public static final Function<String, ErrorReport> messageHandler1 = name -> new ErrorReport("You cannot share configurations with {} because of their invite settings.", name);
    public static final Function<String, ErrorReport> messageHandler2 = name -> new ErrorReport("A request for this configuration is already outgoing to {}!", name);
    public static final Function<String, ErrorReport> messageHandler3 = name -> new ErrorReport("You cannot share configurations with {} because they aren't online.", name);
    public static final Function<String, ErrorReport> messageHandler4 = name -> new ErrorReport("You cannot share configurations with {} because they blocked you.", name);
    static {
        ALREADY_SENT.handler = messageHandler2;
        INVITE_SETTINGS.handler = messageHandler1;
        NOT_ONLINE.handler = messageHandler3;
        BLOCKED.handler = messageHandler4;
    }
}
