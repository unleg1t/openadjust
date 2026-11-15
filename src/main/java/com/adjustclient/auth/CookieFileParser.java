package com.adjustclient.auth;

import java.net.HttpURLConnection;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public final class CookieFileParser {
    public static List<Cookie> a(String text) {
        List<Cookie> cookies = new ArrayList<>();
        for (String line : b(text)) {
            if (line.startsWith("#")) continue;
            String[] parts = line.split("\t");
            if (parts.length < 5) continue;
            if (parts[0].endsWith("live.com")) {
                cookies.add(new Cookie(parts[5], parts.length == 6 ? "" : parts[6]));
            }
        }
        return cookies;
    }
    public static void a(HttpURLConnection connection) {
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:130.0) Gecko/20100101 Firefox/130.0");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
    }
    public static void a(HttpRequest.Builder builder) {
        builder.header("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:130.0) Gecko/20100101 Firefox/130.0");
        builder.timeout(Duration.ofSeconds(5));
    }
    public static String[] b(String text) {
        return text.contains("\n") ? text.split("\n") : new String[] {text};
    }
}
