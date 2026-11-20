package com.adjustclient.utilities.backend.protocol.packet;

import net.minecraft.network.PacketBuffer;

public interface PacketReader extends PacketCodec {
    @Override
    default void write(PacketBuffer buffer) {
        throw new IllegalStateException();
    }
}
