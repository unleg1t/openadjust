package com.adjustclient.hypixel.api.model.antisniper;

import com.google.gson.annotations.SerializedName;

public final class AntiSniperBlacklistResponse {
    @SerializedName("blacklisted")
    public boolean blacklisted;

    public AntiSniperBlacklistResponse() {
    }
}
