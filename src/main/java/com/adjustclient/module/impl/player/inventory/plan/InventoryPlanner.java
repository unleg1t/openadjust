package com.adjustclient.module.impl.player.inventory.plan;

import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.module.impl.player.inventory.action.InventoryAction;
import net.minecraft.item.ItemStack;

public abstract class InventoryPlanner implements MinecraftInstance {
    public final InventorySnapshot snapshot;

    protected InventoryPlanner(InventorySnapshot snapshot) { this.snapshot = snapshot; }
    public abstract ItemStack getStack(int slot);
    public abstract InventoryAction chooseAction(InventorySlot slot);
}
