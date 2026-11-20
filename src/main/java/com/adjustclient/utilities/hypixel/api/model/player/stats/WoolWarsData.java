package com.adjustclient.utilities.hypixel.api.model.player.stats;

import com.google.gson.annotations.SerializedName;

public final class WoolWarsData {
    @SerializedName("kills")
    public int kills;
    @SerializedName("deaths")
    public int deaths;
    @SerializedName("wins")
    public int wins;
    @SerializedName("games_played")
    public int gamesPlayed;

    public WoolWarsData() {
    }
}
