package com.adjustclient.util;

public final class HttpClients {
    public static java.net.http.HttpClient create() {
        return java.net.http.HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(5)).build();
    }
}
