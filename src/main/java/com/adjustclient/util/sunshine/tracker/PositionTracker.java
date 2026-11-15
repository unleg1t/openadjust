package com.adjustclient.util.sunshine.tracker;
import com.adjustclient.util.sunshine.SunshineAnticheat;
import com.adjustclient.util.sunshine.listener.OutgoingPacketListener;
import com.adjustclient.util.sunshine.check.raytrace.RaytraceCheck;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
public final class PositionTracker implements OutgoingPacketListener {
    public final SunshineAnticheat anticheat;
    public PositionKey rotation=new PositionKey(0.0F,0.0F);
    public TrackedPosition position;
    public int ticks;
    public boolean flying;
    public Object field24;
    public PositionTracker(SunshineAnticheat anticheat) { this.anticheat=anticheat; }
    @Override public void onOutgoingPacket(Packet<?> packet) {
        if (!(packet instanceof C03PacketPlayer)) {
            if (packet instanceof C13PacketPlayerAbilities) {
                C13PacketPlayerAbilities p=(C13PacketPlayerAbilities)packet;
                flying=p.isAllowFlying() && p.isFlying();
            }
            return;
        }
        C03PacketPlayer p=(C03PacketPlayer)packet;
        anticheat.listeners.raytrace.listeners.forEach(listener -> ((RaytraceCheck)listener).previous=null);
        if (p.getRotating()) {
            PositionKey old=rotation;
            rotation=new PositionKey(p.getYaw(),p.getPitch());
            if (old!=null && !old.equals(rotation)) {
                anticheat.listeners.rotation.listeners.forEach(listener -> listener.onRotation(old,rotation));
            }
        }
        if (!p.isMoving()) { ticks++; return; }
        TrackedPosition old=position;
        TrackedPosition next=new TrackedPosition(rotation,p.getPositionX(),p.getPositionY(),p.getPositionZ(),p.isOnGround(),p.forkField39);
        if (old!=null && old.equals(next)) return;
        position=next;
        anticheat.listeners.movement.listeners.forEach(listener -> listener.onMovement(old,next));
        if (old!=null && old.ground && !next.ground && !next.teleport) {
            double motion=next.yCoord-old.yCoord;
            if (motion>0.415 && motion<0.426) {
                anticheat.listeners.jump.listeners.forEach(listener -> listener.onJump(old,next,motion));
            }
        }
        ticks++;
    }
}
