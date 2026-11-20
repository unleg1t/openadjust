package com.adjustclient.managers.message;

import com.adjustclient.Adjust;
import com.adjustclient.core.settings.property.UserManager;
import com.adjustclient.managers.BackendMessageHandler;
import com.adjustclient.managers.friends.Friend;
import com.adjustclient.managers.globalchat.ChatFontStyle;
import com.adjustclient.core.gui.hud.notification.NotificationBuilder;
import com.adjustclient.managers.user.ClientUser;
import com.adjustclient.utilities.ChatMessage;
import com.adjustclient.utilities.ChatUtil;
import com.adjustclient.utilities.SoundUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatColor;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;

public final class DirectMessageService implements BackendMessageHandler {
    public List<ClientUser> recentRecipients = new ArrayList<>();
    public ClientUser lastRecipient;

    public void onDisconnect() { }

    public void receive(ClientUser sender, ClientUser recipient, String text) {
        Friend conversation = null;
        if (recipient.useClientColor) {
            SoundUtil.play("party.message", 0.5F, 1.4F);
            if (sender.friend != null) conversation = sender.friend;
        } else {
            SoundUtil.play("party.message", 0.5F, 1.6F);
            if (recipient.friend != null) conversation = recipient.friend;
        }
        if (conversation != null) conversation.addMessage(sender, text);
        String formatted = ChatFontStyle.convert(text, ChatColor.WHITE);
        printMessage(sender, recipient, formatted);
        var screens = Adjust.INSTANCE.screenStack;
        if (screens.friendsScreen != null && screens.overlays.contains(screens.friendsScreen)) {
            screens.friendsScreen.detailsPane.onMessageAdded(conversation);
            return;
        }
        if (!recipient.useClientColor || Minecraft.getMinecraft().theWorld != null) return;
        NotificationBuilder notification = new NotificationBuilder();
        notification.title = "Message from " + String.valueOf(sender.name);
        notification.parts = new ArrayList<>();
        notification.actions = new ArrayList<>();
        notification.addText(text, 0xFFBEBCBD);
        notification.lifetimeSteps = (int) Math.max(53.0F, text.length() * 1.4299999F);
        notification.show();
    }

    public void send(ClientUser recipient, String text, ChatMessage errorMessage) {
        if (recipient == null) return;
        if (recipient.useClientColor) {
            Adjust.INSTANCE.screenStack.directMessages.receive(recipient, recipient, text);
            return;
        }
        if (errorMessage != null) {
            errorMessage.format("Direct messaging is unavailable in offline mode.");
            ChatUtil.addChatMessage(errorMessage.getComponent(), 0, true, ChatUtil.DEFAULT_COLOR);
        }
    }

    @SuppressWarnings("unused")
    private void vendorSend(ClientUser recipient, String text, ChatMessage errorMessage) {
        if (recipient.useClientColor) {
            Adjust.INSTANCE.screenStack.directMessages.receive(recipient, recipient, text);
            return;
        }
        recentRecipients.remove(recipient);
        recentRecipients.add(0, recipient);
        com.adjustclient.utilities.backend.api.request.friends.MessageRequest request =
                new com.adjustclient.utilities.backend.api.request.friends.MessageRequest(recipient, text);
        request.toPacket().sendAsync(
                response -> vendorHandleResponse(recipient, text, errorMessage, response));
    }

    @SuppressWarnings("unused")
    private void vendorHandleResponse(ClientUser recipient, String text, ChatMessage errorMessage,
            com.adjustclient.utilities.backend.api.response.BackendResponse response) {
        if (response.status == com.adjustclient.utilities.backend.api.response.ResponseStatus.OK) {
            ClientUser self = (ClientUser) ((UserManager) Adjust.INSTANCE.field40).field16;
            Adjust.INSTANCE.screenStack.directMessages.receive(self, recipient, text);
            if (!(response.body instanceof com.adjustclient.utilities.backend.api.response.EnumResponseBody)) return;
            Enum<?> error = ((com.adjustclient.utilities.backend.api.response.EnumResponseBody) response.body).value;
            if (!(error instanceof com.adjustclient.utilities.backend.api.error.MessageError)) return;
            if (error != com.adjustclient.utilities.backend.api.error.MessageError.SELF_INVITE_SETTINGS
                    && error != com.adjustclient.utilities.backend.api.error.MessageError.SELF_BLOCKED) return;
            IChatComponent name = new ChatComponentText(String.valueOf(recipient.name)).setChatStyle(
                    new ChatStyle().setColor(recipient.useClientColor ? ChatColor.RESET : ChatColor.WHITE).setBold(true));
            String warning = error == com.adjustclient.utilities.backend.api.error.MessageError.SELF_BLOCKED
                    ? " cannot reply to your messages because you blocked them"
                    : " cannot reply to your messages because of your invite settings.";
            IChatComponent message = new ChatComponentText("").appendSibling(name).appendSibling(
                    new ChatComponentText(warning).setChatStyle(new ChatStyle().setColor(ChatColor.GRAY)));
            ChatUtil.addChatMessage(message, 0, true, ChatUtil.DEFAULT_COLOR);
            return;
        }
        if (response.body instanceof com.adjustclient.utilities.backend.api.response.EnumResponseBody
                && ((com.adjustclient.utilities.backend.api.response.EnumResponseBody) response.body).value instanceof com.adjustclient.utilities.backend.api.error.MessageError) {
            com.adjustclient.utilities.backend.api.error.MessageError error =
                    (com.adjustclient.utilities.backend.api.error.MessageError) ((com.adjustclient.utilities.backend.api.response.EnumResponseBody) response.body).value;
            error.handler.accept(errorMessage, recipient.name);
        } else {
            errorMessage.format("Failed to send message.");
        }
        ChatUtil.addChatMessage(errorMessage.getComponent(), 0, true, ChatUtil.DEFAULT_COLOR);
    }

    public void onConnect() { }

    public void printMessage(ClientUser sender, ClientUser recipient, String text) {
        ChatMessage builder = new ChatMessage();
        IChatComponent message = builder.getComponent();
        builder.append(new ChatComponentText(String.valueOf(sender.name)).setChatStyle(new ChatStyle()
                .setColor(sender.useClientColor ? ChatColor.RESET : ChatColor.WHITE).setBold(true)));
        message.appendSibling(new ChatComponentText(" -> ").setChatStyle(new ChatStyle().setColor(ChatColor.GRAY)));
        message.appendSibling(new ChatComponentText(String.valueOf(recipient.name)).setChatStyle(new ChatStyle()
                .setColor(recipient.useClientColor ? ChatColor.RESET : ChatColor.WHITE).setBold(true)));
        message.appendSibling(new ChatComponentText(": ").setChatStyle(new ChatStyle().setColor(ChatColor.GRAY)));
        message.appendSibling(new ChatComponentText(String.valueOf(text)).setChatStyle(new ChatStyle().setColor(ChatColor.WHITE)));
        ChatUtil.addChatMessage(message, 0, true, ChatUtil.DEFAULT_COLOR);
        Minecraft mc = Minecraft.getMinecraft();
        var previousChannel = mc.ingameGUI.persistantChatGUI;
        mc.ingameGUI.persistantChatGUI = Adjust.chatManager.globalChannel.history;
        ChatUtil.addChatMessage(message, 0, true, ChatUtil.DEFAULT_COLOR);
        mc.ingameGUI.persistantChatGUI = previousChannel;
    }
}
