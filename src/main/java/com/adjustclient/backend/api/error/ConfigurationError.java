package com.adjustclient.backend.api.error;

import com.adjustclient.util.ChatMessage;
import com.adjustclient.util.ErrorReport;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import com.adjustclient.user.ClientUser;
import java.util.function.*;

public enum ConfigurationError {
    ALREADY_SENT, INVITE_SETTINGS, NOT_ONLINE, BLOCKED;
    public Function<String, ErrorReport> handler;
    public static final Function<String, ErrorReport> lambda_150308b75730de8ca8c7458db4b39e880 = name -> new ErrorReport("You cannot share configurations with {} because of their invite settings.", name);
    public static final Function<String, ErrorReport> lambda_43cea1cc6a10d5faa545f320c5b419d50 = name -> new ErrorReport("A request for this configuration is already outgoing to {}!", name);
    public static final Function<String, ErrorReport> lambda_b532cf3ce09c5b404c79f88e074ab3f80 = name -> new ErrorReport("You cannot share configurations with {} because they aren't online.", name);
    public static final Function<String, ErrorReport> lambda_e657f0cd1e0f3fc0326d904af4a8f46b0 = name -> new ErrorReport("You cannot share configurations with {} because they blocked you.", name);
    static {
        ALREADY_SENT.handler = lambda_43cea1cc6a10d5faa545f320c5b419d50;
        INVITE_SETTINGS.handler = lambda_150308b75730de8ca8c7458db4b39e880;
        NOT_ONLINE.handler = lambda_b532cf3ce09c5b404c79f88e074ab3f80;
        BLOCKED.handler = lambda_e657f0cd1e0f3fc0326d904af4a8f46b0;
    }
}
