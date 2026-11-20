package com.adjustclient.utilities.anticheat.player.suspicion;

import com.adjustclient.modules.impl.render.hud.playerlist.stats.PlayerEntry;
import com.adjustclient.utilities.anticheat.ServerTracker;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.utilities.hypixel.api.model.player.HypixelPlayer;
import com.adjustclient.utilities.hypixel.api.model.player.stats.GameStats;

public final class HypixelStatsCheckC extends SuspicionCheck {
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
        Float value = stats.getWinLossRatio();
        Integer divisor = stats.getWlThreshold();
        if (value == null || divisor == null) return score;
        double bonus = (float) (value / divisor.floatValue()) - 0.03;
        Integer games = stats.getGamesPlayed();
        if (games != null) bonus -= 0.1 / games.intValue();
        return score + bonus;
    }
}
