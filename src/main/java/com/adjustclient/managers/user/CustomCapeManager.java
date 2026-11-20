package com.adjustclient.managers.user;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public final class CustomCapeManager {
    private static final Int2ObjectMap<CustomCape> CAPES = new Int2ObjectOpenHashMap<>();

    private CustomCapeManager() {
    }

    public static CustomCape get(int id) {
        synchronized (CAPES) {
            CustomCape cape = CAPES.computeIfAbsent(id, CustomCape::new);
            cape.ensureDownloaded();
            return cape;
        }
    }
}
