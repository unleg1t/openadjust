package com.adjustclient.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import com.adjustclient.util.anticheat.ServerTracker;

public final class DamageUtil implements MinecraftInstance {
    public static boolean canCritical(Entity target) {
        ServerTracker tracker = ServerTracker.INSTANCE;
        return tracker != null && mc.thePlayer != null && !tracker.field196
                && tracker.velocity > 0 && canCritical(mc.thePlayer, target);
    }
    public static DamageResult computeDamage(Entity target, EntityPlayer attacker, ItemStack weapon, boolean critical) {
        float base = (float) attacker.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
        if (weapon != null) {
            AttributeModifier last = null;
            for (AttributeModifier modifier : weapon.getAttributeModifiers().get("generic.attackDamage")) last = modifier;
            if (last != null) base += last.getAmount();
        }
        EntityLivingBase living = target instanceof EntityLivingBase ? (EntityLivingBase) target : null;
        if (living != null && living.getHealth() <= 0) return new DamageResult(new String[0], 0);
        float enchantment = EnchantmentHelper.getModifierForCreature(weapon,
                living == null ? EnumCreatureAttribute.UNDEFINED : living.getCreatureAttribute());
        if (base <= 0 && enchantment <= 0) return new DamageResult(new String[0], 0);
        List<String> modifiers = new ArrayList<>();
        if (canCritical(target) && base > 0) {
            base *= 1.5F;
            modifiers.add("critical");
        }
        float damage = base + enchantment;
        if (living != null) {
            damage = damage * (25 - living.getTotalArmorValue()) / 25.0F;
            if (living.isPotionActive(Potion.resistance)) {
                damage = damage * (25 - 5 * (living.getActivePotionEffect(Potion.resistance).getAmplifier() + 1)) / 25.0F;
            }
            if (damage <= 0) return new DamageResult(new String[0], 0);
            int protection = Math.min(20, EnchantmentHelper.getEnchantmentModifierDamage(
                    living.getInventory(), new EntityDamageSource("player", attacker)));
            if (protection > 0) damage = damage * (25 - protection) / 25.0F;
            if (target instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) target;
                ItemStack held = player.inventory.getCurrentItem();
                if (held != null && held.getItem() instanceof ItemSword && player.isUsingItem()) {
                    damage /= 2.0F;
                    modifiers.add("blocking");
                }
            }
        }
        return new DamageResult(modifiers.toArray(new String[0]), damage);
    }
    public static boolean canCritical(EntityPlayer player, Entity target) {
        Block feet = player.worldObj.getBlockState(new BlockPos(player.posX,
                player.getEntityBoundingBox().minY, player.posZ)).getBlock();
        if (feet == Blocks.ladder || feet == Blocks.vine) {
            if (mc.thePlayer == null || mc.thePlayer.sendQueue == null) return false;
            net.minecraft.client.network.NetworkPlayerInfo info = mc.thePlayer.sendQueue.getPlayerInfo(player.getUniqueID());
            if (info == null || info.getGameType() != net.minecraft.world.WorldSettings.GameType.SPECTATOR) return false;
        }
        return !player.isInWater() && !player.isPotionActive(Potion.blindness)
                && player.ridingEntity == null && (target == null || target instanceof EntityLivingBase);
    }
}
