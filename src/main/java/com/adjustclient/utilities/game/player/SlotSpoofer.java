package com.adjustclient.utilities.game.player;

import com.adjustclient.Adjust;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class SlotSpoofer {
    public int slot;
    public int field8;
    public int field12;
    public boolean active;
    public boolean field17;
    public boolean renderSpoofed;
    public Object field20;

    public ItemStack getRenderItemStack() {
        var inventory = Minecraft.getMinecraft().thePlayer.inventory;
        if (!active || renderSpoofed) return inventory.getCurrentItem();
        int current = inventory.currentItem;
        return current >= 0 && current < 9 ? inventory.mainInventory[current] : null;
    }

    public int getRenderSlot() {
        var mc = Minecraft.getMinecraft();
        var inventory = mc.thePlayer.inventory;
        if (active && !renderSpoofed) return inventory.currentItem;
        if (inventory.player != mc.thePlayer) return inventory.currentItem;
        if (InventoryStateTracker.isInServersideGui()) return mc.playerController.currentPlayerItem;
        return Adjust.slotSpoofer.active ? Adjust.slotSpoofer.slot : inventory.currentItem;
    }

    public int resolveSlot(int realSlot) {
        if (InventoryStateTracker.isInServersideGui()) return Minecraft.getMinecraft().playerController.currentPlayerItem;
        return active ? slot : realSlot;
    }
}
