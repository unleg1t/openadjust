package com.adjustclient.modules.impl.player.inventory.plan;

import com.adjustclient.utilities.MinecraftInstance;
import com.adjustclient.modules.impl.player.inventory.action.InventoryAction;
import net.minecraft.item.ItemStack;

public abstract class InventoryPlanner implements MinecraftInstance {
    public final InventorySnapshot snapshot;

    protected InventoryPlanner(InventorySnapshot snapshot) { this.snapshot = snapshot; }
    public abstract ItemStack getStack(int slot);
    public abstract InventoryAction chooseAction(InventorySlot slot);
}
