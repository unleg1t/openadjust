package com.adjustclient.backend.protocol.packet;

import net.minecraft.network.PacketBuffer;

public class EmptyReader implements PacketReader {
    public byte[] data;

    @Override
    public void read(PacketBuffer buffer) {
    }
}
