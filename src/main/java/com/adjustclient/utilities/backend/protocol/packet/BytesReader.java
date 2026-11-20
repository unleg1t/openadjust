package com.adjustclient.utilities.backend.protocol.packet;

import net.minecraft.network.PacketBuffer;

public class BytesReader implements PacketReader {
    public byte[] data;
    public boolean flag;

    @Override
    public void read(PacketBuffer buffer) {
        int len = buffer.readVarIntFromBuffer();
        byte[] bytes = new byte[len];
        buffer.readBytes(bytes);
        this.data = bytes;
    }
}
