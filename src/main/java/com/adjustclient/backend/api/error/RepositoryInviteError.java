package com.adjustclient.backend.api.error;

import com.adjustclient.util.ChatMessage;
import com.adjustclient.util.ErrorReport;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import com.adjustclient.user.ClientUser;
import java.util.function.*;

public enum RepositoryInviteError {
    ALREADY_USER, ALREADY_INVITED, INVITE_SETTINGS, NOT_ONLINE, LIMIT_REACHED, BLOCKED;
    public BiConsumer<NotificationBuilder, String> handler;
    public static final BiConsumer<NotificationBuilder, String> messageHandler1 = (notification, name) -> notification.format("{} is already in this repository.", name);
    public static final BiConsumer<NotificationBuilder, String> messageHandler2 = (notification, name) -> notification.format("You already have an invite pending to {}.", name);
    public static final BiConsumer<NotificationBuilder, String> messageHandler3 = (notification, name) -> notification.format("You cannot invite {} because of their invite settings.", name);
    public static final BiConsumer<NotificationBuilder, String> messageHandler4 = (notification, name) -> notification.format("This repository has reached maximum users.", name);
    public static final BiConsumer<NotificationBuilder, String> messageHandler5 = (notification, name) -> notification.format("You cannot invite {} because they have blocked you.", name);
    public static final BiConsumer<NotificationBuilder, String> messageHandler6 = (notification, name) -> notification.format("You cannot invite {} because they aren't online.", name);
    static {
        ALREADY_USER.handler = messageHandler1;
        ALREADY_INVITED.handler = messageHandler2;
        INVITE_SETTINGS.handler = messageHandler3;
        NOT_ONLINE.handler = messageHandler6;
        LIMIT_REACHED.handler = messageHandler4;
        BLOCKED.handler = messageHandler5;
    }
}
