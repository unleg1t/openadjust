package com.adjustclient.hypixel.api.model.player.stats;

import com.google.gson.annotations.SerializedName;
import net.minecraft.util.StringUtils;

public final class WoolWarsStats extends GameStats {
    @SerializedName("stats")
    public com.adjustclient.hypixel.api.model.player.stats.WoolWarsData stats;

    public WoolWarsStats() {
        this.stats = new WoolWarsData();
    }

    public int getKills() {
        return stats.kills;
    }

    public int getDeaths() {
        return stats.deaths;
    }

    public Integer getWins() {
        return stats.wins;
    }

    public Integer getLosses() {
        return stats.gamesPlayed - stats.wins;
    }
}
