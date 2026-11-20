package com.adjustclient.modules.impl.player.inventory.item.type;
import com.adjustclient.modules.impl.player.inventory.item.ItemType;
import com.adjustclient.modules.impl.player.inventory.item.filter.BestItemFilter;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
public final class BowItemType extends ItemType implements BestItemFilter.RankedType {
    public BowItemType() { keepFilter = new BestItemFilter(this); }
    @Override public boolean matches(ItemStack stack) { return stack.getItem() instanceof ItemBow; }
    @Override public int score(ItemType type, ItemStack stack) {
        int result = isLowDurability(stack, 25) ? 0 : 1;
        NBTTagList enchantments = stack.getEnchantmentTagList(); // ench; ItemStack handles Unbreakable.
        if (enchantments != null) {
            for (int index = 0; index < enchantments.tagCount(); index++) {
                NBTTagCompound tag = enchantments.getCompoundTagAt(index);
                Enchantment enchantment = Enchantment.getEnchantmentById(tag.getShort("id"));
                if (enchantment == Enchantment.flame) result += 10;
                else if (enchantment == Enchantment.punch || enchantment == Enchantment.power)
                    result += 10 * Math.max(1, tag.getShort("lvl"));
            }
        }
        return result;
    }
}
