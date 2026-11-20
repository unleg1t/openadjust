package com.adjustclient.modules.impl.combat.builder.feature;

import com.adjustclient.core.events.Listener;
import com.adjustclient.modules.impl.combat.builder.BuilderModule;
import com.adjustclient.modules.impl.movement.scaffold.ScaffoldModule;
import com.adjustclient.utilities.GameStateUtil;
import com.adjustclient.utilities.MinecraftInstance;

public abstract class BuilderComponent implements Listener, MinecraftInstance {
    public final BuilderModule owner;

    protected BuilderComponent(BuilderModule owner) { this.owner = owner; }

    public boolean isEnabled() {
        if (!owner.isEnabled() || !((BuilderFeature) this).active) return false;
        if (ScaffoldModule.INSTANCE.isEnabled()
                && ScaffoldModule.INSTANCE.placeReady) return false;
        return GameStateUtil.isActive();
    }
}
