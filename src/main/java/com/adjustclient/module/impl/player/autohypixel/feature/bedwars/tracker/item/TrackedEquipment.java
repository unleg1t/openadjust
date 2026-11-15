package com.adjustclient.module.impl.player.autohypixel.feature.bedwars.tracker.item;

import net.minecraft.item.ItemStack;

public final class TrackedEquipment<T> {
    public final T tier;
    public final ItemStack stack;
    public TrackedEquipment(T tier, ItemStack stack) { this.tier = tier; this.stack = stack; }
}
