package com.adjustclient.utilities.backend.protocol.netty;

import com.adjustclient.utilities.backend.protocol.CerberusProtocolState;
import com.adjustclient.utilities.backend.protocol.packet.EmptyWriter;
import com.adjustclient.utilities.backend.protocol.packet.PacketCodec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.io.IOException;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.PacketBuffer;

public class CerberusPacketEncoder extends MessageToByteEncoder<PacketCodec> {
    public final EnumPacketDirection direction;

    public CerberusPacketEncoder(EnumPacketDirection direction) {
        this.direction = direction;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, PacketCodec msg, ByteBuf out) throws Exception {
        CerberusProtocolState state = ctx.channel().attr(CerberusPacketDecoder.PROTOCOL_KEY).get();
        Integer id = state.getPacketId(msg.getClass());
        if (id == null) {
            throw new IOException("Can't serialize unregistered packet");
        }
        PacketBuffer buffer = new PacketBuffer(out);
        buffer.writeVarIntToBuffer(id);
        if (!(msg instanceof EmptyWriter)) {
            msg.write(buffer);
        }
    }
}
