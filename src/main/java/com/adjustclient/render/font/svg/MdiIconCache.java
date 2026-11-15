package com.adjustclient.render.font.svg;

import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import com.adjustclient.service.storage.LocalStorage;
import com.adjustclient.util.Timer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class MdiIconCache {

    public static final MdiIconCache INSTANCE = new MdiIconCache();

    private final File cacheDirectory;
    private final Map<String, SvgImage> icons;
    private final Set<String> attempted;
    private final Timer rateLimit;
    private Object field20;

    private MdiIconCache() {
        this.cacheDirectory = new File(LocalStorage.resolveStoragePath(), "mdi");
        if (!this.cacheDirectory.isDirectory() && !this.cacheDirectory.mkdirs()) {
            Logger.log(LogLevel.WARN, true, "Could not create MDI icon cache directory: {}", this.cacheDirectory.getPath());
        }
        this.icons = new ConcurrentHashMap<>();
        this.attempted = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.rateLimit = new Timer();
    }

    /**
     * Returns the icon called {@code name} (an optional "mdi-" prefix is stripped, case-insensitive) or {@code null} when it
     * is not available yet. A cache miss that is not yet on disk starts an asynchronous download.
     */
    public SvgImage getIcon(String name) {
        String key = name.toLowerCase();
        if (key.startsWith("mdi-")) {
            key = key.substring(4);
        }
        SvgImage cached = this.icons.get(key);
        if (cached != null) {
            return cached;
        }
        if (this.attempted.contains(key)) {
            return null;
        }
        try {
            return this.loadFromDisk(key);
        } catch (IOException e) {
            String requested = key;
            CompletableFuture.runAsync(() -> this.download(requested));
            return null;
        }
    }

    /** Parses {@code <cacheDirectory>/<name>.svg} and remembers it. Throws FileNotFoundException when it is not cached yet. */
    private SvgImage loadFromDisk(String name) throws IOException {
        try (FileInputStream in = new FileInputStream(new File(this.cacheDirectory, name + ".svg"))) {
            SvgImage image = new SvgImage(in);
            this.icons.put(name, image);
            return image;
        }
    }

    /**
     * Downloads {@code https://api.iconify.design/mdi/<name>.svg?color=white} into the cache directory (white icons, tinted
     * at draw time). Runs on the common fork-join pool. Names that were already attempted are skipped; after a 429 answer
     * no download is started for 60 seconds. HTTP 404 leaves the name marked as attempted, other bad statuses are logged.
     */
    private void download(String name) {
        if (this.attempted.contains(name)) {
            return;
        }
        if (this.rateLimit.lastMs /* lastMs */ + 60000L > System.currentTimeMillis()) {
            return;
        }
        this.attempted.add(name);
        try {
            try (HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()) {
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api.iconify.design/mdi/" + name + ".svg?color=white")).build();   // method "GET"
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                int status = response.statusCode();
                if (status == 200) {
                    try (FileWriter writer = new FileWriter(new File(this.cacheDirectory, name + ".svg"))) {
                        writer.write(response.body());
                    }
                } else {
                    if (status == 429) {
                        this.rateLimit.lastMs = System.currentTimeMillis();
                    }
                    if (status != 404) {
                        throw new IllegalStateException("Bad status code: " + status);
                    }
                    return;                                       // 404: the icon does not exist; the name stays marked as attempted
                }
            }
            this.attempted.remove(name);                          // success: lets the next lookup load the file from disk
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, true, "Couldn't download MDI icon: {}", new Object[] { e });
        }
    }
}
