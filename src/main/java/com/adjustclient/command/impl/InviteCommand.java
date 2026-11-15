package com.adjustclient.command.impl;

import com.adjustclient.command.CommandExecutor;
import com.adjustclient.util.ChatMessage;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatColor;
import com.adjustclient.module.impl.render.streamer.StreamerModule;

public class InviteCommand extends CommandExecutor {
    public Object field24;
    public InviteCommand() {
        super("Invite Code");
        field8 = "Generate an invitation code for a friend to join Adjust.";
        aliases = new String[] {"invitecode", "invite", "invcode"};
    }

    @SuppressWarnings("unused")
    private void vendorHandleResponse(com.adjustclient.backend.api.response.BackendResponse response) {
        if (response.status != com.adjustclient.backend.api.response.ResponseStatus.OK
                || !(response.body instanceof com.adjustclient.backend.api.response.TextResponseBody)) {
            sendMessage(createMessage().format("Unable to get invite code."));
            return;
        }
        String code = ((com.adjustclient.backend.api.response.TextResponseBody) response.body).value;
        char first = code.charAt(0);
        if (first == '>') {
            sendMessage(createMessage().format("You are not allowed to generate invites. Invites have been manually disabled on your account."));
            return;
        }
        if (first == '+') {
            sendMessage(createMessage().format("Trial users are not able to generate invites."));
            return;
        }
        boolean used = first == '-';
        if (used) code = code.substring(1);
        ChatMessage message = createMessage().format("Your invite code is ");
        message.color = ChatColor.WHITE;
        message.strikethrough = used;
        attachCopyAction(message, code);
        if (com.adjustclient.module.impl.render.streamer.StreamerModule.nameMap != null && StreamerModule.INSTANCE.isEnabled()) message.obfuscated = true;
        message.append(code);
        message.format(". ");
        message.format(used ? "This code is already used." : "Share it with a friend!");
        sendReplacingMessage(message);
    }

    public static void attachCopyAction(ChatMessage message, String text) {
        message.clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "",
                () -> com.adjustclient.util.ClipboardUtil.copyToClipboard(text));
        ChatMessage tooltip = new ChatMessage();
        tooltip.color = ChatColor.WHITE;
        tooltip.append("Click to copy!");
        message.hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip.getComponent());
    }

    @Override protected void run(String label, String[] args) {
        sendMessage(createMessage().format("Generating invite codes is unavailable in offline mode."));
    }

    @SuppressWarnings("unused")
    private void vendorRun(String label, String[] args) {
        com.adjustclient.backend.api.PendingRequest pending =
                new com.adjustclient.backend.api.request.client.GenerateInviteCodeRequest().toPacket();
        pending.sendAsync(response -> vendorHandleResponse(response));
    }
}
