package com.adjustclient.module.impl.player.autohypixel.feature.chatstats;

import com.adjustclient.Adjust;
import com.adjustclient.hypixel.HypixelGame;
import com.adjustclient.hypixel.api.model.player.HypixelPlayer;
import com.adjustclient.hypixel.api.model.player.stats.bedwars.BedwarsStats;
import com.adjustclient.module.impl.render.hud.playerlist.StatColorScale;
import com.adjustclient.util.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ChatColor;

public final class BedWarsStatsProvider implements ChatStatsProvider, MinecraftInstance {

    @Override public void showStats(String name, IChatComponent displayName) {
        if (Adjust.gameStateTracker.state.game != HypixelGame.BED_WARS) return;
        CompletableFuture.supplyAsync(() -> {
            try {
                String uuid = WorldUtil.lookupUuid(name);
                return UUID.fromString(uuid.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
            } catch (Exception ignored) { return null; }
        }).thenCompose(uuid -> {
            if (uuid == null) return CompletableFuture.<HypixelPlayer>completedFuture(null);
            HypixelPlayer cached = Adjust.hypixelApiManager.getCachedPlayer(uuid);
            return cached == null ? Adjust.hypixelApiManager.fetchPlayer(uuid) : CompletableFuture.completedFuture(cached);
        }).thenAccept(player -> {
            if (player == null || player.stats == null || player.stats.bedwars == null) return;
            mc.addScheduledTask(() -> displayStats(name, displayName, player));
        });
    }

    public int getPing(String name) {
        if (mc.thePlayer == null || mc.thePlayer.sendQueue == null) return 0;
        for (NetworkPlayerInfo info : mc.thePlayer.sendQueue.getPlayerInfoMap())
            if (info.getGameProfile().getName().equalsIgnoreCase(name)) return info.getResponseTime();
        return 0;
    }

    public void displayStats(String name, IChatComponent displayName, HypixelPlayer player) {
        switch (Adjust.gameStateTracker.state.game) {
            case BED_WARS: break;
            default: return;
        }
        BedwarsStats stats = player.stats.bedwars;
        Float fkd = stats.finalDeaths == 0 ? (stats.finalKills == 0 ? null : (float) stats.finalKills)
                : (float) stats.finalKills / stats.finalDeaths;
        Float wl = stats.getWinLossRatio();
        Float bbl = stats.bedsLost == 0 ? (stats.bedsBroken == 0 ? null : (float) stats.bedsBroken)
                : (float) stats.bedsBroken / stats.bedsLost;
        if (fkd == null || wl == null || bbl == null) return;
        int ping = getPing(name);
        ChatMessage chat = new ChatMessage();
        chat.color = ChatColor.RESET;
        chat.bold = true;
        chat.append("Bedwars").append(" ").append(displayName.createCopy());
        chat.color = ChatColor.WHITE;
        chat.append(" (" + ping + "ms)").append("\n");
        chat.color = ChatColor.WHITE;
        chat.append("FK: ");
        chat.color = StatColorScale.KILLS.getColor(stats.finalKills);
        chat.append(String.format("%,d", stats.finalKills));
        chat.color = ChatColor.DARK_GRAY;
        chat.append(" | ");
        chat.color = ChatColor.WHITE;
        chat.append("FD: ");
        chat.color = StatColorScale.PING.getColor(stats.finalDeaths);
        chat.append(String.format("%,d", stats.finalDeaths));
        chat.color = ChatColor.DARK_GRAY;
        chat.append(" | ");
        chat.color = ChatColor.WHITE;
        chat.append("BB: ");
        chat.color = StatColorScale.GOLD.getColor(stats.bedsBroken);
        chat.append(String.format("%,d", stats.bedsBroken)).append("\n");
        chat.color = ChatColor.WHITE;
        chat.append("FK/D: ");
        chat.color = StatColorScale.KDR.getColor(fkd);
        chat.append(StringUtil.TWO_DECIMALS.format(fkd));
        chat.color = ChatColor.DARK_GRAY;
        chat.append(" | ");
        chat.color = ChatColor.WHITE;
        chat.append("W/L: ");
        chat.color = StatColorScale.WLR.getColor(wl);
        chat.append(StringUtil.TWO_DECIMALS.format(wl));
        chat.color = ChatColor.DARK_GRAY;
        chat.append(" | ");
        chat.color = ChatColor.WHITE;
        chat.append("BB/L: ");
        chat.color = StatColorScale.WLR.getColor(bbl);
        chat.append(StringUtil.TWO_DECIMALS.format(bbl));
        ChatUtil.addChatMessage(chat.getComponent(), 0, true, ChatUtil.DEFAULT_COLOR);
    }
}
