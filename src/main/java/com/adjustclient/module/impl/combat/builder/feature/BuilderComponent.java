package com.adjustclient.module.impl.combat.builder.feature;

import com.adjustclient.event.Listener;
import com.adjustclient.module.impl.combat.builder.BuilderModule;
import com.adjustclient.module.impl.movement.scaffold.ScaffoldModule;
import com.adjustclient.util.GameStateUtil;
import com.adjustclient.util.MinecraftInstance;

public abstract class BuilderComponent implements Listener, MinecraftInstance {
    public final BuilderModule owner;

    protected BuilderComponent(BuilderModule owner) { this.owner = owner; }

    public boolean isEnabled() {
        if (!owner.isEnabled() || !((BuilderFeature) this).active) return false;
        if (ScaffoldModule.INSTANCE.isEnabled()
                && ScaffoldModule.INSTANCE.placeReady != null
                && ScaffoldModule.INSTANCE.placeReady) return false;
        return GameStateUtil.isActive();
    }
}
