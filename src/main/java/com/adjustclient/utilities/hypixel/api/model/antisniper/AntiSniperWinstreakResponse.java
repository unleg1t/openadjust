package com.adjustclient.utilities.hypixel.api.model.antisniper;

import com.google.gson.annotations.SerializedName;

public final class AntiSniperWinstreakResponse {
    @SerializedName("overall_accurate")
    public Boolean overallAccurate;
    @SerializedName("overall_winstreak")
    public Integer overallWinstreak;

    public AntiSniperWinstreakResponse() {
    }
}
