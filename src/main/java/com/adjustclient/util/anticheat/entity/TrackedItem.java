package com.adjustclient.util.anticheat.entity;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatColor;

public final class TrackedItem extends TrackedEntity<EntityItem> {
    public Object field40;
    public boolean collected;
    public Object field48;
    public TrackedItem(EntityItem entity) { super(entity); }
    public boolean matchesType(int mask) { return (mask & 0x10) != 0; }
    public boolean isRemoved() { return removed || collected; }
    public String getDisplayName() {
        ItemStack stack = entity.getEntityItem();
        String name = stack.getDisplayName();
        if (name == null) return "Invalid Item Name";
        return stack.hasDisplayName() ? ChatColor.ITALIC + name : name;
    }
    public ItemStack getItemStack() { return entity.getEntityItem(); }
}
