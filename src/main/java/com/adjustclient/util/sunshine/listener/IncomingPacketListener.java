package com.adjustclient.util.sunshine.listener;
import net.minecraft.network.Packet;
public interface IncomingPacketListener extends CheckListener {
    void onIncomingPacket(Packet<?> packet);
}
