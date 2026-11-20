package com.adjustclient.utilities.hypixel.api.client.karma;

import com.google.gson.annotations.SerializedName;
import com.adjustclient.utilities.hypixel.api.model.player.HypixelPlayer;
import com.adjustclient.utilities.hypixel.api.model.status.HypixelSession;

public final class TwentyFiveKarmaResponse {
    @SerializedName("player") public HypixelPlayer player;
    @SerializedName("mojang") public TwentyFiveKarmaMojangInfo mojang;
    @SerializedName("status") public HypixelSession status;

    public TwentyFiveKarmaResponse() { }
}
