package com.adjustclient.modules.impl.player.inventory.item.type;
import com.adjustclient.modules.impl.player.inventory.item.ItemType;
import com.adjustclient.modules.impl.player.inventory.item.filter.BestItemFilter;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
public final class FishingRodItemType extends ItemType implements BestItemFilter.RankedType {
    public FishingRodItemType() { keepFilter = new BestItemFilter(this); }
    @Override public boolean matches(ItemStack stack) { return stack.getItem() instanceof ItemFishingRod; }
    @Override public int score(ItemType type, ItemStack stack) {
        float result = isLowDurability(stack, 15) ? 10.0F : 11.0F;
        NBTTagList enchantments = stack.getEnchantmentTagList(); // ench
        if (enchantments != null) {
            for (int index = 0; index < enchantments.tagCount(); index++) {
                NBTTagCompound tag = enchantments.getCompoundTagAt(index);
                Enchantment enchantment = Enchantment.getEnchantmentById(tag.getShort("id"));
                int level = Math.max(1, tag.getShort("lvl"));
                if (enchantment == Enchantment.sharpness)
                    result *= enchantment.calcDamageByCreature(level, EnumCreatureAttribute.UNDEFINED);
                else if (enchantment == Enchantment.knockback) result += level * 0.15F;
            }
        }
        return (int) (result * 40000.0F);
    }
}
