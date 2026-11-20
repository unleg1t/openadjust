package com.adjustclient.utilities.sunshine.check.interact;
import com.adjustclient.utilities.sunshine.check.SunshineCheck;
import com.adjustclient.utilities.sunshine.listener.OutgoingPacketListener;
import com.adjustclient.utilities.sunshine.tracker.HistoryLookup;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
public class UseCheck extends SunshineCheck implements OutgoingPacketListener {
    public static final HistoryLookup RELEASE_HISTORY;
    static {
        RELEASE_HISTORY=new HistoryLookup(java.util.List.of(
                C0APacketAnimation.class::isInstance,C02PacketUseEntity.class::isInstance));
    }
    public UseCheck() { super("Use", "No description provided"); }
    @Override public void onOutgoingPacket(Packet<?> packet) {
        if (packet instanceof C02PacketUseEntity && ((C02PacketUseEntity)packet).getAction()==C02PacketUseEntity.Action.ATTACK)
            RELEASE_HISTORY.matches(anticheat.packetTracker.history.samples);
    }
}
