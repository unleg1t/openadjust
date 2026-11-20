package com.adjustclient.utilities.sunshine.listener;
import net.minecraft.network.Packet;
public interface OutgoingPacketListener extends CheckListener {
    void onOutgoingPacket(Packet<?> packet);
}
