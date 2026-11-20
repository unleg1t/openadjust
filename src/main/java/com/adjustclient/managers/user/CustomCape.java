package com.adjustclient.managers.user;

import com.adjustclient.core.logging.LogLevel;
import com.adjustclient.core.logging.Logger;
import com.adjustclient.utilities.vfs.RemoteResourceManager;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import net.minecraft.util.ResourceLocation;

public final class CustomCape {
    public final ResourceLocation resourceLocation;
    public final int id;
    public volatile boolean exists;
    public volatile boolean downloading;

    public CustomCape(int id) {
        this.resourceLocation = new ResourceLocation("adjust_remote", "capes/" + id + ".png");
        this.id = id;
        this.ensureDownloaded();
    }

    public void ensureDownloaded() {
        if (this.exists || this.downloading) {
            return;
        }
        this.downloading = true;
        CompletableFuture.runAsync(this::download);
    }

    public void download() {
        try {
            try (HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://raw.githubusercontent.com/DrRed96/Adjust-custom-capes/refs/heads/main/" + this.id + ".png"))
                        .build();
                HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
                int status = response.statusCode();
                if (status == 200) {
                    RemoteResourceManager.INSTANCE.saveResource(this.resourceLocation, file -> {
                        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
                            out.write(response.body());
                        }
                    });
                    this.exists = true;
                } else if (status == 404) {
                    Logger.log(LogLevel.ERROR, true, "Could not download cape {} because it does not exist", this.id);
                    return;
                } else {
                    throw new IllegalStateException("Bad status code: " + status);
                }
            }
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, true, "Couldn't download cape: {}", this.id, e);
        }
        this.downloading = false;
    }
}
