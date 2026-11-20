package com.adjustclient.utilities.hypixel.api.model.cubelify;

import com.google.gson.annotations.SerializedName;

public final class CubelifyTag {
    @SerializedName("color")
    public int color;
    @SerializedName("icon")
    public String icon;
    @SerializedName("tag_name")
    public String tagName;
    @SerializedName("text")
    public String text;
    @SerializedName("textColor")
    public int textColor;
    @SerializedName("tooltip")
    public String tooltip;

    public CubelifyTag() {
    }
}
