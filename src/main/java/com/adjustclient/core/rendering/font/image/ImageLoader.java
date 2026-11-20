package com.adjustclient.core.rendering.font.image;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public final class ImageLoader {

    public record ImageKey(ResourceLocation resourceLocation, int flags) {
    }

    private static final Map<ImageKey, NvgImage> CACHE = new HashMap<>();

    public static NvgImage getImage(ResourceLocation location, int flags) {
        ImageKey key = new ImageKey(location, flags);
        if (CACHE.containsKey(key)) {
            return CACHE.get(key);
        }
        try {
            InputStream stream = Minecraft.getMinecraft().mcDefaultResourcePack.getInputStream(location);
            NvgImage image = new NvgImage(stream, flags);
            CACHE.put(key, image);
            return image;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
