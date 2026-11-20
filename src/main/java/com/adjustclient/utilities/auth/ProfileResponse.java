package com.adjustclient.utilities.auth;

import com.google.gson.annotations.SerializedName;

public final class ProfileResponse {
    @SerializedName("id") public String id;
    @SerializedName("name") public String name;
    public ProfileResponse() { }
}
