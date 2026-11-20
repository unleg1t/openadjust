package com.adjustclient.utilities.hypixel.api.model.events;

import com.google.gson.annotations.SerializedName;

public final class EventTimerEntry {
    @SerializedName("event")
    public String event;
    @SerializedName("time")
    public long time;
    @SerializedName("name")
    public String name;

    public EventTimerEntry() {
    }
}
