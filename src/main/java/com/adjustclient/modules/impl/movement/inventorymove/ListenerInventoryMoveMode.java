package com.adjustclient.modules.impl.movement.inventorymove;

import com.adjustclient.core.events.Listener;
import com.adjustclient.utilities.MinecraftInstance;

public abstract class ListenerInventoryMoveMode implements Listener, MinecraftInstance, InventoryMoveMode {
    protected final InventoryMoveModule module;
    private final String name;

    protected ListenerInventoryMoveMode(InventoryMoveModule module, String name) {
        this.module = module;
        this.name = name;
    }

    @Override public String getName() { return name; }

    public boolean isActive() { return module.isEnabled() && module.mode.getValue() == this; }
}
