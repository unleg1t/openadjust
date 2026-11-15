package com.adjustclient.command.impl;

import com.adjustclient.Adjust;
import com.adjustclient.command.CommandExecutor;
import com.adjustclient.property.UserManager;
import com.adjustclient.service.friends.Friend;
import com.adjustclient.user.ClientUser;
import com.adjustclient.util.ChatMessage;
import com.adjustclient.util.TimeUtil;
import com.adjustclient.logging.Logger;
import com.adjustclient.ui.chat.ChatCommandSuggestions;
import com.adjustclient.logging.LogLevel;
import net.minecraft.util.ChatColor;

public class FriendCommand extends CommandExecutor {
    public Object field24;
    public FriendCommand() {
        super("Friends");
        field8 = "Accept, decline or send a friend request to an Adjust user or manage your current friend list.";
        aliases = new String[] {"friends", "fr", "fl"};
    }

    @SuppressWarnings("unused")
    private void vendorShowError(com.adjustclient.backend.api.error.FriendsError error, ClientUser user) {
        ChatMessage message = createMessage();
        error.handler.accept(user, message);
        sendPersistentMessage(message);
    }

    public void add(String name, ClientUser user) {
        if (user == null) { sendUnknownUser(name); return; }
        sendPersistentMessage(createMessage().append("Unable to send friend request in offline mode."));
    }

    @SuppressWarnings("unused")
    private void vendorAdd(String name, ClientUser user) {
        if (user == null) { sendUnknownUser(name); return; }
        new com.adjustclient.backend.api.request.friends.AddFriendRequest(user).toPacket()
                .sendAsync(response -> vendorHandleResponse(user, response, "send friend request"));
    }

    @SuppressWarnings("unused")
    private void vendorHandleResponse(ClientUser user, com.adjustclient.backend.api.response.BackendResponse response,
            String action) {
        if (response.status == com.adjustclient.backend.api.response.ResponseStatus.OK) {
            if (response.body instanceof com.adjustclient.backend.api.response.EnumResponseBody
                    && ((com.adjustclient.backend.api.response.EnumResponseBody) response.body).value instanceof com.adjustclient.backend.api.error.FriendsError) {
                vendorShowError((com.adjustclient.backend.api.error.FriendsError) ((com.adjustclient.backend.api.response.EnumResponseBody) response.body).value, user);
            } else {
                sendPersistentMessage(createMessage().format("Unknown error occurred: {}.", response));
            }
            return;
        }
        if (response.status == com.adjustclient.backend.api.response.ResponseStatus.RATELIMITED) { sendRateLimited(); return; }
        sendPersistentMessage(createMessage().append("Unable to " + action + "."));
        Logger.log(LogLevel.ERROR, true, "Unable to " + action + " '{}': {}", new Object[] {user.name, response});
    }

    public void remove(String name, ClientUser user) {
        if (user == null) { sendUnknownUser(name); return; }
        sendPersistentMessage(createMessage().append("Unable to remove friend in offline mode."));
    }

    @SuppressWarnings("unused")
    private void vendorRemove(String name, ClientUser user) {
        if (user == null) { sendUnknownUser(name); return; }
        new com.adjustclient.backend.api.request.friends.RemoveFriendRequest(user).toPacket()
                .sendAsync(response -> vendorHandleResponse(user, response, "remove friend"));
    }

    @Override protected void run(String label, String[] args) {
        if (args.length > 2) { sendTooManyArguments(); return; }
        if (args.length == 0 || args[0].equals("list")) { list(); return; }
        if (args.length == 1 || args[0].equalsIgnoreCase("add")) {
            String name = args[args.length == 1 ? 0 : 1];
            ((UserManager) Adjust.INSTANCE.field40).lookup(name, user -> add(name, user));
        } else if (args[0].equalsIgnoreCase("remove")) {
            remove(args[1]);
        } else {
            sendUnknownArgument(String.join(" ", args), "[username]", "add", "remove", "list");
        }
    }

    public void remove(String name) {
        ((UserManager) Adjust.INSTANCE.field40).lookup(name, user -> remove(name, user));
    }

    public void list() {
        var friends = Adjust.INSTANCE.screenStack.friends.view;
        ChatMessage message = new ChatMessage().format("Showing you {} friends", friends.size());
        if (!friends.isEmpty()) {
            message.append("\n");
            ChatCommandSuggestions.appendChatSeparator(message);
            message.append("\n");
            for (Friend friend : friends) {
                message.format("{}: ", friend.user.name);
                if (friend.online) {
                    message.color = ChatColor.GREEN;
                    message.append("online");
                    message.color = ChatColor.GRAY;
                    message.append(" for " + TimeUtil.formatDuration(System.currentTimeMillis() - friend.statusChangedAt, false));
                } else if (friend.statusChangedAt != 0L) {
                    message.color = ChatColor.RED;
                    message.append("last online ");
                    message.color = ChatColor.GRAY;
                    message.append(TimeUtil.timeAgo(friend.statusChangedAt, " ago"));
                } else {
                    message.color = ChatColor.RED;
                    message.append("has never been online");
                }
                message.append("\n");
            }
            ChatCommandSuggestions.appendChatSeparator(message);
        }
        sendReplacingMessage(message);
    }
}
