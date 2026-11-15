package com.adjustclient.backend.protocol.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class CerberusFramePrepender extends MessageToByteEncoder<ByteBuf> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) {
        int length = msg.readableBytes();
        out.ensureWritable(4 + length);
        out.writeInt(length);
        out.writeBytes(msg, msg.readerIndex(), length);
    }
}
