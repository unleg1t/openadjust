package com.adjustclient.hypixel.api.model.cubelify;

import com.google.gson.annotations.SerializedName;

public final class CubelifyResponse {
    @SerializedName("score")
    public com.adjustclient.hypixel.api.model.cubelify.CubelifyScore score;
    @SerializedName("tags")
    public com.adjustclient.hypixel.api.model.cubelify.CubelifyTag[] tags;
    @SerializedName("timestamp")
    public String timestamp;

    public CubelifyResponse() {
    }
}
