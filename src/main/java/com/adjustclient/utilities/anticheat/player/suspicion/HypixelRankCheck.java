package com.adjustclient.utilities.anticheat.player.suspicion;

import com.adjustclient.modules.impl.render.hud.playerlist.stats.PlayerEntry;
import com.adjustclient.utilities.anticheat.ServerTracker;
import com.adjustclient.utilities.server.ServerType;
import com.adjustclient.utilities.hypixel.api.model.player.HypixelPlayer;
import com.adjustclient.utilities.hypixel.api.model.player.HypixelRank;

public final class HypixelRankCheck extends SuspicionCheck {
    public Object field4;
    @Override public boolean applies() {
        return ServerTracker.INSTANCE.serverState != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == ServerType.HYPIXEL;
    }
    @Override public double score(PlayerEntry player, double score) {
        HypixelPlayer data = player.hypixelStats;
        if (data == null) return score;
        // getEffectiveRank() retains the NONE/NORMAL fallback to monthly/package rank.
        String rank = data.getEffectiveRank();
        if (rank == null) return score;
        return switch (HypixelRank.valueOf(rank)) {
            case VIP -> score * 0.95;
            case VIP_PLUS -> score * 0.9;
            case MVP -> score * 0.83;
            case MVP_PLUS -> score * 0.8;
            case SUPERSTAR -> score * 0.75;
            case YOUTUBER -> score * 0.3;
            case STAFF -> 0.0;
            case NONE -> score;
        };
    }
}
