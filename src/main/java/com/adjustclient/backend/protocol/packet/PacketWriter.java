package com.adjustclient.backend.protocol.packet;

import net.minecraft.network.PacketBuffer;

public interface PacketWriter extends PacketCodec {
    @Override
    default void read(PacketBuffer buffer) {
        throw new IllegalStateException();
    }
}
