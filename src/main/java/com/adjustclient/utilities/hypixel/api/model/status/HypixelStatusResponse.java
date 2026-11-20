package com.adjustclient.utilities.hypixel.api.model.status;

import com.google.gson.annotations.SerializedName;

public final class HypixelStatusResponse {
    @SerializedName("session")
    public com.adjustclient.utilities.hypixel.api.model.status.HypixelSession session;

    public HypixelStatusResponse() {
    }
}
