package com.adjustclient.utilities.sunshine.check.interact;
import com.adjustclient.utilities.sunshine.*;
import com.adjustclient.utilities.sunshine.check.SunshineCheck;
import com.adjustclient.utilities.sunshine.listener.OutgoingPacketListener;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import com.adjustclient.utilities.sunshine.CheckCategory;
public final class RotationCheck extends SunshineCheck implements OutgoingPacketListener {
    public RotationCheck() { super("Rotation", "No description provided"); }
    @Override public void onOutgoingPacket(Packet<?> packet) {
        if (packet instanceof C03PacketPlayer) {
            C03PacketPlayer p=(C03PacketPlayer)packet;
            if (p.getRotating() && !p.forkField39 && Math.abs(p.getPitch())>90.0F)
                fail(new ViolationDetails(CheckCategory.KICK,"Impossible pitch").put("pitch",p.getPitch()));
        }
    }
}
