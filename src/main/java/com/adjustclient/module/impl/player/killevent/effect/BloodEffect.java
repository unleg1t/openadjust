package com.adjustclient.module.impl.player.killevent.effect;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
public final class BloodEffect extends KillEffect {
    public BloodEffect(EntityLivingBase entity) { super(entity); }
    @Override public void play() {
        for (int i = 0; i < 50; i++) {
            mc.theWorld.spawnParticle(EnumParticleTypes.BLOCK_CRACK, entity.posX,
                    entity.posY + entity.getEyeHeight() - 0.2D, entity.posZ,
                    0.0D, 0.0D, 0.0D, Block.getStateId(Blocks.redstone_block.getDefaultState()));
        }
        mc.theWorld.playSound(entity.posX, entity.posY, entity.posZ, "dig.stone",
                Blocks.redstone_block.stepSound.getVolume() + 1.0F, 1.0F, false);
        finished = true;
    }
}
