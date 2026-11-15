package com.adjustclient.backend.protocol.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.IdleStateHandler;
import net.minecraft.network.EnumPacketDirection;

public class CerberusChannelInitializer extends ChannelInitializer<Channel> {
    public final CerberusChannelHandler handler;

    public CerberusChannelInitializer(CerberusChannelHandler handler) {
        this.handler = handler;
    }

    @Override
    protected void initChannel(Channel ch) {
        ch.config().setOption(ChannelOption.TCP_NODELAY, Boolean.TRUE);
        ch.pipeline()
                .addLast("idler", new IdleStateHandler(60, 30, 0))
                .addLast("splitter", new CerberusFrameSplitter())
                .addLast("decoder", new CerberusPacketDecoder(EnumPacketDirection.SERVERBOUND))
                .addLast("prepender", new CerberusFramePrepender())
                .addLast("encoder", new CerberusPacketEncoder(EnumPacketDirection.CLIENTBOUND))
                .addLast("packet_handler", handler);
    }
}
