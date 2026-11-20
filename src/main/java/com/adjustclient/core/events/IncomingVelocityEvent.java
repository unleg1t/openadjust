package com.adjustclient.core.events;

import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;

public class IncomingVelocityEvent extends CancellableEvent {
    public boolean suppressApplication;
    public Packet<?> packet;

    public IncomingVelocityEvent(Packet<?> packet, boolean suppressApplication) {
        this.packet = packet;
        this.suppressApplication = suppressApplication;
    }

    public boolean hasVelocity() {
        if (packet instanceof S12PacketEntityVelocity) {
            S12PacketEntityVelocity velocity = (S12PacketEntityVelocity) packet;
            if (Minecraft.getMinecraft().thePlayer == null
                    || velocity.getEntityID() != Minecraft.getMinecraft().thePlayer.getEntityId()) return false;
            double x = velocity.getMotionX() / 8000.0;
            double y = velocity.getMotionY() / 8000.0;
            double z = velocity.getMotionZ() / 8000.0;
            if (x == -0.0) x = 0.0;
            if (y == -0.0) y = 0.0;
            if (z == -0.0) z = 0.0;
            return Math.sqrt(x * x + y * y + z * z) >= 0.005;
        }
        if (!(packet instanceof S27PacketExplosion)) return false;
        S27PacketExplosion explosion = (S27PacketExplosion) packet;
        if (explosion.suppressPlayerMotion) return false;
        float x = explosion.func_149149_c();
        float y = explosion.func_149144_d();
        float z = explosion.func_149147_e();
        if (x == -0.0) x = 0.0f;
        if (y == -0.0) y = 0.0f;
        if (z == -0.0) z = 0.0f;
        return Math.sqrt(x * x + y * y + z * z) >= 0.005;
    }
}
