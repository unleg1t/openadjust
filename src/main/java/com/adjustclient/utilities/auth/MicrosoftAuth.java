package com.adjustclient.utilities.auth;

import com.adjustclient.core.logging.Logger;
import com.adjustclient.utilities.HttpClients;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public final class MicrosoftAuth {
    public static NameChangeStatus getNameChangeStatus(String token) {
        try (HttpClient client = HttpClients.create()) {
            HttpRequest request = HttpRequest.newBuilder(new URI("https://api.minecraftservices.com/minecraft/profile/namechange"))
                    .header("Authorization", "Bearer " + token).GET().timeout(Duration.ofSeconds(5)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // Parsing happens before status dispatch, including unsuccessful HTTP responses.
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            return switch (response.statusCode()) {
                case 200 -> json.get("nameChangeAllowed").getAsBoolean() ? NameChangeStatus.ALLOWED : NameChangeStatus.COOLDOWN;
                case 401 -> NameChangeStatus.INVALID;
                case 429 -> NameChangeStatus.TOO_MANY_REQUESTS;
                default -> NameChangeStatus.UNKNOWN;
            };
        } catch (IOException | InterruptedException | URISyntaxException e) {
            Logger.debugStackTrace(e, null);
            return NameChangeStatus.UNKNOWN;
        }
    }

    public static int changeName(String token, String name) {
        try (HttpClient client = HttpClients.create()) {
            HttpRequest request = HttpRequest.newBuilder(new URI("https://api.minecraftservices.com/minecraft/profile/name/" + name))
                    .header("Authorization", "Bearer " + token).header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString("{\"profileName\":\"" + name + "\"}"))
                    .timeout(Duration.ofSeconds(5)).build();
            return client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode();
        } catch (IOException | InterruptedException | URISyntaxException e) {
            Logger.debugStackTrace(e, null);
            return -1;
        }
    }

    public static String post(String url, String body, boolean json) {
        try (HttpClient client = HttpClients.create()) {
            HttpRequest request = HttpRequest.newBuilder(new URI(url))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
                    .header("Content-Type", json ? "application/json" : "application/x-www-form-urlencoded; charset=UTF-8")
                    .header("Accept", "application/json").POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(5)).build();
            return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            Logger.debugStackTrace(e, null);
            return null;
        }
    }

    public static int setSkin(String token, String skinUrl, String variant) {
        try (HttpClient client = HttpClients.create()) {
            HttpRequest request = HttpRequest.newBuilder(new URI("https://api.minecraftservices.com/minecraft/profile/skins"))
                    .header("Authorization", "Bearer " + token).header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"variant\":\"" + variant + "\",\"url\":\"" + skinUrl + "\"}"))
                    .timeout(Duration.ofSeconds(5)).build();
            return client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode();
        } catch (IOException | InterruptedException | URISyntaxException e) {
            Logger.debugStackTrace(e, null);
            return -1;
        }
    }

    public static boolean ownsMinecraft(String token) {
        try (HttpClient client = HttpClients.create()) {
            HttpRequest request = HttpRequest.newBuilder(new URI("https://api.minecraftservices.com/entitlements/mcstore"))
                    .header("Accept", "application/json").header("Authorization", "Bearer " + token)
                    .GET().timeout(Duration.ofSeconds(15)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) return false;
            for (JsonElement item : JsonParser.parseString(response.body()).getAsJsonObject().getAsJsonArray("items")) {
                if (List.of("game_minecraft", "product_minecraft", "product_game_pass_pc").contains(item.getAsJsonObject().get("name").getAsString())) return true;
            }
            return false;
        } catch (IOException | InterruptedException | URISyntaxException e) {
            return false;
        }
    }

    public static int createProfile(String token, String name) {
        try (HttpClient client = HttpClients.create()) {
            HttpRequest request = HttpRequest.newBuilder(new URI("https://api.minecraftservices.com/minecraft/profile"))
                    .header("Authorization", "Bearer " + token).header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"profileName\":\"" + name + "\"}"))
                    .timeout(Duration.ofSeconds(5)).build();
            return client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode();
        } catch (IOException | InterruptedException | URISyntaxException e) {
            Logger.debugStackTrace(e, null);
            return -1;
        }
    }
}
