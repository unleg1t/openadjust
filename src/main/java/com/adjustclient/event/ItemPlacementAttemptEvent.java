package com.adjustclient.event;

public final class ItemPlacementAttemptEvent extends Event {
    public net.minecraft.item.ItemStack stack;

    public ItemPlacementAttemptEvent(net.minecraft.item.ItemStack stack) {
        this.stack = stack;
    }
}
