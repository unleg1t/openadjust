package com.adjustclient.utilities.hypixel.api.client;

import com.adjustclient.utilities.hypixel.api.model.ApiResult;
import com.adjustclient.utilities.hypixel.api.model.player.HypixelPlayer;
import com.adjustclient.utilities.hypixel.api.model.antisniper.AntiSniperBlacklistResponse;
import com.adjustclient.utilities.hypixel.api.model.antisniper.AntiSniperWinstreakResponse;
import com.adjustclient.utilities.hypixel.api.model.antisniper.AntiSniperDenickResponse;
import com.adjustclient.managers.config.user.UserConfiguration;
import com.adjustclient.core.logging.Logger;
import com.adjustclient.core.logging.LogLevel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;
import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class AntiSniperApiClient extends PlayerApiClient {
    public final Map<String, CompletableFuture<AntiSniperWinstreakResponse>> winstreakRequests = new HashMap<>();
    public final Map<String, CompletableFuture<AntiSniperBlacklistResponse>> blacklistRequests = new HashMap<>();
    public AntiSniperApiClient() { super(60000L, 120); }

    @Override public ApiResult<HypixelPlayer> fetchPlayer(UUID uuid) {
        String key = UserConfiguration.ANTI_SNIPER_API_KEY.keyString;
        try {
            HttpsURLConnection connection = openConnection("https://api.antisniper.net/v2/hypixel/player?raw=true&dreams=false&player=" + uuid);
            connection.setRequestProperty("Reason", null);
            connection.setRequestProperty("Apikey", key);
            recordRequest();
            int status = connection.getResponseCode();
            if (status != 200) {
                if (status == 429) exhaust();
                return null;
            }
            try {
                Gson gson = new Gson();
                JsonObject root = gson.fromJson(IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8), JsonObject.class);
                if (!root.has("player") || root.get("player").isJsonNull()) return new ApiResult<>(null, true);
                return new ApiResult<>(gson.fromJson(root.getAsJsonObject("player"), HypixelPlayer.class), true);
            } catch (ClassCastException e) {
                return new ApiResult<>(null, true);
            }
        } catch (IOException e) {
            if (Logger.debug) Logger.log(LogLevel.ERROR, true, "Failed to request AntiSniper API ({}): {}", new Object[]{uuid, e});
            return null;
        }
    }

    public AntiSniperBlacklistResponse fetchBlacklist(String uuid) {
        String key = UserConfiguration.ANTI_SNIPER_API_KEY.keyString;
        try {
            HttpsURLConnection connection = openConnection("https://api.antisniper.net/v2/blacklist?player=" + uuid);
            connection.setRequestProperty("Apikey", key);
            recordRequest();
            int status = connection.getResponseCode();
            if (status != 200) {
                if (status == 429) exhaust();
                return null;
            }
            return new Gson().fromJson(IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8), AntiSniperBlacklistResponse.class);
        } catch (IOException e) {
            if (Logger.debug) Logger.log(LogLevel.ERROR, true, "Failed to request AntiSniper blacklist API ({}): {}", new Object[]{uuid, e});
            return null;
        }
    }

    public AntiSniperWinstreakResponse fetchWinstreak(String uuid) {
        String key = UserConfiguration.ANTI_SNIPER_API_KEY.keyString;
        try {
            HttpsURLConnection connection = openConnection("https://api.antisniper.net/v2/player/winstreak?player=" + uuid);
            connection.setRequestProperty("Apikey", key);
            recordRequest();
            int status = connection.getResponseCode();
            if (status != 200) {
                if (status == 429) exhaust();
                return null;
            }
            return new Gson().fromJson(IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8), AntiSniperWinstreakResponse.class);
        } catch (IOException e) {
            if (Logger.debug) Logger.log(LogLevel.ERROR, true, "Failed to request AntiSniper winstreak API ({}): {}", new Object[]{uuid, e});
            return null;
        }
    }

    public AntiSniperDenickResponse fetchDenick(String nickname) {
        String key = UserConfiguration.ANTI_SNIPER_API_KEY.keyString;
        try {
            HttpsURLConnection connection = openConnection("https://api.antisniper.net/v2/other/denick?nick=" + nickname);
            connection.setRequestProperty("Apikey", key);
            recordRequest();
            int status = connection.getResponseCode();
            if (status != 200) {
                if (status == 429) exhaust();
                return null;
            }
            return new Gson().fromJson(IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8), AntiSniperDenickResponse.class);
        } catch (IOException e) {
            if (Logger.debug) Logger.log(LogLevel.ERROR, true, "Failed to request AntiSniper denick API ({}): {}", new Object[]{nickname, e});
            return null;
        }
    }

    public void removeBlacklistRequest(String uuid, CompletableFuture<AntiSniperBlacklistResponse> future) {
        synchronized (blacklistRequests) {
            if (blacklistRequests.get(uuid) == future) blacklistRequests.remove(uuid);
        }
    }

    public void removeWinstreakRequest(String uuid, CompletableFuture<AntiSniperWinstreakResponse> future) {
        synchronized (winstreakRequests) {
            if (winstreakRequests.get(uuid) == future) winstreakRequests.remove(uuid);
        }
    }

    public CompletableFuture<AntiSniperWinstreakResponse> getWinstreak(String uuid) {
        synchronized (winstreakRequests) {
            CompletableFuture<AntiSniperWinstreakResponse> existing = winstreakRequests.get(uuid);
            if (existing != null) return existing;
            CompletableFuture<AntiSniperWinstreakResponse> future = CompletableFuture.supplyAsync(() -> fetchWinstreak(uuid));
            winstreakRequests.put(uuid, future);
            future.whenComplete((result, error) -> removeWinstreakRequest(uuid, future));
            return future;
        }
    }

    public CompletableFuture<AntiSniperBlacklistResponse> getBlacklist(String uuid) {
        synchronized (blacklistRequests) {
            CompletableFuture<AntiSniperBlacklistResponse> existing = blacklistRequests.get(uuid);
            if (existing != null) return existing;
            CompletableFuture<AntiSniperBlacklistResponse> future = CompletableFuture.supplyAsync(() -> fetchBlacklist(uuid));
            blacklistRequests.put(uuid, future);
            future.whenComplete((result, error) -> removeBlacklistRequest(uuid, future));
            return future;
        }
    }

    public CompletableFuture<AntiSniperDenickResponse> getDenick(String nickname) {
        return CompletableFuture.supplyAsync(() -> fetchDenick(nickname));
    }
}
