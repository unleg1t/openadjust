package com.adjustclient.utilities.hypixel.api.model.antisniper;

import com.google.gson.annotations.SerializedName;

public final class AntiSniperDenickResult {
    @SerializedName("uuid")
    public String uuid;
    @SerializedName("ign")
    public String ign;
    @SerializedName("method")
    public String method;
    @SerializedName("percent")
    public String percent;
    @SerializedName("latest_nick")
    public String latestNick;
    @SerializedName("last_seen")
    public Long lastSeen;

    public AntiSniperDenickResult() {
    }
}
