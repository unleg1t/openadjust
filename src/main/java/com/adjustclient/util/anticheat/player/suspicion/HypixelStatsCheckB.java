package com.adjustclient.util.anticheat.player.suspicion;

import com.adjustclient.module.impl.render.hud.playerlist.stats.PlayerEntry;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.server.ServerType;
import com.adjustclient.hypixel.api.model.player.HypixelPlayer;
import com.adjustclient.hypixel.api.model.player.stats.GameStats;

public final class HypixelStatsCheckB extends SuspicionCheck {
    public Object field4;
    @Override public boolean applies() {
        return ServerTracker.INSTANCE.serverState != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == ServerType.HYPIXEL;
    }
    @Override public double score(PlayerEntry player, double score) {
        HypixelPlayer data = player.hypixelStats;
        if (data == null || data.stats == null) return score;
        GameStats stats = data.stats.getCurrentGameStats();
        if (stats == null) return score;
        Float value = stats.getKillDeathRatio();
        Integer divisor = stats.getKdThreshold();
        if (divisor == null) return score;
        if (value == null) return score + 0.125;
        double bonus = (float) (value / divisor.floatValue()) - 0.1;
        Integer games = stats.getGamesPlayed();
        if (games != null) bonus -= 0.2 / games.intValue();
        return score + bonus;
    }
}
