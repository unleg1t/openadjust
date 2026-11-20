package com.adjustclient.modules.impl.player.inventory.item.type;
import com.adjustclient.modules.impl.player.inventory.item.ItemType;
import com.adjustclient.modules.impl.player.inventory.item.filter.SimpleItemFilter;
import net.minecraft.item.*;
import net.minecraft.init.Items;
public final class FoodItemType extends ItemType {
    public FoodItemType(Item... exemptItems) {
        keepFilter = new SimpleItemFilter(64, exemptItems);
        sortKeyFunction = stack -> sortKey(stack);
    }
    @Override public boolean matches(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof ItemFood && item != Items.rotten_flesh && item != Items.spider_eye
                && ((ItemFood) item).getHealAmount(stack) >= 4;
    }
    public int sortKey(ItemStack stack) {
        if (stack == null) return 0;
        if (stack.getItem() == Items.golden_apple) return stack.getItemDamage() == 0 ? -9999999 : Integer.MIN_VALUE;
        return stack.getItem() instanceof ItemFood ? -((ItemFood) stack.getItem()).getHealAmount(stack) : 0;
    }
}
