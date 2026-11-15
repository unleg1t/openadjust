package com.adjustclient.protocol.netty;

public class ProtocolPacketEncoder extends io.netty.handler.codec.MessageToByteEncoder<net.minecraft.network.Packet<?>> {
    @Override
    protected void encode(io.netty.channel.ChannelHandlerContext ctx, net.minecraft.network.Packet<?> msg, io.netty.buffer.ByteBuf out) {
    }
}
