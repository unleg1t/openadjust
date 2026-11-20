package com.adjustclient.core.gui.chat;

import com.adjustclient.Adjust;
import com.adjustclient.core.events.EventBus;
import com.adjustclient.core.events.ChatSendEvent;
import com.adjustclient.core.events.Listener;
import com.adjustclient.core.settings.property.CommandManager;
import com.adjustclient.core.commands.CommandBase;
import com.adjustclient.managers.globalchat.GlobalChatChannel;
import com.adjustclient.utilities.MinecraftInstance;
import com.adjustclient.utilities.Timer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatColor;
import com.adjustclient.modules.impl.render.hud.GlobalChatMode;

public final class ChatChannelManager implements Listener, MinecraftInstance {
    public List<ChatChannel> channels = new ArrayList<>();
    public List<ChatChannel> allChannels = new ArrayList<>();
    public ChatChannel selected;
    public GlobalChatChannel globalChannel;
    public ChatChannel minecraftChannel;
    public Object field24;

    public void initialize() {
        java.util.function.Consumer<String> minecraftSender = text -> {
                    if (mc.thePlayer == null) return;
                    CommandManager commands = (CommandManager) CommandManager.INSTANCE;
                    if (commands != null && commands.isCommand(text)) {
                        int space = text.indexOf(' ');
                        String label = text.substring(1, space == -1 ? text.length() : space);
                        String rest = space == -1 ? "" : text.substring(space + 1).trim();
                        String[] args = rest.isEmpty() ? new String[0] : rest.split(" ");
                        List<CommandBase> found = commands.findCommands(label, true);
                        if (found.isEmpty()) commands.reportUnknownCommand(label);
                        else {
                            CommandBase command = found.get(0);
                            command.execution = new CommandManager.ResultConsumer(label);
                            if (command.execute(label, args)) command.completeExecution();
                        }
                        ChatSendEvent consumed = new ChatSendEvent(text, true);
                        consumed.cancelled = true;
                        EventBus.post(consumed);
                        return;
                    }
                    if (Adjust.chatTextReplacer != null) text = Adjust.chatTextReplacer.replace(text);
                    ChatSendEvent event = new ChatSendEvent(text, true);
                    EventBus.post(event);
                    if (!event.cancelled) mc.thePlayer.sendChatMessage(event.message);
                };
        var chatGui = mc != null && mc.ingameGUI != null ? mc.ingameGUI.getChatGUI() : null;
        if (minecraftChannel != null) {
            syncMinecraftHistory();
            if (selected == null) selectChannel(minecraftChannel);
            return;
        }
        minecraftChannel = new ChatChannel("Minecraft", chatGui, minecraftSender, 0);
        addChannel(minecraftChannel);
        if (globalChannel == null) {
            globalChannel = new GlobalChatChannel();
            addChannel(globalChannel);
        }
        syncMinecraftHistory();
        com.adjustclient.managers.config.user.UserConfiguration.chatMode.notifyListeners(
                com.adjustclient.managers.config.user.UserConfiguration.chatMode.getValue(),
                com.adjustclient.managers.config.user.UserConfiguration.chatMode.getValue());
    }

    /** Binds {@code mc.ingameGUI.getChatGUI()} once {@code GuiIngame} has been constructed. */
    public void syncMinecraftHistory() {
        var chatGui = mc != null && mc.ingameGUI != null ? mc.ingameGUI.getChatGUI() : null;
        if (minecraftChannel != null && minecraftChannel.history == null && chatGui != null) {
            minecraftChannel.history = chatGui;
        }
    }
    public void updateConnection() {
    }

    @SuppressWarnings("unused")
    private void vendorUpdateConnection() {
        GlobalChatChannel global = globalChannel;
        if (selected == global) {
            if (Adjust.hydraConnection.authenticated && Adjust.hydraConnection.ready) {
                if (global.pendingMessages == null) global.loadMessages();
                else {
                    if (global.field48 == null) global.field48 = new Timer();
                    ((Timer) global.field48).reset();
                }
            } else global.showLoading("Establishing connection to Hydra", ChatColor.RED);
        }
        if (com.adjustclient.managers.config.user.UserConfiguration.chatMode.getValue()
                != com.adjustclient.modules.impl.render.hud.GlobalChatMode.ALWAYS) {
            if (global.field48 != null && ((Timer) global.field48).lastMs + 300000L <= System.currentTimeMillis()) {
                global.pendingMessages = null;
                global.locallySubscribed = false;
                global.field48 = null;
                new com.adjustclient.utilities.backend.api.request.globalchat.SubscribeRequest().toPacket().sendAsync(null);
            }
        } else if (Adjust.hydraConnection.authenticated && Adjust.hydraConnection.ready) {
            if (global.pendingMessages == null) global.loadMessages();
            if (global.field48 == null) global.field48 = new Timer();
        }
    }
    public void removeChannel(ChatChannel channel) {
        channels.remove(channel);
        if (selected == channel) selected = channels.isEmpty() ? null : channels.getFirst();
        sortChannels();
    }
    public void selectChannel(ChatChannel channel) {
        if (selected instanceof GlobalChatChannel) ((GlobalChatChannel) selected).field20 = null;
        selected = Objects.requireNonNull(channel);
    }
    public void addChannel(ChatChannel channel) {
        if (channels.contains(channel)) return;
        channels.add(channel);
        allChannels.add(channel);
        if (selected == null) selectChannel(channel);
        sortChannels();
    }
    public void sortChannels() {
        channels.sort(Comparator.comparingInt(channel -> channel.sortOrder));
    }
}
