package com.adjustclient.auth;

import com.adjustclient.util.HttpClients;
import com.adjustclient.util.http.ApiException;
import com.adjustclient.util.http.ApiRequest;
import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class LoginWithXboxRequest implements ApiRequest<LoginWithXboxResponse> {
    private final String body;
    public LoginWithXboxRequest(String body) { this.body = body; }

    public LoginWithXboxResponse a() {
        try (HttpClient client = HttpClients.create()) {
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("https://api.minecraftservices.com/authentication/login_with_xbox"));
            CookieFileParser.a(request);
            request.header("Content-Type", "application/json");
            request.header("Accept", "application/json");
            request.POST(HttpRequest.BodyPublishers.ofString(body));
            HttpResponse<String> response = client.send(request.build(), HttpResponse.BodyHandlers.ofString());
            com.google.gson.stream.JsonReader reader = new com.google.gson.stream.JsonReader(new java.io.StringReader(response.body()));
            LoginWithXboxResponse result = new Gson().fromJson(reader, LoginWithXboxResponse.class);
            if (result != null && reader.peek() != com.google.gson.stream.JsonToken.END_DOCUMENT) {
                throw new com.google.gson.JsonSyntaxException("JSON document was not fully consumed.");
            }
            return result;
        } catch (IOException | InterruptedException e) {
            throw new ApiException(e.getMessage());
        }
    }
}
