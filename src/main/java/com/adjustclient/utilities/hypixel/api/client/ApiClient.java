package com.adjustclient.utilities.hypixel.api.client;

import com.adjustclient.utilities.Timer;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;

public abstract class ApiClient {
    public final List<Timer> requests = new ArrayList<>();
    public long windowMillis;
    public int requestLimit;

    protected ApiClient(long windowMillis, int requestLimit) {
        this.windowMillis = windowMillis;
        this.requestLimit = requestLimit;
    }

    public void pruneRequests() {
        synchronized (requests) {
            requests.removeIf(timer -> timer.hasElapsed(windowMillis));
        }
    }

    public static HttpsURLConnection openConnection(String url) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) URI.create(url).toURL().openConnection();
        connection.setConnectTimeout(3000);
        connection.setReadTimeout(2000);
        return connection;
    }

    public void exhaust() {
        while (requests.size() < requestLimit) {
            synchronized (requests) {
                requests.add(new Timer());
            }
        }
    }

    public static HttpRequest.Builder request(String url) {
        return HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(3));
    }

    public void recordRequest() {
        synchronized (requests) {
            requests.add(new Timer());
        }
    }
}
