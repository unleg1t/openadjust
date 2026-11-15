package com.adjustclient.hypixel.api.client.karma;

import com.google.gson.annotations.SerializedName;
import java.util.UUID;

public final class TwentyFiveKarmaMojangInfo {
    @SerializedName("uuid") public UUID uuid;
    @SerializedName("username") public String username;

    public TwentyFiveKarmaMojangInfo() { }
}
