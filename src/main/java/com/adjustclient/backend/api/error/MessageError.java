package com.adjustclient.backend.api.error;

import com.adjustclient.util.ChatMessage;
import com.adjustclient.util.ErrorReport;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import com.adjustclient.user.ClientUser;
import java.util.function.*;

public enum MessageError {
    NOT_ONLINE, INVITE_SETTINGS, SELF_INVITE_SETTINGS, SELF_BLOCKED, BLOCKED;
    public BiConsumer<ChatMessage, String> handler;
    public static final BiConsumer<ChatMessage, String> lambda_24385613a51d1779af73b33e82384b2c0 = (chat, name) -> { };
    public static final BiConsumer<ChatMessage, String> lambda_5a383eda300eafa01bf84e318091d7140 = (chat, name) -> chat.format("You cannot message {} because they aren't online.", name);
    public static final BiConsumer<ChatMessage, String> lambda_83c09ba541af85ebb7aa02dcb702913d0 = (chat, name) -> chat.format("You cannot message {} because they blocked you.", name);
    public static final BiConsumer<ChatMessage, String> lambda_895a61cca4ffeabb3d1c5cbcf702de6d0 = (chat, name) -> chat.format("You cannot message {} because of their invite settings.", name);
    public static final BiConsumer<ChatMessage, String> lambda_dde1571af2c07f681cf325af5c94432e0 = (chat, name) -> { };
    static {
        NOT_ONLINE.handler = lambda_5a383eda300eafa01bf84e318091d7140;
        INVITE_SETTINGS.handler = lambda_895a61cca4ffeabb3d1c5cbcf702de6d0;
        SELF_INVITE_SETTINGS.handler = lambda_dde1571af2c07f681cf325af5c94432e0;
        SELF_BLOCKED.handler = lambda_24385613a51d1779af73b33e82384b2c0;
        BLOCKED.handler = lambda_83c09ba541af85ebb7aa02dcb702913d0;
    }
}
