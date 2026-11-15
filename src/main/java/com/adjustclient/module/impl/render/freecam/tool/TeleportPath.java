package com.adjustclient.module.impl.render.freecam.tool;

import com.adjustclient.util.MinecraftInstance;
import com.adjustclient.util.Vec2f;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.Vec3;

public final class TeleportPath implements MinecraftInstance {
    public Vec2f rotation;
    public Object field8;

    public C03PacketPlayer.C06PacketPlayerPosLook packet(Vec3 position) {
        if (rotation == null) rotation = new Vec2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
        return new C03PacketPlayer.C06PacketPlayerPosLook(position.xCoord, position.yCoord, position.zCoord,
                rotation.x, rotation.y, position.yCoord % 0.015625D == 0.0D);
    }
}
