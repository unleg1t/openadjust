package com.adjustclient.utilities.hypixel.game;
import com.google.gson.annotations.SerializedName;
public final class LocrawResponse {
    @SerializedName("server") public String server;
    @SerializedName("gametype") public String gameType;
    @SerializedName("mode") public String mode;
    @SerializedName("map") public String map;
    public String field20;
    public Object field24;
    public LocrawResponse() { }
}
