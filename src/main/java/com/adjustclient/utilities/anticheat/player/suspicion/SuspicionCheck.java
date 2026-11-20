package com.adjustclient.utilities.anticheat.player.suspicion;

import com.adjustclient.modules.impl.render.hud.playerlist.stats.PlayerEntry;

public abstract class SuspicionCheck {
    public boolean applies() { return true; }
    public abstract double score(PlayerEntry player, double score);
}
