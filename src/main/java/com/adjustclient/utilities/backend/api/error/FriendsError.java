package com.adjustclient.utilities.backend.api.error;

import com.adjustclient.utilities.ChatMessage;
import com.adjustclient.utilities.ErrorReport;
import com.adjustclient.core.gui.hud.notification.NotificationBuilder;
import com.adjustclient.managers.user.ClientUser;
import java.util.function.*;

public enum FriendsError {
    ALREADY_FRIENDS, FRIEND_REQUEST_SENT, NOT_ONLINE, INVITE_SETTINGS,
    REQUEST_ALREADY_SENT, INVITING_YOURSELF, BLOCKED, NOT_FRIENDS_WITH,
    NO_USER_EXISTS, REMOVED_FRIEND, ALREADY_BLOCKED, ALREADY_UNBLOCKED,
    USER_BLOCKED, USER_UNBLOCKED;
    public BiConsumer<ClientUser, ChatMessage> handler;
    public static final BiConsumer<ClientUser, ChatMessage> messageHandler1 = (user, chat) -> chat.format("Added {} to your block list.", user.name);
    public static final BiConsumer<ClientUser, ChatMessage> messageHandler2 = (user, chat) -> chat.format("{} is not blocked.", user.name);
    public static final BiConsumer<ClientUser, ChatMessage> messageHandler3 = (user, chat) -> chat.format("You are not friends with {}!", user.name);
    public static final BiConsumer<ClientUser, ChatMessage> messageHandler4 = (user, chat) -> chat.format("{} has blocked you.", user.name);
    public static final BiConsumer<ClientUser, ChatMessage> messageHandler5 = (user, chat) -> chat.format("You have removed {} from your friends list!", user.name);
    public static final BiConsumer<ClientUser, ChatMessage> messageHandler6 = (user, chat) -> chat.format("You can't invite {} because of their invite settings!", user.name);
    public static final BiConsumer<ClientUser, ChatMessage> messageHandler7 = (user, chat) -> chat.format("{} is not online.", user.name);
    public static final BiConsumer<ClientUser, ChatMessage> messageHandler8 = (user, chat) -> chat.format("You are already friends with {}!", user.name);
    public static final BiConsumer<ClientUser, ChatMessage> messageHandler9 = (user, chat) -> chat.format("You cannot invite yourself!");
    public static final BiConsumer<ClientUser, ChatMessage> messageHandler10 = (user, chat) -> chat.format("{} is already blocked.", user.name);
    public static final BiConsumer<ClientUser, ChatMessage> messageHandler11 = (user, chat) -> chat.format("No user by the name {} exists.", user.name);
    public static final BiConsumer<ClientUser, ChatMessage> messageHandler12 = (user, chat) -> chat.format("You already sent a request to {}!", user.name);
    public static final BiConsumer<ClientUser, ChatMessage> messageHandler13 = (user, chat) -> chat.format("Removed {} from your block list.", user.name);
    public static final BiConsumer<ClientUser, ChatMessage> messageHandler14 = (user, chat) -> chat.format("Friend request sent to {}! They have 30 seconds to accept it.", user.name);
    static {
        ALREADY_FRIENDS.handler = messageHandler8;
        FRIEND_REQUEST_SENT.handler = messageHandler14;
        NOT_ONLINE.handler = messageHandler7;
        INVITE_SETTINGS.handler = messageHandler6;
        REQUEST_ALREADY_SENT.handler = messageHandler12;
        INVITING_YOURSELF.handler = messageHandler9;
        BLOCKED.handler = messageHandler4;
        NOT_FRIENDS_WITH.handler = messageHandler3;
        NO_USER_EXISTS.handler = messageHandler11;
        REMOVED_FRIEND.handler = messageHandler5;
        ALREADY_BLOCKED.handler = messageHandler10;
        ALREADY_UNBLOCKED.handler = messageHandler2;
        USER_BLOCKED.handler = messageHandler1;
        USER_UNBLOCKED.handler = messageHandler13;
    }
}
