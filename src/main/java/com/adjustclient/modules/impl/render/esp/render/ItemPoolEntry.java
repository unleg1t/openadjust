package com.adjustclient.modules.impl.render.esp.render;

import net.minecraft.item.ItemStack;

final class ItemPoolEntry {
    ItemStack stack;
    String name;
    int count;
    Object field16;

    ItemPoolEntry() { }
    ItemPoolEntry(ItemStack stack) { this.stack = stack; }
}
