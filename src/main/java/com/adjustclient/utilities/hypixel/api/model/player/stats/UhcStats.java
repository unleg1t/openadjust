package com.adjustclient.utilities.hypixel.api.model.player.stats;

import com.google.gson.annotations.SerializedName;
import net.minecraft.util.StringUtils;

public final class UhcStats extends GameStats {
    @SerializedName("coins")
    public int coins;
    @SerializedName("wins")
    public int wins;
    @SerializedName("kills")
    public int kills;
    @SerializedName("deaths")
    public int deaths;

    public UhcStats() {
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public Integer getKdThreshold() {
        return 5;
    }

    public Float getWinLossRatio() {
        return null;
    }
}
