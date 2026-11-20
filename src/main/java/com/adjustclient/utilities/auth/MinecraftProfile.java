package com.adjustclient.utilities.auth;

import com.google.gson.annotations.SerializedName;

public final class MinecraftProfile {
    @SerializedName("id") public String id;
    @SerializedName("name") public String name;
    public MinecraftProfile() { }
}
