package com.adjustclient.utilities.hypixel.api.model.cubelify;

import com.google.gson.annotations.SerializedName;

public class CubelifyPingData {
    @SerializedName("today")
    public com.adjustclient.utilities.hypixel.api.model.cubelify.CubelifyPingStats today;
    @SerializedName("yesterday")
    public com.adjustclient.utilities.hypixel.api.model.cubelify.CubelifyPingStats yesterday;

    public CubelifyPingData() {
    }
}
