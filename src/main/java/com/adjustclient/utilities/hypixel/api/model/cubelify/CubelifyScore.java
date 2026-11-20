package com.adjustclient.utilities.hypixel.api.model.cubelify;

import com.google.gson.annotations.SerializedName;

public final class CubelifyScore {
    @SerializedName("mode")
    public String mode;
    @SerializedName("value")
    public float value;

    public CubelifyScore() {
    }
}
