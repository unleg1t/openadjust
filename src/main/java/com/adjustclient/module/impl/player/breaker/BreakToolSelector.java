package com.adjustclient.module.impl.player.breaker;

import com.adjustclient.util.BlockBreakUtil;
import com.adjustclient.util.MinecraftInstance;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public final class BreakToolSelector implements MinecraftInstance {
    public final BreakerModule module;
    public BreakToolSelector(BreakerModule module) { this.module = module; }
    public ToolChoice select(Block block) {
        ItemStack held = mc.thePlayer.inventory.getCurrentItem();
        int slot = mc.thePlayer.inventory.m();
        float speed = held == null ? 1.0F : BlockBreakUtil.getDigSpeed(mc.thePlayer, block, held, true, false);
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack == null) continue;
            float candidate = BlockBreakUtil.getDigSpeed(mc.thePlayer, block, stack, true, false);
            if (candidate > speed) { speed = candidate; slot = i; }
        }
        ItemStack stack = mc.thePlayer.inventory.mainInventory[slot];
        if (stack != null) BlockBreakUtil.getDigSpeed(mc.thePlayer, block, stack, true, false);
        return new ToolChoice(stack, slot);
    }
}
