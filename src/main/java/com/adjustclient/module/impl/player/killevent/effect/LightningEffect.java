package com.adjustclient.module.impl.player.killevent.effect;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
public final class LightningEffect extends KillEffect {
    public LightningEffect(EntityLivingBase entity) { super(entity); }
    @Override public void play() {
        EntityLightningBolt bolt = new EntityLightningBolt(mc.theWorld, entity.posX, entity.posY, entity.posZ);
        entity.serverPosX = (int)(entity.posX * 32.0D);
        entity.serverPosY = (int)(entity.posY * 32.0D);
        entity.serverPosZ = (int)(entity.posZ * 32.0D);
        entity.rotationYaw = 0.0F;
        entity.rotationPitch = 0.0F;
        entity.setEntityId(-666);
        mc.theWorld.weatherEffects.add(bolt);
        mc.theWorld.playSound(entity.posX, entity.posY, entity.posZ, "ambient.weather.thunder", 1.0F, 1.0F, false);
        finished = true;
    }
}
