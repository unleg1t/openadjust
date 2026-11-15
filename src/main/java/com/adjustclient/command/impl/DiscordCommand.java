package com.adjustclient.command.impl;

import com.adjustclient.command.CommandExecutor;
import com.adjustclient.util.ChatMessage;
import java.io.*;
import java.util.Objects;
import com.adjustclient.module.impl.render.streamer.StreamerModule;
import net.minecraft.util.ChatColor;

public class DiscordCommand extends CommandExecutor {
    public Object field24;

    public DiscordCommand(String name) {
        super(name);
        field8 = "Link your Discord account or view your linked account.";
        aliases = new String[] {"discord"};
    }

    @SuppressWarnings("unused")
    private void vendorHandleResponse(com.adjustclient.backend.api.response.BackendResponse response) {
        if (response.status != com.adjustclient.backend.api.response.ResponseStatus.OK) {
            sendMessage(createMessage().format("Unable to retrieve Discord code."));
            return;
        }
        com.adjustclient.backend.api.response.DataResponseBody body =
                (com.adjustclient.backend.api.response.DataResponseBody) Objects.requireNonNull(response.body, "payload");
        LinkCodeResponse data = new LinkCodeResponse(body.value);
        ChatMessage message = createMessage().format("Your ");
        message.color = ChatColor.BLUE;
        message.append("Discord");
        message.format(" link code is ");
        message.color = ChatColor.WHITE;
        InviteCommand.attachCopyAction(message, data.code);
        if (StreamerModule.nameMap != null && StreamerModule.INSTANCE.isEnabled()) message.obfuscated = true;
        message.append(data.code);
        message.format(", use the ");
        message.color = ChatColor.WHITE;
        message.append("/link code");
        message.format(" command from the Adjust bot in ");
        String invite = "https://discord.com/invite/" + data.invite + "/";
        InviteCommand.attachCopyAction(message, invite);
        message.color = ChatColor.WHITE;
        message.append(invite);
        message.format(" to link your account.");
        sendReplacingMessage(message);
    }

    @Override protected void run(String label, String[] args) {
        sendMessage(createMessage().format("Discord linking is unavailable in offline mode."));
    }

    @SuppressWarnings("unused")
    private void vendorRun(String label, String[] args) {
        com.adjustclient.backend.api.PendingRequest pending =
                new com.adjustclient.backend.api.request.client.LinkDiscordRequest().toPacket();
        pending.sendAsync(response -> vendorHandleResponse(response));
    }

    static class LinkCodeResponse {
        public final String code;
        public final String invite;
        public Object field12;

        LinkCodeResponse(byte[] payload) {
            try (java.io.DataInputStream input = new java.io.DataInputStream(new java.io.ByteArrayInputStream(payload))) {
                code = input.readUTF();
                invite = input.readUTF();
            } catch (Exception exception) {
                throw new RuntimeException("how? payload", exception);
            }
        }
    }
}
