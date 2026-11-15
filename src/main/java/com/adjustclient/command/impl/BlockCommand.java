package com.adjustclient.command.impl;

import com.adjustclient.command.CommandExecutor;
import com.adjustclient.Adjust;
import com.adjustclient.util.ChatMessage;
import com.adjustclient.util.ChatUtil;
import com.adjustclient.property.UserManager;
import com.adjustclient.user.ClientUser;
import com.adjustclient.logging.Logger;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.ui.chat.ChatCommandSuggestions;
import java.util.List;

public final class BlockCommand extends CommandExecutor {
    public Object field24;
    public static final String listSeparator = "\n";

    public BlockCommand(String name) {
        super(name);
        field8 = "Unblock or block an Adjust user from interacting with you or view your blocked list.";
        aliases = new String[] {"block", "ignore"};
    }

    @SuppressWarnings("unused")
    private void vendorShowError(com.adjustclient.backend.api.error.FriendsError error, ClientUser user) {
        ChatMessage message = createMessage();
        error.handler.accept(user, message);
        ChatUtil.addChatMessage(message.getComponent(), 0, true, ChatUtil.DEFAULT_COLOR);
    }

    public void showBlockedUsers(ChatMessage message, List<ClientUser> users) {
        if (users == null) {
            message.format("Failed to retrieve blocked list. You may not be connected to Hydra or you are ratelimited.");
        } else {
            message.format("Showing you {} blocked users", users.size());
            if (users.isEmpty()) { sendReplacingMessage(message); return; }
            message.append("\n");
            ChatCommandSuggestions.appendChatSeparator(message);
            message.append(listSeparator);
            for (ClientUser user : users) {
                message.format("{}", user.name);
                message.append(listSeparator);
            }
            ChatCommandSuggestions.appendChatSeparator(message);
        }
        sendReplacingMessage(message);
    }

    public void blockUser(String name, ClientUser user) {
        if (user == null) { sendUnknownUser(name); return; }
        sendPersistentMessage(createMessage().append("Unable to add to block list in offline mode."));
    }

    @SuppressWarnings("unused")
    private void vendorBlockUser(String name, ClientUser user) {
        if (user == null) { sendUnknownUser(name); return; }
        // /friends/blockUser, 45000 ms, positional fields [ClientUser, Boolean.TRUE].
        // BackendRequest.toPacket/writeField own "Writing field #", "Unknown field type: " and "Unknown type: " diagnostics.
        com.adjustclient.backend.api.PendingRequest pending =
                new com.adjustclient.backend.api.request.friends.BlockUserRequest(user, true).toPacket();
        pending.sendAsync(response -> vendorHandleResponse(user, "add to block list", response));
    }

    @SuppressWarnings("unused")
    private void vendorHandleResponse(ClientUser user, String operation,
            com.adjustclient.backend.api.response.BackendResponse response) {
        if (response.status == com.adjustclient.backend.api.response.ResponseStatus.OK) {
            if (response.body instanceof com.adjustclient.backend.api.response.EnumResponseBody
                    && ((com.adjustclient.backend.api.response.EnumResponseBody) response.body).value instanceof com.adjustclient.backend.api.error.FriendsError) {
                vendorShowError((com.adjustclient.backend.api.error.FriendsError) ((com.adjustclient.backend.api.response.EnumResponseBody) response.body).value, user);
            } else sendPersistentMessage(createMessage().format("Unknown error occurred: {}.", response));
        } else if (response.status == com.adjustclient.backend.api.response.ResponseStatus.RATELIMITED) {
            sendRateLimited();
        } else {
            sendPersistentMessage(createMessage().append("Unable to " + operation + "."));
            Logger.log(LogLevel.ERROR, true, "Unable to " + operation + " '{}': {}", user.name, response);
        }
    }

    public void unblockUser(String name, ClientUser user) {
        if (user == null) { sendUnknownUser(name); return; }
        sendPersistentMessage(createMessage().append("Unable to remove from block list in offline mode."));
    }

    @SuppressWarnings("unused")
    private void vendorUnblockUser(String name, ClientUser user) {
        if (user == null) { sendUnknownUser(name); return; }
        // /friends/blockUser, 45000 ms, positional fields [ClientUser, Boolean.FALSE].
        com.adjustclient.backend.api.PendingRequest pending =
                new com.adjustclient.backend.api.request.friends.BlockUserRequest(user, false).toPacket();
        pending.sendAsync(response -> vendorHandleResponse(user, "remove from block list", response));
    }

    @Override protected void run(String label, String[] args) {
        if (args.length >= 3) { sendTooManyArguments(); return; }
        if (args.length == 0 || args[0].equals("list")) { listBlockedUsers(); return; }
        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) { removeByName(args[1]); return; }
        if (args.length == 2 && !args[0].equalsIgnoreCase("add")) {
            sendUnknownArgument(String.join(" ", args), "[username]", "add", "remove", "list");
            return;
        }
        String name = args[args.length == 1 ? 0 : 1];
        UserManager users = (UserManager) Adjust.INSTANCE.field40;
        if (name.length() < 3 || !name.matches("^[a-zA-Z0-9]*$") || name.length() >= 17) { blockUser(name, null); return; }
        ClientUser user = users.get(name);
        blockUser(name, user);
    }

    @SuppressWarnings("unused")
    private void vendorAddByName(UserManager users, String name) {
        users.pending.put(users.nextRequestId, found -> vendorBlockUser(name, found));
        com.adjustclient.service.util.ServiceUtil.sendToBackend(
                com.adjustclient.backend.transport.HydraPacketEncoder.encodeUserLookup(users.nextRequestId, name));
        ++users.nextRequestId;
    }

    public void removeByName(String name) {
        UserManager users = (UserManager) Adjust.INSTANCE.field40;
        if (name.length() < 3 || !name.matches("^[a-zA-Z0-9]*$") || name.length() >= 17) { unblockUser(name, null); return; }
        ClientUser user = users.get(name);
        unblockUser(name, user);
    }

    @SuppressWarnings("unused")
    private void vendorRemoveByName(UserManager users, String name) {
        users.pending.put(users.nextRequestId, found -> vendorUnblockUser(name, found));
        byte[] packet = com.adjustclient.backend.transport.HydraPacketEncoder.encodeUserLookup(users.nextRequestId, name);
        if (Adjust.hydraConnection != null && Adjust.hydraConnection.authenticated) Adjust.hydraConnection.send(packet);
        ++users.nextRequestId;
    }

    public void listBlockedUsers() {
        showBlockedUsers(new ChatMessage(), null);
    }

    @SuppressWarnings("unused")
    private void vendorListBlockedUsers() {
        ChatMessage message = new ChatMessage();
        new com.adjustclient.backend.api.request.friends.RefreshFriendsRequest().toPacket()
                .sendAsync(response -> showBlockedUsers(message, null));
    }
}
