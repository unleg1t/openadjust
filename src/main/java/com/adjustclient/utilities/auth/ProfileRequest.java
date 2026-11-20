package com.adjustclient.utilities.auth;

import com.adjustclient.utilities.HttpClients;
import com.adjustclient.utilities.http.ApiException;
import com.adjustclient.utilities.http.ApiRequest;
import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class ProfileRequest implements ApiRequest<ProfileResponse> {
    private final String accessToken;
    public ProfileRequest(String accessToken, Object unused) { this.accessToken = accessToken; }
    public ProfileResponse a() {
        try (HttpClient client = HttpClients.create()) {
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("https://api.minecraftservices.com/minecraft/profile"));
            CookieFileParser.a(request);
            request.header("Authorization", "Bearer " + accessToken);
            HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 == 4) throw new AuthApiException();
            return new Gson().fromJson(response.body(), ProfileResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new ApiException(e.getMessage());
        }
    }
}
