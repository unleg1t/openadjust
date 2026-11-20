package com.adjustclient.utilities.hypixel.api.model.player.stats.bedwars;

import com.google.gson.annotations.SerializedName;

public class BedwarsSlumber {
    @SerializedName("tickets")
    public int tickets;
    @SerializedName("total_tickets_earned")
    public int totalTicketsEarned;

    public BedwarsSlumber() {
    }
}
