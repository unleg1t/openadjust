package com.adjustclient.utilities.hypixel.api.model.player.stats;

import com.google.gson.annotations.SerializedName;
import net.minecraft.util.StringUtils;

public final class CaptureTheWoolStats extends GameStats {
    @SerializedName("stats")
    public com.adjustclient.utilities.hypixel.api.model.player.stats.CaptureTheWoolData stats;

    public CaptureTheWoolStats() {
        this.stats = new CaptureTheWoolData();
    }

    public int getKills() {
        return stats.kills;
    }

    public int getDeaths() {
        return stats.deaths;
    }
}
