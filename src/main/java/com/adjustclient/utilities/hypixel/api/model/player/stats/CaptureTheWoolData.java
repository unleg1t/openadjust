package com.adjustclient.utilities.hypixel.api.model.player.stats;

import com.google.gson.annotations.SerializedName;

public final class CaptureTheWoolData {
    @SerializedName("kills")
    public int kills;
    @SerializedName("deaths")
    public int deaths;

    public CaptureTheWoolData() {
    }
}
