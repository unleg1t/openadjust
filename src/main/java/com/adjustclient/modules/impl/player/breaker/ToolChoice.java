package com.adjustclient.modules.impl.player.breaker;

import net.minecraft.item.ItemStack;

public class ToolChoice {
    public final ItemStack stack;
    public final int slot;
    public ToolChoice(ItemStack stack, int slot) { this.stack = stack; this.slot = slot; }
}
