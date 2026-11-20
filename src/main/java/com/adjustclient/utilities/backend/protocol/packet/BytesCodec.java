package com.adjustclient.utilities.backend.protocol.packet;

import net.minecraft.network.PacketBuffer;

public class BytesCodec implements PacketCodec {
    public byte[] data;
    public int requestId;

    @Override
    public void read(PacketBuffer buffer) {
        this.requestId = buffer.readVarIntFromBuffer();
        int len = buffer.readVarIntFromBuffer();
        byte[] bytes = new byte[len];
        buffer.readBytes(bytes);
        this.data = bytes;
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeVarIntToBuffer(requestId);
        buffer.writeVarIntToBuffer(data.length);
        buffer.writeBytes(data);
    }
}
