package com.adjustclient.module.impl.render.tweaks;

import com.adjustclient.Adjust;
import com.adjustclient.event.ChatLineAddedEvent;
import com.adjustclient.event.ChatLineEvent;
import com.adjustclient.event.DirectionalPacketEvent;
import com.adjustclient.event.EventHandler;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import com.adjustclient.module.impl.render.streamer.StreamerModule;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.server.ServerType;
import com.adjustclient.setting.BooleanSetting;
import com.adjustclient.setting.ExpandSetting;
import com.adjustclient.setting.NumberSetting;
import com.adjustclient.ui.chat.ChatChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.CommandSuggestions;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiPersistentChat;
import net.minecraft.event.HoverEvent;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.ChatColor;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StringUtils;

public final class ChatTweak extends Tweak {
    public static ChatTweak INSTANCE;
    public final BooleanSetting enabled;
    public final BooleanSetting stackingEnabled;
    public final NumberSetting timeout;
    public final BooleanSetting transparent;
    public final BooleanSetting noLobbyMessages;
    public final BooleanSetting playerHeads;
    public final BooleanSetting modernSuggestions;
    public final Map<GuiPersistentChat, ChatLineKey> stackMap;
    private Object field40;

    public ChatTweak(TweaksModule module) {
        super(module);
        INSTANCE = this;
        stackMap = new HashMap<>();
        ExpandSetting settings = new ExpandSetting(module, "Chat");
        settings.enabledPredicate = this::chatPredicate;
        enabled = new BooleanSetting(settings, "Enabled", true, "Yes", "No");
        ExpandSetting stacking = new ExpandSetting(settings, "Message stacking");
        stacking.enabledPredicate = this::stackingPredicate;
        stackingEnabled = new BooleanSetting(stacking, "Enabled", true, "Yes", "No");
        stackingEnabled.addListener((Runnable) this::clearStackListener);
        timeout = new NumberSetting(stacking, "Timeout", 5.0F, 3.0F, 15.0F, 0.5F)
                .formatter(NumberSetting.SECONDS);
        transparent = new BooleanSetting(settings, "Transparent", false);
        noLobbyMessages = new BooleanSetting(settings, "No lobby messages", false);
        noLobbyMessages.setDescription("Removes join & leave messages in server lobbies");
        playerHeads = new BooleanSetting(settings, "Player heads", true);
        playerHeads.setDescription("Shows player heads beside their chat messages");
        modernSuggestions = new BooleanSetting(settings, "Modern suggestions", false);
        modernSuggestions.setDescription("Shows modern chat completions");
        modernSuggestions.addListener((Runnable) this::clearCommandInput);
        playerHeads.addListener((Runnable) this::updateChatChannel);
        enabled.addListener((Runnable) this::updateGlobalChannel);
    }

    public void clearSuggestions() {
        stackMap.clear();
        refreshSuggestions();
    }

    public boolean isChatEnabled() {
        return module.isEnabled() && isModernSuggestionsEnabled() && modernSuggestions.isEnabled();
    }

    public static void refreshSuggestions() {
        if (mc != null && mc.currentScreen instanceof GuiChat) {
            CommandSuggestions suggestions = ((GuiChat) mc.currentScreen).commandSuggestions;
            if (suggestions != null) suggestions.forkMethod14396d3a0();
        }
        if (Adjust.chatManager == null) return;
        Adjust.chatManager.syncMinecraftHistory();
        for (ChatChannel channel : Adjust.chatManager.channels) {
            if (channel != null && channel.history != null) {
                channel.history.forkMethod143a2f000();
            }
        }
    }

    public void rebuildChannels() {
        stackMap.clear();
        if (Adjust.chatManager != null) {
            Adjust.chatManager.syncMinecraftHistory();
            for (ChatChannel channel : Adjust.chatManager.channels) {
                if (channel != null && channel.history != null) {
                    stackMap.put(channel.history, new ChatLineKey(channel.history));
                }
            }
        }
        refreshSuggestions();
    }

    @EventHandler public void shouldClearStack(com.adjustclient.event.WorldResetEvent event) {
        if (!stackingEnabled.isEnabled()) return;
        for (ChatLineKey history : stackMap.values()) history.hash = null;
        rebuildChannels();
    }

    @EventHandler public void updateStack(ChatLineAddedEvent event) {
        if (!stackingEnabled.isEnabled()) return;
        ChatLineKey history = stackMap.get(event.chat);
        if (history == null || history.hash == null) return;
        history.lines.computeIfAbsent(history.hash, ignored -> {
            return new HashSet<>();
        }).add(event.chatLine);
    }

    @EventHandler public void decorateMessage(ChatLineEvent event) {
        if (!stackingEnabled.isEnabled() || event.removedOldLine) return;
        ChatLineKey history = stackMap.get(event.chat);
        if (!event.pre) {
            if (history != null) history.hash = null;
            return;
        }
        if (event.displayOnly || history == null) return;
        IChatComponent message = event.component;
        String text = StringUtils.stripControlCodes(message.forkMethod14492b400()).trim();
        if (text.isEmpty() || isSeparator(text)) return;
        int hash = hashComponent(message);
        history.hash = hash;
        ChatStackEntry entry = history.counts.get(hash);
        if (entry == null) {
            history.counts.put(hash, new ChatStackEntry());
            return;
        }
        if (entry.timer.hasElapsed((long) (timeout.getValue().floatValue() * 1000.0F))) {
            history.counts.put(hash, new ChatStackEntry());
            return;
        }
        if (!mergeStack(history, hash)) {
            history.counts.put(hash, new ChatStackEntry());
            return;
        }
        entry.timer.reset();
        ++entry.count;
        StackedChatComponent suffix = new StackedChatComponent(" (" + String.format("%,d", entry.count) + ")");
        suffix.getChatStyle().setColor(ChatColor.GRAY);
        message.appendSibling(suffix);
        suffix.getChatStyle().forkMethod144941ea0();
    }

