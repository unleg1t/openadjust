package com.adjustclient.util.anticheat.player.suspicion;

import com.adjustclient.module.impl.render.hud.playerlist.stats.PlayerEntry;
import com.adjustclient.util.anticheat.ServerTracker;
import com.adjustclient.server.ServerType;
import com.adjustclient.hypixel.api.model.player.HypixelPlayer;

public final class AccountAgeCheck extends SuspicionCheck {
    public Object field4;
    @Override public boolean applies() {
        return ServerTracker.INSTANCE.serverState != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel != null
                && ServerTracker.INSTANCE.serverState.antiCheatModel.serverType == ServerType.HYPIXEL;
    }
    @Override public double score(PlayerEntry player, double score) {
        HypixelPlayer data = player.hypixelStats;
        if (data == null || data.firstLogin == null) return score;
        if (data.lastLogin == null) return score * 0.9;
        long age = data.lastLogin.longValue() - data.firstLogin.longValue();
        if (age >= 86400000L) return score;
        double bonus = 1.0 - (int) age / 86400000.0;
        if (age >= 300000L) bonus *= 0.98;
        else if ((int) age < 150000.0) bonus *= 1.1;
        return score + bonus;
    }
}
