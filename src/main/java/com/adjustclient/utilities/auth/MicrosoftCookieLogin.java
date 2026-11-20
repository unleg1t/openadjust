package com.adjustclient.utilities.auth;

import com.adjustclient.utilities.http.ApiException;
import com.adjustclient.utilities.http.ApiRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javax.net.ssl.HttpsURLConnection;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class MicrosoftCookieLogin implements ApiRequest<XboxIdentity> {
    private final URI uri;
    private final String cookies;
    public MicrosoftCookieLogin(URI uri, String cookies) { this.uri = uri; this.cookies = cookies; }

    public XboxIdentity a() {
        try {
            URI current = uri;
            for (int redirects = 0; redirects < 5; redirects++) {
                String host = current.getHost();
                if (!"https".equalsIgnoreCase(current.getScheme()) || host == null) {
                    throw new ApiException("Microsoft login returned an invalid redirect URI.");
                }
                if (!host.equalsIgnoreCase("login.microsoftonline.com") && !host.equalsIgnoreCase("login.live.com")
                        && !host.equalsIgnoreCase("sisu.xboxlive.com")) {
                    throw new ApiException("Unexpected Microsoft login redirect host.");
                }
                HttpsURLConnection connection = (HttpsURLConnection) current.toURL().openConnection();
                try {
                    CookieFileParser.a(connection);
                    if (host.equalsIgnoreCase("login.live.com")) connection.setRequestProperty("Cookie", cookies);
                    connection.setInstanceFollowRedirects(false);
                    int status = connection.getResponseCode();
                    if (status < 300 || status >= 400) throw new ApiException("Microsoft login returned HTTP " + status + ".");
                    String location = connection.getHeaderField("Location");
                    if (location == null || location.isBlank()) throw new ApiException("Microsoft login returned no redirect location.");
                    current = current.resolve(location.replace(" ", "%20"));
                } finally {
                    connection.disconnect();
                }
                String accessToken = null;
                if (current.getRawFragment() != null) {
                    for (String part : current.getRawFragment().split("&")) {
                        if (part.startsWith("accessToken=")) {
                            accessToken = URLDecoder.decode(part.substring(12).replace("+", "%2B"), StandardCharsets.UTF_8);
                            break;
                        }
                    }
                }
                if (accessToken == null && current.getRawQuery() != null) {
                    for (String part : current.getRawQuery().split("&")) {
                        if (part.startsWith("accessToken=")) {
                            accessToken = URLDecoder.decode(part.substring(12).replace("+", "%2B"), StandardCharsets.UTF_8);
                            break;
                        }
                    }
                }
                if (accessToken == null) continue;
                if (!"https".equalsIgnoreCase(current.getScheme()) || !"www.minecraft.net".equalsIgnoreCase(current.getHost())) {
                    throw new ApiException("Unexpected Microsoft token redirect host.");
                }
                Base64.Decoder decoder = accessToken.indexOf('-') >= 0 || accessToken.indexOf('_') >= 0
                        ? Base64.getUrlDecoder() : Base64.getDecoder();
                JsonArray tokens = JsonParser.parseString(new String(decoder.decode(accessToken), StandardCharsets.UTF_8)).getAsJsonArray();
                for (JsonElement element : tokens) {
                    JsonObject entry = element.getAsJsonObject();
                    if (!entry.get("Item1").getAsString().equals("rp://api.minecraftservices.com/")) continue;
                    JsonObject tokenData = entry.getAsJsonObject("Item2");
                    String token = tokenData.get("Token").getAsString();
                    JsonArray users = tokenData.getAsJsonObject("DisplayClaims").getAsJsonArray("xui");
                    if (token.isBlank() || users.isEmpty()) throw new ApiException("Microsoft login returned incomplete Xbox token data.");
                    String userHash = users.get(0).getAsJsonObject().get("uhs").getAsString();
                    if (userHash.isBlank()) throw new ApiException("Microsoft login returned incomplete Xbox user data.");
                    return new XboxIdentity("XBL3.0 x=" + userHash + ";" + token);
                }
                throw new ApiException("Microsoft login returned no Minecraft Xbox token.");
            }
            throw new ApiException("Microsoft login exceeded the redirect limit.");
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(e.getMessage());
        }
    }
}
