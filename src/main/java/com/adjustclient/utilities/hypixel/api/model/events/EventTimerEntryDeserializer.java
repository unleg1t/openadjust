package com.adjustclient.utilities.hypixel.api.model.events;

import com.adjustclient.Adjust;
import com.adjustclient.utilities.hypixel.api.model.player.stats.*;
import com.adjustclient.utilities.hypixel.api.model.player.stats.bedwars.BedwarsStats;
import com.google.gson.annotations.SerializedName;

public final class EventTimerEntryDeserializer implements com.google.gson.JsonDeserializer<java.util.List<EventTimerEntry>> {

    @Override
    public java.util.List<EventTimerEntry> deserialize(com.google.gson.JsonElement json, java.lang.reflect.Type type,
            com.google.gson.JsonDeserializationContext context) {
        java.util.List<EventTimerEntry> entries = new java.util.ArrayList<>();
        if (!json.isJsonArray()) return entries;
        for (com.google.gson.JsonElement element : json.getAsJsonArray()) {
            entries.add(context.deserialize(element, EventTimerEntry.class));
        }
        return entries;
    }
}
