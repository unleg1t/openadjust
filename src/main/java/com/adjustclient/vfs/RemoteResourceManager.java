package com.adjustclient.vfs;

import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import com.adjustclient.service.storage.LocalStorage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.SimpleResource;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;

public final class RemoteResourceManager implements IResourceManager {
    public static final RemoteResourceManager INSTANCE = new RemoteResourceManager();
    public static final Set<String> DOMAINS = Collections.singleton("adjust_remote");

    @FunctionalInterface
    public interface ResourceWriter {
        void write(File file) throws IOException;
    }

    private final File assetsDirectory;
    private final IMetadataSerializer metadataSerializer;

    public RemoteResourceManager() {
        String storagePath = LocalStorage.resolveStoragePath();
        this.assetsDirectory = new File(storagePath, "assets");
        if (!this.assetsDirectory.isDirectory() && !this.assetsDirectory.mkdirs()) {
            RuntimeException exception = new RuntimeException("Could not create remote resources directory: " + this.assetsDirectory.mkdir());
            Logger.log(LogLevel.ERROR, true, "Failed to set remote resources directory ({}): {}", storagePath, exception);
            throw new RuntimeException(exception);
        }
        this.metadataSerializer = new IMetadataSerializer();
    }

    public void saveResource(ResourceLocation location, ResourceWriter writer) throws IOException {
        File file = new File(this.assetsDirectory, location.getResourcePath());
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        writer.write(file);
    }

    @Override
    public Set<String> getResourceDomains() {
        return DOMAINS;
    }

    @Override
    public IResource getResource(ResourceLocation location) throws IOException {
        File file = new File(this.assetsDirectory, location.getResourcePath());
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        File mcmetaFile = new File(this.assetsDirectory, location.getResourcePath() + ".mcmeta");
        return new SimpleResource(
                "Adjust remote resources",
                location,
                new FileInputStream(file),
                mcmetaFile.exists() ? new FileInputStream(mcmetaFile) : null,
                this.metadataSerializer
        );
    }

    @Override
    public List<IResource> getAllResources(ResourceLocation location) throws IOException {
        return Collections.singletonList(this.getResource(location));
    }
}
