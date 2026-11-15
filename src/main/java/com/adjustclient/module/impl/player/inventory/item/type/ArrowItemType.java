package com.adjustclient.module.impl.player.inventory.item.type;
import com.adjustclient.module.impl.player.inventory.item.ItemType;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
public final class ArrowItemType extends ItemType {
    @Override public boolean matches(ItemStack stack) { return stack.getItem() == Items.arrow; }
}
