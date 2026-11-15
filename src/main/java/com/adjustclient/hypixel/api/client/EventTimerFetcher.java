package com.adjustclient.hypixel.api.client;

import com.adjustclient.hypixel.api.model.events.EventTimerEntry;
import com.adjustclient.hypixel.api.model.events.EventTimerEntryDeserializer;
import com.adjustclient.util.HttpClients;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.function.Supplier;

public final class EventTimerFetcher {
    public Supplier<List<EventTimerEntry>> fetchTask() {
        return () -> fetch();
    }

    public List<EventTimerEntry> fetch() {
        try (HttpClient client = HttpClients.create()) {
            HttpResponse<String> response = client.send(ApiClient.request(
                    "https://raw.githubusercontent.com/BrookeAFK/brookeafk-api/main/events.js").build(),
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return null;
            return new GsonBuilder().registerTypeAdapter(new EventTimerListTypeToken().getType(),
                    new EventTimerEntryDeserializer()).create().fromJson(response.body(), new EventTimerAdapterTypeToken().getType());
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }
}
