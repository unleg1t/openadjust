package com.adjustclient.utilities.protocol.v1_12_2;

public class ProtocolPacketBuffer extends net.minecraft.network.PacketBuffer {
    public ProtocolPacketBuffer() {
        super(io.netty.buffer.Unpooled.buffer());
    }

    public ProtocolPacketBuffer(io.netty.buffer.ByteBuf wrapped) {
        super(wrapped);
    }

}
