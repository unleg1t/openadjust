package com.adjustclient.utilities.anticheat.player.suspicion;

import com.adjustclient.modules.impl.render.hud.playerlist.stats.PlayerEntry;
import com.adjustclient.utilities.anticheat.ServerTracker;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.utilities.hypixel.api.model.player.HypixelPlayer;
import com.adjustclient.Adjust;
import com.adjustclient.utilities.hypixel.api.model.player.stats.GameStats;

public final class HypixelStatsCheckE extends SuspicionCheck {
    public Object field4;
    @Override public boolean applies() {
        return ServerTracker.INSTANCE.serverState != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == ServerType.HYPIXEL;
    }
    @Override public double score(PlayerEntry player, double score) {
        HypixelPlayer data = player.hypixelStats;
        if (data == null || data.stats == null) return score;
        switch (Adjust.gameStateTracker.state.game) {
            case UHC_CHAMPIONS, BLITZ_SG, DUELS, BED_WARS_PRACTICE, BED_WARS, SKYWARS:
                GameStats stats = data.stats.getCurrentGameStats();
                if (stats == null) return score * 1.3 + 0.3;
                return score;
            default: return score;
        }
    }
}
