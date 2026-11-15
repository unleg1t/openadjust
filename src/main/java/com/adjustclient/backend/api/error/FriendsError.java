package com.adjustclient.backend.api.error;

import com.adjustclient.util.ChatMessage;
import com.adjustclient.util.ErrorReport;
import com.adjustclient.ui.hud.notification.NotificationBuilder;
import com.adjustclient.user.ClientUser;
import java.util.function.*;

public enum FriendsError {
    ALREADY_FRIENDS, FRIEND_REQUEST_SENT, NOT_ONLINE, INVITE_SETTINGS,
    REQUEST_ALREADY_SENT, INVITING_YOURSELF, BLOCKED, NOT_FRIENDS_WITH,
    NO_USER_EXISTS, REMOVED_FRIEND, ALREADY_BLOCKED, ALREADY_UNBLOCKED,
    USER_BLOCKED, USER_UNBLOCKED;
    public BiConsumer<ClientUser, ChatMessage> handler;
    public static final BiConsumer<ClientUser, ChatMessage> lambda_0fffd7274d1d5b321abe0b4f81b415380 = (user, chat) -> chat.format("Added {} to your block list.", user.name);
    public static final BiConsumer<ClientUser, ChatMessage> lambda_37a7250a0abe9a777c77b1b05abbfe170 = (user, chat) -> chat.format("{} is not blocked.", user.name);
    public static final BiConsumer<ClientUser, ChatMessage> lambda_3e3eeaf962fcb273617d8c6772ddbe5e0 = (user, chat) -> chat.format("You are not friends with {}!", user.name);
    public static final BiConsumer<ClientUser, ChatMessage> lambda_618c962d65c3f4ff5ecdcdb4e6d7de300 = (user, chat) -> chat.format("{} has blocked you.", user.name);
    public static final BiConsumer<ClientUser, ChatMessage> lambda_7360689a330261384a2f3d8e1e24cd420 = (user, chat) -> chat.format("You have removed {} from your friends list!", user.name);
    public static final BiConsumer<ClientUser, ChatMessage> lambda_7caefbaeb03494921cb1f9279d21658d0 = (user, chat) -> chat.format("You can't invite {} because of their invite settings!", user.name);
    public static final BiConsumer<ClientUser, ChatMessage> lambda_7f7189920dfd8da405125258e8500c880 = (user, chat) -> chat.format("{} is not online.", user.name);
    public static final BiConsumer<ClientUser, ChatMessage> lambda_8b0797141867d3aec36291090834cc670 = (user, chat) -> chat.format("You are already friends with {}!", user.name);
    public static final BiConsumer<ClientUser, ChatMessage> lambda_aaf7ac2c09b3dca0e9f68a4abe411ae70 = (user, chat) -> chat.format("You cannot invite yourself!");
    public static final BiConsumer<ClientUser, ChatMessage> lambda_bca3383dabaff518aede554fb14a37900 = (user, chat) -> chat.format("{} is already blocked.", user.name);
    public static final BiConsumer<ClientUser, ChatMessage> lambda_bdeac4370352797d7895b164555829840 = (user, chat) -> chat.format("No user by the name {} exists.", user.name);
    public static final BiConsumer<ClientUser, ChatMessage> lambda_cdff3f849df483c37f532c6789deaaa30 = (user, chat) -> chat.format("You already sent a request to {}!", user.name);
    public static final BiConsumer<ClientUser, ChatMessage> lambda_d878542a10ce602dc0b3e0abc9c490c40 = (user, chat) -> chat.format("Removed {} from your block list.", user.name);
    public static final BiConsumer<ClientUser, ChatMessage> lambda_de39bb2c2d858769e86d2c0f53466ec50 = (user, chat) -> chat.format("Friend request sent to {}! They have 30 seconds to accept it.", user.name);
    static {
        ALREADY_FRIENDS.handler = lambda_8b0797141867d3aec36291090834cc670;
        FRIEND_REQUEST_SENT.handler = lambda_de39bb2c2d858769e86d2c0f53466ec50;
        NOT_ONLINE.handler = lambda_7f7189920dfd8da405125258e8500c880;
        INVITE_SETTINGS.handler = lambda_7caefbaeb03494921cb1f9279d21658d0;
        REQUEST_ALREADY_SENT.handler = lambda_cdff3f849df483c37f532c6789deaaa30;
        INVITING_YOURSELF.handler = lambda_aaf7ac2c09b3dca0e9f68a4abe411ae70;
        BLOCKED.handler = lambda_618c962d65c3f4ff5ecdcdb4e6d7de300;
        NOT_FRIENDS_WITH.handler = lambda_3e3eeaf962fcb273617d8c6772ddbe5e0;
        NO_USER_EXISTS.handler = lambda_bdeac4370352797d7895b164555829840;
        REMOVED_FRIEND.handler = lambda_7360689a330261384a2f3d8e1e24cd420;
        ALREADY_BLOCKED.handler = lambda_bca3383dabaff518aede554fb14a37900;
        ALREADY_UNBLOCKED.handler = lambda_37a7250a0abe9a777c77b1b05abbfe170;
        USER_BLOCKED.handler = lambda_0fffd7274d1d5b321abe0b4f81b415380;
        USER_UNBLOCKED.handler = lambda_d878542a10ce602dc0b3e0abc9c490c40;
    }
}
