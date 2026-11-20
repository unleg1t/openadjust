package com.adjustclient.utilities.hypixel.api.model.player;

import com.google.gson.annotations.SerializedName;

public final class HypixelAchievements {
    @SerializedName("bedwars_level")
    public Integer bedwarsLevel;
    @SerializedName("skywars_you_re_a_star")
    public Integer skywarsStar;
    @SerializedName("general_wins")
    public Integer generalWins;

    public HypixelAchievements() {
    }
}
