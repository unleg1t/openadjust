package com.adjustclient.utilities.game.render;

import com.adjustclient.core.events.Listener;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.entity.Entity;

public final class FrozenPlayerRenderListener implements Listener {
    public Map<String, FrozenPlayerRenderer> renderers = new HashMap<>();
    public Object field8;

    public FrozenPlayerRenderer find(Entity entity) {
        return renderers.values().stream()
                .filter(renderer -> renderer.entity == entity)
                .findFirst().orElse(null);
    }

    public java.util.function.Function<String, FrozenPlayerRenderer> factory =
            name -> new FrozenPlayerRenderer(name);
}
