package com.adjustclient.render.font.svg;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class SvgResources {

    private static final Map<ResourceLocation, SvgImage> CACHE = new HashMap<>();

    /** Returns the (cached) SVG for {@code resourceLocation}, loading it on first use. */
    public static SvgImage get(ResourceLocation resourceLocation) {
        Objects.requireNonNull(resourceLocation, "resourceLocation");
        SvgImage cached = CACHE.get(resourceLocation);
        if (cached != null) {
            return cached;
        }
        SvgImage image = load(resourceLocation);
        CACHE.put(resourceLocation, image);
        return image;
    }

    private static SvgImage load(ResourceLocation resourceLocation) {
        // the resource's stream field (@+16 of the resource object) is handed to SvgImage and closed via try-with-resources
        try (InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation).getInputStream()) {
            return new SvgImage(stream);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not load SVG resource " + resourceLocation, e);
        }
    }
}
