package com.adjustclient.utilities.sunshine.tracker;
import com.adjustclient.utilities.sunshine.*;
import com.adjustclient.utilities.sunshine.listener.OutgoingPacketListener;
import net.minecraft.network.Packet;
public final class TimedPacketTracker implements OutgoingPacketListener {
    public final SunshineAnticheat anticheat;
    public final TickCounter history = new TickCounter();
    public TimedPacketTracker(SunshineAnticheat anticheat) { this.anticheat = anticheat; }
    @Override public void onOutgoingPacket(Packet<?> packet) {
        while (history.samples.size() >= 10) history.samples.removeFirst();
        history.samples.addLast(new TimedSample(packet));
    }
}
