package com.adjustclient.utilities;

import com.adjustclient.modules.impl.player.fastuse.feature.BlockBreakingFeature;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;

public final class BlockBreakUtil {
    public static float getRelativeHardness(Block block, BlockPos pos, ItemStack tool) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        float hardness = block.getBlockHardness(Minecraft.getMinecraft().theWorld, pos);
        if (hardness < 0.0F) return 0.0F;
        boolean harvest = block.getMaterial().isToolNotRequired() || tool != null && tool.canHarvestBlock(block);
        return getDigSpeed(player, block, tool, player.onGround, false) / hardness / (harvest ? 30.0F : 100.0F);
    }

    public static float getDigSpeed(EntityPlayer player, Block block, ItemStack tool, boolean onGround, boolean unused) {
        float speed = tool == null ? 1.0F : tool.getStrVsBlock(block);
        if (speed > 1.0F) {
            int efficiency = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, tool);
            if (efficiency > 0) speed += efficiency * efficiency + 1;
        }
        if (player.isPotionActive(Potion.digSpeed)) {
            speed *= 1.0F + (player.getActivePotionEffect(Potion.digSpeed).getAmplifier() + 1) * 0.2F;
        }
        if (player.isPotionActive(Potion.digSlowdown)) {
            float multiplier;
            switch (player.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) {
                case 0: multiplier = 0.3F; break;
                case 1: multiplier = 0.09F; break;
                case 2: multiplier = 0.0027F; break;
                default: multiplier = Float.intBitsToFloat(978605614); break;
            }
            float slowed = speed * multiplier;
            if (!(player instanceof EntityPlayerSP)
                    || BlockBreakingFeature.INSTANCE == null
                    || !BlockBreakingFeature.INSTANCE.isActive()
                    || !BlockBreakingFeature.INSTANCE.ignoreMiningFatigue.isEnabled()) speed = slowed;
        }
        double eyeY = player.posY + player.getEyeHeight();
        BlockPos eyePos = new BlockPos(player.posX, eyeY, player.posZ);
        net.minecraft.block.state.IBlockState state = player.worldObj.getBlockState(eyePos);
        if (state.getBlock().getMaterial() == Material.water) {
            int level = state.getBlock().getMetaFromState(state);
            if (level >= 8) level = 0;
            float surfaceOffset = (level + 1) / 9.0F - 0.11111111F;
            if (eyeY < eyePos.getY() + 1 - surfaceOffset && !EnchantmentHelper.getAquaAffinityModifier(player)) speed /= 5.0F;
        }
        if (!onGround) speed /= 5.0F;
        return speed * BlockBreakingFeature.INSTANCE.getMultiplier();
    }

    public static float getRelativeHardness(EntityPlayer player, Block block, BlockPos pos, ItemStack tool, boolean onGround) {
        float hardness = block.getBlockHardness(Minecraft.getMinecraft().theWorld, pos);
        if (hardness < 0.0F) return 0.0F;
        boolean harvest = block.getMaterial().isToolNotRequired() || tool != null && tool.canHarvestBlock(block);
        return getDigSpeed(player, block, tool, onGround, false) / hardness / (harvest ? 30.0F : 100.0F);
    }
}
