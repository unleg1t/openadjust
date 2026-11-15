package com.adjustclient.backend.protocol.packet;

import net.minecraft.network.PacketBuffer;

public class ByteWriter implements PacketWriter {
    public int value = 134;

    public ByteWriter() {
    }

    public ByteWriter(int value) {
        this.value = value;
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeVarIntToBuffer(134);
    }
}
