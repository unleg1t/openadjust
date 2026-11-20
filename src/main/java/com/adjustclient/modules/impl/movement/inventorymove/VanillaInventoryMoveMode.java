package com.adjustclient.modules.impl.movement.inventorymove;

public enum VanillaInventoryMoveMode implements InventoryMoveMode {
    VANILLA("Vanilla");

    private final String displayName;

    VanillaInventoryMoveMode(String displayName) { this.displayName = displayName; }

    @Override public String getName() { return displayName; }

}
