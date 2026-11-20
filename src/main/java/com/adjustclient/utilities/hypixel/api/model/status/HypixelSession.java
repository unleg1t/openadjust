package com.adjustclient.utilities.hypixel.api.model.status;

import com.google.gson.annotations.SerializedName;

public final class HypixelSession {
    @SerializedName("gameType")
    public String gameType;
    @SerializedName("mode")
    public String mode;
    @SerializedName("map")
    public String map;

    public HypixelSession() {
    }
}
