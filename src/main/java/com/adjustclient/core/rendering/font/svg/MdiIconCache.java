package com.adjustclient.core.rendering.font.svg;

import com.adjustclient.core.logging.LogLevel;
import com.adjustclient.core.logging.Logger;
import com.adjustclient.utilities.Timer;
import com.adjustclient.utilities.vfs.RemoteResourceManager;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
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
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public final class MdiIconCache {

    public static final MdiIconCache INSTANCE = new MdiIconCache();

    private final Map<ResourceLocation, SvgImage> icons;
    private final Set<ResourceLocation> attempted;
    private final Timer rateLimit;

    private MdiIconCache() {
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
        ResourceLocation location = new ResourceLocation("adjust_remote", "mdi/" + key + ".svg");
        if (this.icons.containsKey(location)) {
            return this.icons.get(location);
        }
        if (this.attempted.contains(location)) {
            return null;
        }
        try {
            return this.loadFromResource(location);
        } catch (FileNotFoundException e) {
            String requested = key;
            CompletableFuture.runAsync(() -> this.download(requested, location));
            return null;
        } catch (Exception e) {
            this.attempted.add(location);
            e.printStackTrace();
            return null;
        }
    }

    private SvgImage loadFromResource(ResourceLocation location) throws IOException {
        try (InputStream in = Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream()) {
            SvgImage image = new SvgImage(in);
            this.icons.put(location, image);
            return image;
        }
    }

    /**
     * Downloads {@code https://api.iconify.design/mdi/<name>.svg?color=white} into the remote resource directory (white icons,
     * tinted at draw time). Runs on the common fork-join pool. Locations that were already attempted are skipped; after a 429
     * answer no download is started for 60 seconds. HTTP 404 leaves the location marked as attempted, other bad statuses are logged.
     */
    private void download(String name, ResourceLocation location) {
        if (this.attempted.contains(location)) {
            return;
        }
        if (this.rateLimit.lastMs + 60000L > System.currentTimeMillis()) {
            return;
        }
        this.attempted.add(location);
        try {
            try (HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()) {
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api.iconify.design/mdi/" + name + ".svg?color=white")).build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                int status = response.statusCode();
                if (status == 200) {
                    RemoteResourceManager.INSTANCE.saveResource(location, file -> {
                        try (FileWriter writer = new FileWriter(file)) {
                            writer.write(response.body());
                        }
                    });
                } else {
                    if (status == 429) {
                        this.rateLimit.lastMs = System.currentTimeMillis();
                    }
                    if (status == 404) {
                        Logger.log(LogLevel.ERROR, true, "Could not download MDI icon {} because it does not exist", name);
                        return;
                    }
                    throw new IllegalStateException("Bad status code: " + status);
                }
            }
            this.attempted.remove(location);
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, true, "Couldn't download MDI icon {}: {}", name, e);
        }
    }
}
