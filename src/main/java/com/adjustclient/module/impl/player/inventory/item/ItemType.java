package com.adjustclient.module.impl.player.inventory.item;
import net.minecraft.item.ItemStack;
public abstract class ItemType {
    public ItemFilter keepFilter;
    public ItemScoreFunction sortKeyFunction;
    protected ItemType() {
        keepFilter = (type, stack, snapshot) -> true;
    }
    public abstract boolean matches(ItemStack stack);
    public boolean isLowDurability(ItemStack stack, int threshold) {
        return stack.isItemStackDamageable() && threshold > stack.getMaxDamage() - stack.getItemDamage();
        // ItemStack.isItemStackDamageable includes the NBT "Unbreakable" check.
    }
}
