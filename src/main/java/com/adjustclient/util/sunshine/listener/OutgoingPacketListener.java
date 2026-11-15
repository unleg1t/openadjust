package com.adjustclient.util.sunshine.listener;
import net.minecraft.network.Packet;
public interface OutgoingPacketListener extends CheckListener {
    void onOutgoingPacket(Packet<?> packet);
}
