package com.adjustclient.utilities.auth;

import com.adjustclient.utilities.HttpClients;
import com.adjustclient.utilities.http.ApiException;
import com.adjustclient.utilities.http.ApiRequest;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class SisuLoginUrlRequest implements ApiRequest<String> {
    public String a() {
        try (HttpClient client = HttpClients.create()) {
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("https://sisu.xboxlive.com/connect/XboxLive/?state=login&cobrandId=8058f65d-ce06-4c30-9559-473c9275a65d&tid=896928775&ru=https://www.minecraft.net/en-us/login?return_url=https%3A%2F%2Fwww.minecraft.net%2Fen-us%2Fmsaprofile%2Fmygames%2Feditprofile&aid=1142970254"));
            CookieFileParser.a(request);
            return client.send(request.build(), HttpResponse.BodyHandlers.ofString())
                    .headers().firstValue("Location").orElseThrow().replaceAll(" ", "%20");
        } catch (Exception e) {
            throw new ApiException(e.getMessage());
        }
    }
}
