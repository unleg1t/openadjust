package com.adjustclient.hypixel.api.model.player.stats;

import com.google.gson.annotations.SerializedName;

public final class PitStats {
    @SerializedName("profile")
    public com.adjustclient.hypixel.api.model.player.stats.PitProfile profile;

    public PitStats() {
    }
}
