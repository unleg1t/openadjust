package com.adjustclient.modules.impl.movement.antivoid;

import com.adjustclient.utilities.Vec2f;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import static com.adjustclient.utilities.MinecraftInstance.mc;

final class LegitSafeSpot {
    final Vec2f rotation;
    EntityOtherPlayerMP entity;
    final boolean sprint;

    LegitSafeSpot(Vec2f rotation, boolean sprint) { this.rotation = rotation; this.sprint = sprint; }

    void capture(EntityPlayer source) {
        entity = new EntityOtherPlayerMP(source.worldObj, mc.thePlayer.getGameProfile());
        entity.setPositionAndRotation(source.posX, source.posY, source.posZ, source.rotationYaw, source.rotationPitch);
        entity.prevRotationYaw = source.prevRotationYaw;
        entity.prevRotationPitch = source.prevRotationPitch;
        entity.prevRenderYawOffset = source.prevRenderYawOffset;
        entity.renderYawOffset = source.renderYawOffset;
    }
}
