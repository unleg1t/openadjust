package com.adjustclient.hypixel.api.client;

import com.adjustclient.hypixel.api.model.ApiResult;
import com.adjustclient.hypixel.api.model.player.HypixelPlayer;
import com.adjustclient.hypixel.api.model.status.HypixelStatusResponse;
import com.adjustclient.service.config.user.UserConfiguration;
import com.adjustclient.util.HttpClients;
import com.adjustclient.logging.Logger;
import com.adjustclient.logging.LogLevel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.UUID;

public final class HypixelApiClient extends PlayerApiClient {
    public HypixelApiClient() { super(300000L, 300); }

    @Override public ApiResult<HypixelPlayer> fetchPlayer(UUID uuid) { return fetchPlayer(uuid, false); }

    public ApiResult<HypixelPlayer> fetchPlayer(UUID uuid, boolean unused) {
        String key = UserConfiguration.HYPIXEL_API_KEY.keyString;
        try (HttpClient client = HttpClients.create()) {
            HttpResponse<String> response = client.send(request("https://api.hypixel.net/player?uuid=" + uuid)
                    .header("API-Key", key).GET().build(), HttpResponse.BodyHandlers.ofString());
            recordRequest();
            if (response.statusCode() != 200) {
                if (response.statusCode() == 429 && !JsonParser.parseString(response.body()).getAsJsonObject()
                        .get("cause").getAsString().equals("You have already looked up this player too recently, please try again shortly")) {
                    exhaust();
                }
                return null;
            }
            try {
                Gson gson = new Gson();
                JsonObject root = gson.fromJson(response.body(), JsonObject.class);
                if (!root.has("player") || root.get("player").isJsonNull()) return new ApiResult<>(null, true);
                return new ApiResult<>(gson.fromJson(root.getAsJsonObject("player"), HypixelPlayer.class), true);
            } catch (ClassCastException e) {
                return new ApiResult<>(null, true);
            }
        } catch (IOException | InterruptedException e) {
            if (Logger.debug) Logger.log(LogLevel.ERROR, true, "Failed to request Hypixel API ({}): {}", new Object[]{uuid, e});
            return null;
        }
    }

    public HypixelStatusResponse fetchStatus(String uuid) {
        String key = UserConfiguration.HYPIXEL_API_KEY.keyString;
        try (HttpClient client = HttpClients.create()) {
            HttpResponse<String> response = client.send(request("https://api.hypixel.net/status?uuid=" + uuid)
                    .header("API-Key", key).GET().build(), HttpResponse.BodyHandlers.ofString());
            recordRequest();
            if (response.statusCode() != 200) {
                if (response.statusCode() == 429) exhaust();
                return null;
            }
            return new Gson().fromJson(response.body(), HypixelStatusResponse.class);
        } catch (IOException | InterruptedException e) {
            if (Logger.debug) Logger.log(LogLevel.ERROR, true, "Failed to request Hypixel API status ({}): {}", new Object[]{uuid, e});
            return null;
        }
    }
}
