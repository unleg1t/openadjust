package com.adjustclient.auth;

import com.adjustclient.util.HttpClients;
import com.adjustclient.util.http.ApiException;
import com.adjustclient.util.http.ApiRequest;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public final class EntitlementsRequest implements ApiRequest<EntitlementResult> {
    private final String accessToken;
    public EntitlementsRequest(String accessToken) { this.accessToken = accessToken; }
    public EntitlementResult a() {
        try (HttpClient client = HttpClients.create()) {
            HttpRequest.Builder request = HttpRequest.newBuilder(new URI("https://api.minecraftservices.com/entitlements/mcstore"));
            CookieFileParser.a(request);
            request.header("Accept", "application/json");
            request.header("Authorization", "Bearer " + accessToken);
            HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
            JsonObject json = new Gson().fromJson(response.body(), JsonObject.class);
            for (JsonElement item : json.getAsJsonArray("items")) {
                if (List.of("game_minecraft", "product_minecraft", "product_game_pass_pc").contains(item.getAsJsonObject().get("name").getAsString())) {
                    return new EntitlementResult(true);
                }
            }
            return new EntitlementResult(false);
        } catch (IOException | InterruptedException e) {
            throw new ApiException(e.getMessage());
        } catch (java.net.URISyntaxException e) {
            throw new ApiException(e.getMessage());
        }
    }
}
