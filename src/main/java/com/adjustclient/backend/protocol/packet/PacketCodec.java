package com.adjustclient.backend.protocol.packet;

import net.minecraft.network.PacketBuffer;

public interface PacketCodec {
    void read(PacketBuffer buffer);

    void write(PacketBuffer buffer);
}
