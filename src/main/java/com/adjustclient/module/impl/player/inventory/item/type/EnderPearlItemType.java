package com.adjustclient.module.impl.player.inventory.item.type;
import com.adjustclient.module.impl.player.inventory.item.ItemType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemEnderPearl;
public final class EnderPearlItemType extends ItemType {
    @Override public boolean matches(ItemStack stack) { return stack.getItem() instanceof ItemEnderPearl; }
}
