package com.adjustclient.utilities.backend.protocol.packet;

import net.minecraft.network.PacketBuffer;

public class BytesWriter implements PacketWriter {
    public int field4;
    public int requestId;
    public boolean flag;
    public byte[] data;

    public BytesWriter() {
    }

    public BytesWriter(int requestId, boolean flag, byte[] data) {
        this.requestId = requestId;
        this.flag = flag;
        this.data = data;
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeVarIntToBuffer(requestId);
        buffer.writeByte(flag ? 1 : 0);
        buffer.writeVarIntToBuffer(data.length);
        buffer.writeBytes(data);
    }
}