    @EventHandler public void filterLobbyMessage(DirectionalPacketEvent event) {
        if (event.isCancelled() || event.outgoing || !(event.packet instanceof S02PacketChat) || !noLobbyMessages.isEnabled()) return;
        S02PacketChat packet = (S02PacketChat) event.packet;
        if (packet.getType() == 2) return;
        if (ServerTracker.INSTANCE.serverState == null
                || ServerTracker.INSTANCE.serverState.antiCheatModel == null
                || ServerTracker.INSTANCE.serverState.antiCheatModel.serverType != ServerType.HYPIXEL) return;
        IChatComponent message = packet.getChatComponent();
        if (StreamerModule.getChatSenderName(message) == null) return;
        String text = message.forkMethod14492ae00();
        if (text.length() < 6 || text.charAt(0) != '\u00a7' || text.charAt(2) != '['
                || !(text.contains("§f§6") || text.contains("§f §6"))) return;
        if (Logger.debug) {
            Logger.log(LogLevel.DEBUG, true, "Blocked lobby chat message: {}",
                    IChatComponent.Serializer.componentToJson(message));
        }
        event.cancelled = true;
    }

    public static boolean isSeparator(String text) {
        if (text.length() < 5) return false;
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if (c != '-' && c != '=' && c != '\u25ac') return false;
        }
        return true;
    }

    public static int componentHash(ChatStyle style) {
        HoverEvent hover = style.getChatHoverEvent();
        return Objects.hash(style.getColor(), style.getBold(), style.getItalic(), style.getUnderlined(),
                style.getStrikethrough(), style.getObfuscated(), hover == null ? null : hover.getAction(),
                hover == null ? 0 : hashComponent(hover.getValue()), style.getChatClickEvent(), style.getInsertion());
    }

    public static boolean removeOldChatLines(Set<ChatLine> lines) {
        if (mc == null || mc.ingameGUI == null || mc.ingameGUI.persistantChatGUI == null) return false;
        boolean removed = false;
        Iterator<ChatLine> iterator = mc.ingameGUI.persistantChatGUI.chatLines.iterator();
        for (int examined = 0; iterator.hasNext() && examined < 101; ++examined) {
            if (lines.contains(iterator.next())) { iterator.remove(); removed = true; }
        }
        return removed;
    }

    public static int hashComponent(IChatComponent component) {
        List<Integer> hashes = new ArrayList<>();
        for (IChatComponent sibling : component.getSiblings()) {
            if (sibling != null && !(sibling instanceof StackedChatComponent)) hashes.add(hashComponent(sibling));
        }
        if (component instanceof StackedChatComponent) return Objects.hash(hashes);
        return Objects.hash(component.forkMethod14492ae00().trim(), hashes, componentHash(component.getChatStyle()));
    }

    public boolean mergeStack(ChatLineKey history, int hash) {
        if (!history.lines.containsKey(hash)) return false;
        Set<ChatLine> lines = history.lines.get(hash);
        if (lines.isEmpty()) return false;
        history.lines.remove(hash);
        // The first removal deliberately addresses the ordinary in-game chat, not history.chat.
        if (!removeOldChatLines(lines)) return false;
        Set<IChatComponent> originals = Collections.newSetFromMap(new IdentityHashMap<>());
        for (ChatLine line : lines) {
            originals.add(line.originalComponent);
        }
        Iterator<ChatLine> wrapped = mc.ingameGUI.persistantChatGUI.drawnChatLines.iterator();
        for (int examined = 0; wrapped.hasNext() && examined < 301; ++examined) {
            ChatLine line = wrapped.next();
            if (lines.contains(line) || originals.contains(line.originalComponent)) wrapped.remove();
        }
        return true;
    }

    public boolean isModernSuggestionsEnabled() { return enabled.isEnabled(); }

    public boolean isStackingEnabled() {
        return module.isEnabled() && isModernSuggestionsEnabled() && playerHeads.isEnabled();
    }

    private boolean stackingPredicate() { return stackingEnabled.isEnabled(); }
    private boolean chatPredicate() { return enabled.isEnabled(); }
    private void updateChatChannel() { refreshSuggestions(); }
    private void updateGlobalChannel() { refreshSuggestions(); }
    private void clearStackListener() {
        if (stackingEnabled.isEnabled()) rebuildChannels();
        else { stackMap.clear(); refreshSuggestions(); }
    }
    private void clearCommandInput() {
        if (!(mc.currentScreen instanceof GuiChat)) return;
        CommandSuggestions suggestions = ((GuiChat) mc.currentScreen).commandSuggestions;
        if (suggestions == null) return;
        suggestions.completionCandidates.clear();
        suggestions.suggestions.clear();
        suggestions.awaitingCompletionResponse = false;
        suggestions.lastCompletedInput = "";
        suggestions.lastCompletedCursorPosition = -1;
        suggestions.selectedSuggestionIndex = 0;
        suggestions.firstVisibleSuggestionIndex = 0;
    }
    @Override public void onEnable() { rebuildChannels(); }
    @Override public void onDisable() { clearSuggestions(); }
    @Override protected boolean enabledSetting() { return isModernSuggestionsEnabled(); }
}
