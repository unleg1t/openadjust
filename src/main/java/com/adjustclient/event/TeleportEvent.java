package com.adjustclient.event;

public final class TeleportEvent extends CancellableEvent {
    public boolean pre;
    public java.util.List<net.minecraft.network.Packet<?>> acknowledgements;
    public java.util.Set<net.minecraft.network.play.server.S08PacketPlayerPosLook.EnumFlags> relativeFlags;
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;

    public TeleportEvent(double x, double y, double z, float yaw, float pitch,
            java.util.Set<net.minecraft.network.play.server.S08PacketPlayerPosLook.EnumFlags> relativeFlags, boolean pre) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.relativeFlags = relativeFlags;
        this.pre = pre;
        this.acknowledgements = new java.util.ArrayList<>();
        this.acknowledgements.add(new net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook(
                x, y, z, yaw % 360.0F, pitch % 360.0F, false));
    }
}
