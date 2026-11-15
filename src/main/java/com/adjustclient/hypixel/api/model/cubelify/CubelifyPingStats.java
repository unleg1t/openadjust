package com.adjustclient.hypixel.api.model.cubelify;

import com.google.gson.annotations.SerializedName;

public class CubelifyPingStats {
    @SerializedName("data")
    public String data;
    @SerializedName("max")
    public int max;
    @SerializedName("min")
    public int min;
    @SerializedName("avg")
    public int avg;

    public CubelifyPingStats() {
    }
}
