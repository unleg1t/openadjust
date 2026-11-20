package com.adjustclient.utilities.hypixel.api.client.karma;

import com.adjustclient.utilities.hypixel.api.client.PlayerApiClient;
import com.adjustclient.utilities.hypixel.api.model.ApiResult;
import com.adjustclient.utilities.hypixel.api.model.player.HypixelPlayer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.util.UUIDTypeAdapter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class TwentyFiveKarmaApiClient extends PlayerApiClient {
    private static final Gson GSON;
    private final HttpClient client;
    private final Duration timeout;
    private final String nonce;
    private final Object clockLock = new Object();
    private boolean clockRequested;
    private long clockOffsetSeconds;

    static {
        GSON = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();
    }

    public TwentyFiveKarmaApiClient() {
        super(60000L, 12);
        timeout = Duration.ofSeconds(15);
        client = HttpClient.newBuilder().connectTimeout(timeout).build();
        nonce = TwentyFiveKarmaDeobfuscator.randomNonce();
    }

    public HttpResponse<byte[]> send(HttpRequest request) throws IOException {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("request interrupted", e);
        }
    }

    @Override public ApiResult<HypixelPlayer> fetchPlayer(UUID uuid) throws IOException {
        return new ApiResult<>(fetchObfuscated(uuid).player, true);
    }

    public String responseBody(HttpResponse<byte[]> response) throws IOException {
        if (response.statusCode() == 429) exhaust();
        if (response.statusCode() != 200) throw new IOException("Bad status code: " + response.statusCode());
        return new String(response.body(), StandardCharsets.UTF_8);
    }

    public String requestPlayer(UUID uuid, boolean obfuscated) throws IOException {
        String url = "https://karma-25.uc.r.appspot.com/player/" + uuid;
        Instant now = Instant.now();
        long offset = synchronizeClock();
        String range = TwentyFiveKarmaDeobfuscator.range(now.plusSeconds(offset));
        String encodedNonce = TwentyFiveKarmaDeobfuscator.encodeNonce(nonce);
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url)).timeout(timeout)
                .header("Content-Range", range + " " + encodedNonce)
                .header("Origin", "https://25karma.xyz")
                .header("Referer", "https://25karma.xyz/")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36");
        if (obfuscated) builder.header("x-obf", "1");
        recordRequest();
        try {
            return responseBody(client.send(builder.GET().build(), HttpResponse.BodyHandlers.ofByteArray()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("request interrupted", e);
        }
    }

    public TwentyFiveKarmaResponse fetchObfuscated(UUID uuid) throws IOException {
        String path = "/player/" + uuid;
        String body = requestPlayer(uuid, true);
        try {
            TwentyFiveKarmaEnvelope envelope = GSON.fromJson(body, TwentyFiveKarmaEnvelope.class);
            if (envelope == null || envelope.epoch == 0 || envelope.payload == null)
                throw new IOException("obfuscated response missing epoch or data");
            String key = TwentyFiveKarmaDeobfuscator.responseKey("GET", path, envelope.epoch);
            JsonObject decoded = TwentyFiveKarmaDeobfuscator.decodeObject(envelope.payload, key, 0);
            JsonElement payload = decoded.get("payload");
            if (payload == null) throw new RuntimeException("no payload in deobfuscated response");
            if (!payload.isJsonObject()) throw new RemoteException("payload is not an object");
            TwentyFiveKarmaResponse result = GSON.fromJson(payload, TwentyFiveKarmaResponse.class);
            result.player.displayName = result.mojang.username;
            return result;
        } catch (JsonSyntaxException e) {
            throw new IOException("parsing obfuscated response", e);
        }
    }

    public TwentyFiveKarmaResponse fetchPlain(UUID uuid) throws IOException {
        String body = requestPlayer(uuid, false);
        try {
            JsonObject root = GSON.fromJson(body, JsonObject.class);
            if (!root.get("success").getAsBoolean()) throw new RuntimeException("API error: player not found or request rejected");
            TwentyFiveKarmaResponse result = GSON.fromJson(root, TwentyFiveKarmaResponse.class);
            result.player.displayName = result.mojang.username;
            return result;
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("parsing response", e);
        }
    }

    public long synchronizeClock() {
        synchronized (clockLock) {
            if (!clockRequested) {
                clockRequested = true;
                try {
                    HttpRequest request = HttpRequest.newBuilder(URI.create("https://karma-25.uc.r.appspot.com/epoch/seconds"))
                            .timeout(timeout).GET().build();
                    JsonObject root = GSON.fromJson(responseBody(send(request)), JsonObject.class);
                    if (root != null && root.has("seconds")) {
                        long seconds = root.get("seconds").getAsLong();
                        if (seconds > 0L) {
                            clockOffsetSeconds = seconds - Instant.now().getEpochSecond();
                        }
                    }
                } catch (IOException | RuntimeException ignored) {
                }
            }
            return clockOffsetSeconds;
        }
    }
}
