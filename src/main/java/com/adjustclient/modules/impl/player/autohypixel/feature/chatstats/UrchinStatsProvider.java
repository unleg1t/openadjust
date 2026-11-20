package com.adjustclient.modules.impl.player.autohypixel.feature.chatstats;

import com.adjustclient.Adjust;
import com.adjustclient.utilities.hypixel.HypixelGameState;
import com.adjustclient.utilities.hypixel.api.HypixelApiManager;
import com.adjustclient.utilities.hypixel.api.model.cubelify.*;
import com.adjustclient.utilities.*;
import java.util.*;
import java.util.concurrent.*;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ChatColor;

public class UrchinStatsProvider implements ChatStatsProvider, MinecraftInstance {
    public final Set<String> requestedNames = ConcurrentHashMap.newKeySet();
    public static HypixelGameState lastState;
    public static final Set<UUID> lobbyPlayers = new HashSet<>();
    public static final Set<UUID> gamePlayers = new HashSet<>();

    @Override public void showStats(String name, IChatComponent displayName) {
        HypixelApiManager api = Adjust.hypixelApiManager;
        if (!api.isUrchinAvailable()) return;
        String key = name.toLowerCase(java.util.Locale.ROOT);
        if (!requestedNames.add(key)) return;
        CompletableFuture.supplyAsync(() -> {
            try {
                String uuid = WorldUtil.lookupUuid(name);
                return UUID.fromString(uuid.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
            } catch (Exception ignored) { return null; }
        }).thenCompose(uuid -> {
            if (uuid == null) return CompletableFuture.<CubelifyResponse>completedFuture(null);
            return api.urchinClient.getTags(uuid);
        }).thenAccept(result -> {
            if (result == null || result.tags == null || result.tags.length == 0) return;
            mc.addScheduledTask(() -> displayTags(displayName, result));
        }).whenComplete((result, error) -> {
            requestedNames.remove(key);
        });
    }

    public static void displayTags(IChatComponent displayName, CubelifyResponse response) {
        if (response == null || response.tags == null) return;
        for (CubelifyTag tag : response.tags) {
            if (tag == null || tag.tooltip == null) continue;
            String[] lines = tag.tooltip.split("\n");
            String reason = lines[0];
            int addedIndex = reason.indexOf(" (Added ");
            String added = null;
            if (addedIndex != -1) {
                added = reason.substring(addedIndex + 8, reason.length() - 1);
                reason = reason.substring(0, addedIndex);
            }
            if (reason.equalsIgnoreCase("Invalid Key")) return;
            ChatMessage information = null;
            for (int i = 1; i < lines.length; i++) {
                if (information == null) {
                    information = new ChatMessage();
                    information.color = ChatColor.RED;
                    information.bold = true;
                    information.append("Information");
                }
                information.append("\n");
                information.color = ChatColor.WHITE;
                information.append(lines[i].startsWith("- ") ? lines[i].substring(2) : lines[i]);
            }
            ChatMessage chat = new ChatMessage();
            chat.bold = true;
            chat.append("Urchin ").append(displayName.createCopy());
            chat.color = ChatColor.WHITE;
            chat.append(" is ");
            chat.color = ChatColor.RED;
            chat.bold = true;
            chat.append("blacklisted ");
            chat.color = ChatColor.WHITE;
            chat.append("for ");
            chat.color = ChatColor.AQUA;
            if (information != null) chat.hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, information.getComponent());
            chat.append(reason);
            if (added != null) {
                chat.color = ChatColor.WHITE;
                chat.append(" (" + added + ")");
            }
            ChatUtil.addChatMessage(chat.getComponent(), 0, true, ChatUtil.DEFAULT_COLOR);
        }
    }

    public static void displayOnce(UUID uuid, IChatComponent displayName, CubelifyResponse response) {
        HypixelGameState current = Adjust.gameStateTracker.state;
        if (current != lastState) {
            lastState = current;
            lobbyPlayers.clear();
            gamePlayers.clear();
        }
        if (current == null) return;
        Set<UUID> seen = current.startTimer == null ? lobbyPlayers : gamePlayers;
        if (seen.add(uuid)) displayTags(displayName, response);
    }
}
