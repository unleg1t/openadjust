package com.adjustclient.modules.impl.player.inventory.item;

import net.minecraft.item.ItemStack;
import java.util.function.ToIntFunction;

@FunctionalInterface
public interface ItemScoreFunction extends ToIntFunction<ItemStack> {
}
