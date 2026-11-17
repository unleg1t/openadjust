package com.adjustclient.backend.api.error;

import com.adjustclient.util.ChatMessage;
import com.adjustclient.util.ErrorReport;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import com.adjustclient.user.ClientUser;
import java.util.function.*;

public enum MessageError {
    NOT_ONLINE, INVITE_SETTINGS, SELF_INVITE_SETTINGS, SELF_BLOCKED, BLOCKED;
    public BiConsumer<ChatMessage, String> handler;
    public static final BiConsumer<ChatMessage, String> messageHandler1 = (chat, name) -> { };
    public static final BiConsumer<ChatMessage, String> messageHandler2 = (chat, name) -> chat.format("You cannot message {} because they aren't online.", name);
    public static final BiConsumer<ChatMessage, String> messageHandler3 = (chat, name) -> chat.format("You cannot message {} because they blocked you.", name);
    public static final BiConsumer<ChatMessage, String> messageHandler4 = (chat, name) -> chat.format("You cannot message {} because of their invite settings.", name);
    public static final BiConsumer<ChatMessage, String> messageHandler5 = (chat, name) -> { };
    static {
        NOT_ONLINE.handler = messageHandler2;
        INVITE_SETTINGS.handler = messageHandler4;
        SELF_INVITE_SETTINGS.handler = messageHandler5;
        SELF_BLOCKED.handler = messageHandler1;
        BLOCKED.handler = messageHandler3;
    }
}
