package com.adjustclient.modules.impl.player.inventory.item.type;
import com.adjustclient.modules.impl.player.inventory.item.ItemType;
import com.adjustclient.modules.impl.player.inventory.item.filter.BestItemFilter.RankedType;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
public final class BucketItemType extends ItemType implements RankedType {
    @Override public boolean matches(ItemStack stack) {
        return stack.getItem() == Items.bucket || stack.getItem() == Items.water_bucket;
    }
    @Override public int score(ItemType type, ItemStack stack) { return stack.getItem() == Items.water_bucket ? 1 : 0; }
}
