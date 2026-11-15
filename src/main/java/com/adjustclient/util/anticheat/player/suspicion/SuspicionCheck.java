package com.adjustclient.util.anticheat.player.suspicion;

import com.adjustclient.module.impl.render.hud.playerlist.stats.PlayerEntry;

public abstract class SuspicionCheck {
    public boolean applies() { return true; }
    public abstract double score(PlayerEntry player, double score);
}
