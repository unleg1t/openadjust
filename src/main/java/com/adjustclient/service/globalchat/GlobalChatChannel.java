package com.adjustclient.service.globalchat;

import com.adjustclient.Adjust;
import com.adjustclient.ui.chat.ChatChannel;
import com.adjustclient.service.party.ui.ContextMenuProvider;
import com.adjustclient.util.ChatMessage;
import com.adjustclient.util.ChatUtil;
import com.adjustclient.util.Timer;
import com.adjustclient.util.TimeUtil;
import com.adjustclient.logging.Logger;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.user.*;
import java.io.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPersistentChat;
import net.minecraft.util.*;
import net.minecraft.event.HoverEvent;
import com.adjustclient.service.config.user.UserConfiguration;
import net.minecraft.util.ChatColor;
import com.adjustclient.module.impl.render.hud.GlobalChatMode;

public class GlobalChatChannel extends ChatChannel implements ContextMenuProvider {
    public Object field20;
    public HoverEvent channelHover;
    public Timer loadTimer;
    public Timer loadingDotsTimer;
    public final Object lock;
    public Timer sendTimer;
    public List<GlobalChatMessage> pendingMessages;
    public Object field48;
    public boolean locallySubscribed;
    public Object field56;

    public GlobalChatChannel() {
        super("Adjust");
        history = new GuiPersistentChat(Minecraft.getMinecraft());
        sender = text -> {
            GlobalChatChannel channel = Adjust.chatManager.globalChannel;
            if (channel != null) channel.sendMessage(text);
        };
        sortOrder = 1;
        loadTimer = new Timer();
        lock = new Object();
        locallySubscribed = false;
        sendTimer = new Timer();
        history.maxDrawnChatLines = 1200;
        ChatMessage help = new ChatMessage();
        help.color = ChatColor.GOLD;
        help.append("IRC ");
        help.color = ChatColor.WHITE;
        help.append("is enabled globally. ");
        help.color = ChatColor.GRAY;
        help.bold = true;
        help.append("(click for settings)").append("\n");
        help.color = ChatColor.WHITE;
        help.append("Start your messages with ");
        help.color = ChatColor.GOLD;
        help.append(String.valueOf(UserConfiguration.IRC_PREFIX.getChar()));
        help.color = ChatColor.WHITE;
        help.append(" to talk.");
        channelHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, help.getComponent());
    }

    @SuppressWarnings("unused")
    private List<GlobalChatMessage> vendorDecodeMessages(com.adjustclient.backend.api.response.BackendResponse response)
            throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(
                ((com.adjustclient.backend.api.response.DataResponseBody) response.body).value));
        int usersCount = in.readUnsignedShort();
        Map<Integer, ClientUser> users = new HashMap<>(usersCount);
        for (int i = 0; i < usersCount; i++) {
            int id = in.readUnsignedShort();
            users.put(id, Adjust.INSTANCE.field40.getOrCreate(in.readUTF()));
        }
        int count = in.readInt();
        long baseTime = com.adjustclient.util.ByteUtil.readVarLong(in);
        List<GlobalChatMessage> decoded = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ClientUser user = users.get(in.readUnsignedShort());
            String text = in.readUTF();
            int id = in.readInt();
            int color = in.readInt();
            int flags = in.readUnsignedByte();
            long time = baseTime + com.adjustclient.util.ByteUtil.readVarLong(in);
            UserRank rank;
            try { rank = UserRank.values()[flags & 15]; }
            catch (Exception exception) { rank = UserRank.NORMAL; }
            UserBadge badge;
            try { badge = UserBadge.values()[(flags >> 4) & 15]; }
            catch (Exception exception) { badge = UserBadge.REGULAR; }
            decoded.add(new GlobalChatMessage(user, ChatFontStyle.convert(text, ChatColor.WHITE),
                    rank, badge, id, time, color));
        }
        return decoded;
    }

    public void receiveMessage(GlobalChatMessage message) {
        if (pendingMessages == null) {
            Logger.log(LogLevel.WARN, true, "Received message {} while not locally subscribed!", message.id);
            return;
        }
        synchronized (lock) {
            pendingMessages.add(message);
            displayMessage(message, false);
        }
    }

    public void sendMessage(String text) {
        while (text.startsWith(" ")) {
            text = text.substring(1);
            if (text.isEmpty()) return;
        }
        if (text.isEmpty() || loadTimer.lastMs + 150L > System.currentTimeMillis()) return;
        loadTimer.reset();
        ChatMessage message = new ChatMessage();
        message.color = ChatColor.GOLD;
        message.append("[IRC] ");
        message.color = ChatColor.GRAY;
        message.append("Global chat is unavailable in offline mode.");
        ChatUtil.addChatMessage(message.getComponent(), 0, true, ChatUtil.DEFAULT_COLOR);
    }

    @SuppressWarnings("unused")
    private void vendorSendMessage(String text) {
        while (text.startsWith(" ")) {
            text = text.substring(1);
            if (text.isEmpty()) return;
        }
        if (text.isEmpty() || loadTimer.lastMs + 150L > System.currentTimeMillis()) return;
        loadTimer.reset();
        new com.adjustclient.backend.api.request.globalchat.SendGlobalMessageRequest(text) /* /globalchat/sendMessage */
                .toPacket().sendAsync(GlobalChatChannel::vendorSendResponse);
    }

    public void displayMessage(GlobalChatMessage message, boolean history) {
        ChatMessage formatted = new ChatMessage();
        if (message.rank.prefix != null) {
            formatted.color = message.rank.color;
            if (message.rank.description != null)
                formatted.hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, message.rank.description);
            formatted.append(message.rank.prefix + " ");
        }
        if (message.badge != null && message.badge.prefix != null) {
            formatted.color = message.badge.color;
            if (message.badge.description != null)
                formatted.hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, message.badge.description);
            formatted.append("[" + message.badge.prefix + "]");
            formatted.color = ChatColor.RESET;
            formatted.append(" ");
        }
        formatted.color = ChatColor.RESET;
        formatted.append(message.sender.name);
        formatted.color = ChatColor.WHITE;
        formatted.append(": ");
        formatted.color = ChatColor.WHITE;
        formatted.append(message.text);
        int color = message.color == 0 ? -1 : message.color;
        if (!history && UserConfiguration.chatMode.getValue() == GlobalChatMode.ALWAYS) {
            ChatMessage inGame = new ChatMessage();
            inGame.color = ChatColor.GOLD;
            inGame.hoverEvent = channelHover;
            inGame.clickEvent = new net.minecraft.event.ClickEvent(net.minecraft.event.ClickEvent.Action.SUGGEST_COMMAND, "");
            inGame.clickEvent.displayOnly = (Runnable) GlobalChatChannel::openSettings;
            inGame.append("[IRC] ").append(formatted.getComponent());
            ChatUtil.addChatMessage(inGame.getComponent(), 0, false,
                    () -> color);
        }
        IChatComponent timestamp = new ChatComponentText("[" + TimeUtil.formatTimestamp(message.timestamp) + "] ");
        timestamp.setChatStyle(new ChatStyle().setColor(ChatColor.GRAY));
        IChatComponent root = new ChatComponentText("");
        root.appendSibling(timestamp).appendSibling(formatted.getComponent());
        ChatUtil.setColorSupplier(root,
                () -> color);
        this.history.printChatMessage(root);
        if (history || UserConfiguration.chatMode.getValue() == GlobalChatMode.ALWAYS)
            this.history.resetScroll();
    }

    public void showLoading(String text, ChatColor color) {
        if (loadingDotsTimer == null) loadingDotsTimer = new Timer();
        StringBuilder dots = new StringBuilder(text);
        for (int i = 1; i < 5; i++) {
            if (loadingDotsTimer.hasElapsed(1000L * i)) {
                if (i == 4) loadingDotsTimer.reset();
                else dots.append('.');
            }
        }
        if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
            Minecraft.getMinecraft().addScheduledTask(() -> showStatus(color, dots));
            return;
        }
        history.clearChatMessages();
        ChatMessage message = new ChatMessage();
        IChatComponent bullet = new ChatComponentText(" * ");
        bullet.setChatStyle(new ChatStyle().setColor(color).setBold(true));
        message.append(bullet);
        message.color = ChatColor.GRAY;
        message.append(dots.toString());
        printStatus(message.getComponent(), GlobalChatChannel::accentColor);
    }

    public void showStatus(ChatColor color, StringBuilder text) {
        history.clearChatMessages();
        IChatComponent bullet = new ChatComponentText(" * ");
        bullet.setChatStyle(new ChatStyle().setColor(color).setBold(true));
        ChatMessage message = new ChatMessage();
        message.append(bullet);
        message.color = ChatColor.GRAY;
        message.append(text.toString());
        IChatComponent component = message.getComponent();
        ChatUtil.setColorSupplier(component, GlobalChatChannel::accentColor);
        history.printChatMessageWithOptionalDeletion(component, 0, false);
    }

    @SuppressWarnings("unused")
    private void vendorShowStatus(ChatColor color, StringBuilder text) {
        history.clearChatMessages();
        IChatComponent bullet = new ChatComponentText(" * ");
        bullet.setChatStyle(new ChatStyle().setColor(color).setBold(true));
        ChatMessage message = new ChatMessage();
        message.append(bullet);
        message.color = ChatColor.GRAY;
        message.append(text.toString());
        IChatComponent component = message.getComponent();
        ChatUtil.setColorSupplier(component, GlobalChatChannel::accentColor);
        if (!com.adjustclient.backend.auth.Authenticator.authenticated) {
            component = new ChatComponentText("The dog says: bow wow");
        }
        history.printChatMessageWithOptionalDeletion(component, 0, false);
    }

    public void printStatus(IChatComponent component, java.util.function.Supplier<Integer> color) {
        ChatUtil.setColorSupplier(component, color);
        history.printChatMessageWithOptionalDeletion(component, 0, false);
    }

    @SuppressWarnings("unused")
    private void vendorPrintStatus(IChatComponent component, java.util.function.Supplier<Integer> color) {
        ChatUtil.setColorSupplier(component, color);
        IChatComponent rendered = com.adjustclient.backend.auth.Authenticator.authenticated
                ? component
                : new ChatComponentText("The dog says: bow wow");
        synchronized (history.pendingMessagesLock) {
            if (!history.pendingMessages.isEmpty() || !Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
                history.pendingMessages.add(rendered);
                return;
            }
        }
        history.printChatMessageWithOptionalDeletion(rendered, 0, false);
    }

    @SuppressWarnings("unused")
    private void vendorLoadResponse(com.adjustclient.backend.api.response.BackendResponse response) {
        if (response.status != com.adjustclient.backend.api.response.ResponseStatus.OK) {
            Logger.log(LogLevel.ERROR, true, "Failed to load messages due to: {}", response.toString());
        } else {
            try {
                pendingMessages = vendorDecodeMessages(response);
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    history.resetScroll();
                    history.clearChatMessages();
                    synchronized (lock) {
                        for (GlobalChatMessage message : pendingMessages) displayMessage(message, true);
                    }
                });
            } catch (Exception exception) {
                Logger.log(LogLevel.ERROR, true, "Failed to parse messages due to: {}", exception);
            }
        }
        locallySubscribed = false;
        sendTimer.reset();
    }

    @SuppressWarnings("unused")
    private static void vendorSendResponse(com.adjustclient.backend.api.response.BackendResponse response) {
        if (response.status == com.adjustclient.backend.api.response.ResponseStatus.OK) return;
        if (!(response.body instanceof com.adjustclient.backend.api.response.IntResponseBody)) {
            Logger.log(LogLevel.ERROR, true, "Failed to send global message due to: {}", response);
            return;
        }
        GuiPersistentChat chat = Adjust.chatManager.selected.history;
        int mutedUntil = ((com.adjustclient.backend.api.response.IntResponseBody) response.body).value;
        ChatMessage message = new ChatMessage();
        message.color = ChatColor.GOLD;
        message.append("[IRC] ");
        message.color = ChatColor.GRAY;
        message.append("You are muted for ");
        message.color = ChatColor.RED;
        message.append(TimeUtil.timeAgo(System.currentTimeMillis() - mutedUntil, ""));
        message.format(".");
        chat.printChatMessageWithOptionalDeletion(message.getComponent(), (int) 3582794541L);
    }

    public void loadMessages() {
        if (pendingMessages == null) pendingMessages = new ArrayList<>();
        showStatus(ChatColor.YELLOW, new StringBuilder("Global chat is unavailable in offline mode"));
    }

    @SuppressWarnings("unused")
    private void vendorLoadMessages() {
        if (!locallySubscribed && sendTimer.hasElapsed(3000L)) {
            locallySubscribed = true;
            new com.adjustclient.backend.api.request.globalchat.GetMessagesRequest()
                    .toPacket().sendAsync(this::vendorLoadResponse);
        }
        showLoading("Loading messages", ChatColor.RESET);
    }

    public void clearMessages() {
        synchronized (lock) {
            history.clearChatMessages();
            pendingMessages.clear();
            GuiPersistentChat chat = Minecraft.getMinecraft().ingameGUI.getChatGUI();
            chat.removeMatchingLines(this::isChannelLine);
            chat.refreshChat();
        }
    }

    public boolean isChannelLine(net.minecraft.client.gui.ChatLine line) {
        if (line == null || line.getChatComponent() == null) return false;
        IChatComponent component = line.getChatComponent();
        if (component.getChatStyle() == null || component.getChatStyle().colorSupplier == null
                || component.getSiblings().isEmpty()) return false;
        if (component.getSiblings().get(0).getChatStyle() == null) return false;
        return component.getChatStyle().getChatHoverEvent() == channelHover;
    }

    public Runnable clearTask() { return this::clearMessages; }

    public static void sendToActiveChannel(String text) {
        if (Adjust.chatManager.globalChannel != null)
            Adjust.chatManager.globalChannel.sendMessage(text);
    }

    @SuppressWarnings("unused")
    private static java.util.function.Consumer<com.adjustclient.backend.api.response.BackendResponse> vendorSendCallback() {
        return GlobalChatChannel::vendorSendResponse;
    }

    public static Integer accentColor() {
        return com.adjustclient.module.impl.render.hud.HUDModule.animatedColor != null
                ? com.adjustclient.module.impl.render.hud.HUDModule.animatedColor.getRGB()
                : com.adjustclient.module.impl.render.hud.HUDModule.colorSetting.getColor().getRGB();
    }

    @SuppressWarnings("unused")
    private java.util.function.Consumer<com.adjustclient.backend.api.response.BackendResponse> vendorLoadCallback() {
        return this::vendorLoadResponse;
    }

    public java.util.function.Predicate<net.minecraft.client.gui.ChatLine> channelLinePredicate() { return this::isChannelLine; }

    public static void openSettings() { com.adjustclient.util.ScreenUtil.openClickGuiAt(UserConfiguration.chatMode); }
}
