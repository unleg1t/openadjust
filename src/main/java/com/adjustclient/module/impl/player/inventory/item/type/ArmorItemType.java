package com.adjustclient.module.impl.player.inventory.item.type;
import com.adjustclient.module.impl.player.inventory.item.ItemType;
import com.adjustclient.module.impl.player.inventory.item.filter.BestItemFilter;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.util.DamageSource;
public final class ArmorItemType extends ItemType implements BestItemFilter.RankedType {
    public final int armorType;
    public ArmorItemType(int armorType) { this.armorType = armorType; keepFilter = new BestItemFilter(this); }
    @Override public boolean matches(ItemStack stack) {
        return stack.getItem() instanceof ItemArmor && ((ItemArmor) stack.getItem()).armorType == armorType;
    }
    @Override public int score(ItemType type, ItemStack stack) {
        int protection = EnchantmentHelper.getEnchantmentModifierDamage(new ItemStack[] { stack },
                DamageSource.causePlayerDamage(Minecraft.getMinecraft().thePlayer));
        int result = 100 * (((ItemArmor) stack.getItem()).damageReduceAmount + Math.min(20, protection));
        NBTTagList enchantments = stack.getEnchantmentTagList(); // ench
        if (enchantments != null) {
            for (int index = 0; index < enchantments.tagCount(); index++) {
                NBTTagCompound tag = enchantments.getCompoundTagAt(index);
                Enchantment enchantment = Enchantment.getEnchantmentById(tag.getShort("id"));
                int level = Math.max(1, tag.getShort("lvl")) + 1;
                if (enchantment == Enchantment.featherFalling || enchantment == Enchantment.blastProtection) result += 2 * level;
                else if (enchantment == Enchantment.projectileProtection || enchantment == Enchantment.depthStrider) result += level;
            }
        }
        for (ItemStack equipped : Minecraft.getMinecraft().thePlayer.inventory.armorInventory) {
            if (equipped == stack) {
                result++;
                break;
            }
        }
        return result;
    }
}
