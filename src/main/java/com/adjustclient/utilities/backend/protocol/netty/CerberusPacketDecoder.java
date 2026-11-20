package com.adjustclient.utilities.backend.protocol.netty;

import com.adjustclient.utilities.backend.protocol.CerberusProtocolState;
import com.adjustclient.utilities.backend.protocol.packet.EmptyReader;
import com.adjustclient.utilities.backend.protocol.packet.PacketCodec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.AttributeKey;
import java.io.IOException;
import java.util.List;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.PacketBuffer;

public class CerberusPacketDecoder extends ByteToMessageDecoder {
    public static final AttributeKey<CerberusProtocolState> PROTOCOL_KEY = AttributeKey.valueOf("protocol");

    public final EnumPacketDirection direction;

    public CerberusPacketDecoder(EnumPacketDirection direction) {
        this.direction = direction;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() <= 0) {
            return;
        }
        int id = readVarInt(in);
        CerberusProtocolState state = ctx.channel().attr(PROTOCOL_KEY).get();
        PacketCodec codec = state.createPacket(id);
        if (codec == null) {
            throw new IOException("Bad packet id " + id);
        }
        PacketBuffer buffer = new PacketBuffer(in);
        if (!(codec instanceof EmptyReader)) {
            codec.read(buffer);
        }
        if (buffer.readableBytes() > 0) {
            throw new IOException("Packet " + state + "/" + id + " (" + codec.getClass().getSimpleName()
                    + ") was larger than I expected, found " + buffer.readableBytes()
                    + " bytes extra whilst reading packet " + id);
        }
        out.add(codec);
    }

    private static int readVarInt(ByteBuf buf) {
        int result = 0;
        int bytes = 0;
        byte b;
        do {
            b = buf.readByte();
            result |= (b & 0x7F) << (bytes++ * 7);
            if (bytes > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while ((b & 0x80) == 0x80);
        return result;
    }
}
