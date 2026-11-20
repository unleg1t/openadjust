package com.adjustclient.utilities.game.packet;

import net.minecraft.network.Packet;

@FunctionalInterface
public interface PacketTransform {
    Packet<?> transform(Packet<?> packet);

    class Identity implements PacketTransform {
        public Packet<?> transform(Packet<?> packet) { return packet; }
    }
}
