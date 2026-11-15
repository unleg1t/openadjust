package com.adjustclient.util.sunshine.tracker;
import com.adjustclient.util.sunshine.SunshineAnticheat;
import com.adjustclient.util.sunshine.listener.OutgoingPacketListener;
import net.minecraft.network.Packet;
public class NoopPacketTracker implements OutgoingPacketListener {
    public final SunshineAnticheat anticheat;
    public Object field8;
    public NoopPacketTracker(SunshineAnticheat anticheat) { this.anticheat = anticheat; }
    @Override public void onOutgoingPacket(Packet<?> packet) { }
}
