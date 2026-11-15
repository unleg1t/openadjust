package com.adjustclient.game.packet;

import net.minecraft.network.Packet;

@FunctionalInterface
public interface PacketQueueFilter {
    boolean isPacketQueueAllowed(Packet<?> packet);
}
