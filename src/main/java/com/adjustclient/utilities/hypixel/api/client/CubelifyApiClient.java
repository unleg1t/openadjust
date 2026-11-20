package com.adjustclient.utilities.hypixel.api.client;

import com.adjustclient.utilities.hypixel.api.model.ApiResult;
import com.adjustclient.utilities.hypixel.api.model.player.HypixelPlayer;
import com.adjustclient.core.commands.impl.CubelifyCommand;
import com.adjustclient.utilities.hypixel.api.model.cubelify.CubelifyPingResponse;
import java.util.concurrent.CompletableFuture;
import com.adjustclient.utilities.HttpClients;
import com.adjustclient.core.logging.Logger;
import com.adjustclient.core.logging.LogLevel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.UUID;

public final class CubelifyApiClient extends PlayerApiClient {
    public CubelifyApiClient() { super(60000L, 32); }

    public ApiResult<HypixelPlayer> fetchPlayer(UUID uuid) {
        String key = com.adjustclient.managers.config.user.UserConfiguration.INSTANCE.cubelifyToken;
        if (key == null) return null;
        try (HttpClient client = HttpClients.create()) {
            HttpResponse<String> response = client.send(request("https://api.cubelify.com/proxy/hypixel/player/" + uuid)
                    .header("authorization", "Bearer " + key).GET().build(), HttpResponse.BodyHandlers.ofString());
            recordRequest();
            if (response.statusCode() != 200) {
                if (response.statusCode() == 429 && !JsonParser.parseString(response.body()).getAsJsonObject()
                        .get("cause").getAsString().equals("You have already looked up this player too recently, please try again shortly")) {
                    exhaust();
                }
                return null;
            }
            {
                Gson gson = new Gson();
                JsonObject root = gson.fromJson(response.body(), JsonObject.class);
                if (!root.has("data") || root.get("data").isJsonNull()) return new ApiResult<>(null, true);
                return new ApiResult<>(gson.fromJson(root.getAsJsonObject("data"), HypixelPlayer.class), true);
            }
        } catch (IOException | InterruptedException e) {
            if (Logger.debug) Logger.log(LogLevel.ERROR, true, "Failed to request Hypixel API ({}): {}", new Object[]{uuid, e});
            return null;
        }
    }

    public CubelifyPingResponse fetchPing(UUID uuid) {
        String key = com.adjustclient.managers.config.user.UserConfiguration.INSTANCE.cubelifyToken;
        if (key == null) return null;
        try (HttpClient client = HttpClients.create()) {
            HttpResponse<String> response = client.send(request("https://api.cubelify.com/proxy/seraph/ping/today/" + uuid)
                    .header("authorization", "Bearer " + key).GET().build(), HttpResponse.BodyHandlers.ofString());
            recordRequest();
            if (response.statusCode() != 200) {
                if (response.statusCode() == 429) exhaust();
                return null;
            }
            return new Gson().fromJson(response.body(), CubelifyPingResponse.class);
        } catch (IOException | InterruptedException e) {
            if (Logger.debug) Logger.log(LogLevel.ERROR, true, "Failed to request Cubelify Ping API ({}): {}", new Object[]{uuid, e});
            return null;
        }
    }

    public CompletableFuture<CubelifyPingResponse> getPing(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> fetchPing(uuid));
    }
}
