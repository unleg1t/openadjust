package com.adjustclient.utilities.hypixel.api.client;

import com.adjustclient.utilities.hypixel.api.model.cubelify.CubelifyResponse;
import com.adjustclient.managers.config.user.UserConfiguration;
import com.adjustclient.core.logging.Logger;
import com.adjustclient.core.logging.LogLevel;
import com.adjustclient.utilities.HttpClients;
import com.google.gson.Gson;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class UrchinApiClient extends ApiClient {
    public final Map<UUID, CompletableFuture<CubelifyResponse>> requestsByPlayer = new HashMap<>();
    public UrchinApiClient() { super(300000L, 600); }

    public CubelifyResponse fetchTags(UUID uuid) {
        String key = UserConfiguration.URCHIN_API_KEY.keyString;
        try (HttpClient client = HttpClients.create()) {
            HttpResponse<String> response = client.send(request("https://api.urchin.gg/v3/cubelify?uuid=" + uuid + "&key=" + key).build(), HttpResponse.BodyHandlers.ofString());
            recordRequest();
            if (response.statusCode() != 200) {
                if (response.statusCode() == 429) exhaust();
                return null;
            }
            return new Gson().fromJson(response.body(), CubelifyResponse.class);
        } catch (IOException | InterruptedException e) {
            if (Logger.debug) Logger.log(LogLevel.ERROR, true, "Failed to request Urchin API ({}): {}", new Object[]{uuid, e});
            return null;
        }
    }

    public CompletableFuture<CubelifyResponse> getTags(UUID uuid) {
        synchronized (requestsByPlayer) {
            CompletableFuture<CubelifyResponse> existing = requestsByPlayer.get(uuid);
            if (existing != null) return existing;
            CompletableFuture<CubelifyResponse> future = CompletableFuture.supplyAsync(() -> fetchTags(uuid));
            requestsByPlayer.put(uuid, future);
            future.whenComplete((result, error) -> removeFailedRequest(uuid, future, result, error));
            return future;
        }
    }

    public void removeFailedRequest(UUID uuid, CompletableFuture<CubelifyResponse> future, CubelifyResponse result, Throwable error) {
        if (result != null && error == null) return;
        synchronized (requestsByPlayer) {
            if (requestsByPlayer.get(uuid) == future) requestsByPlayer.remove(uuid);
        }
    }
}
