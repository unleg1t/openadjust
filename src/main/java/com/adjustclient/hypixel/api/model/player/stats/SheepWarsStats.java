package com.adjustclient.hypixel.api.model.player.stats;

import com.google.gson.annotations.SerializedName;
import net.minecraft.util.StringUtils;

public final class SheepWarsStats extends GameStats {
    @SerializedName("stats")
    public com.adjustclient.hypixel.api.model.player.stats.SheepWarsData stats;

    public SheepWarsStats() {
        this.stats = new SheepWarsData();
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
        return stats.losses;
    }
}
