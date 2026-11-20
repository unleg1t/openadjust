package com.adjustclient.utilities.hypixel.api.model.player;

import com.google.gson.annotations.SerializedName;

public final class HypixelSocialLinks {
    @SerializedName("YOUTUBE")
    public String youtube;
    @SerializedName("TWITCH")
    public String twitch;
    @SerializedName("TWITTER")
    public String twitter;
    @SerializedName("DISCORD")
    public String discord;
    @SerializedName("HYPIXEL")
    public String hypixel;

    public HypixelSocialLinks() {
    }
}
