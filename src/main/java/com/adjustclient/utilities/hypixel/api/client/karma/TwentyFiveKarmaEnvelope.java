package com.adjustclient.utilities.hypixel.api.client.karma;

import com.google.gson.annotations.SerializedName;
import com.google.gson.JsonObject;

public final class TwentyFiveKarmaEnvelope {
    @SerializedName("a") public long epoch;
    @SerializedName("b") public JsonObject payload;

    public TwentyFiveKarmaEnvelope() { }
}
