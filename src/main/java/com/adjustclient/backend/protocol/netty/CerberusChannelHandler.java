package com.adjustclient.backend.protocol.netty;

import com.adjustclient.backend.protocol.CerberusClient;
import com.adjustclient.backend.protocol.CerberusProtocolState;
import com.adjustclient.backend.protocol.packet.ByteWriter;
import com.adjustclient.backend.protocol.packet.BytesCodec;
import com.adjustclient.backend.protocol.packet.BytesReader;
import com.adjustclient.backend.protocol.packet.EmptyReader;
import com.adjustclient.backend.protocol.packet.EmptyWriter;
import com.adjustclient.backend.protocol.packet.PacketCodec;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CerberusChannelHandler extends ChannelInboundHandlerAdapter {
    public volatile boolean connected;
    public volatile Channel channel;
    public final CerberusClient client;
    public final Queue<PacketCodec> pendingQueue = new ConcurrentLinkedQueue<>();

    public CerberusChannelHandler(CerberusClient client) {
        this.client = client;
    }

    public static CerberusChannelHandler connect(int port, CerberusClient client) {
        CerberusChannelHandler handler = new CerberusChannelHandler(client);
        NioEventLoopGroup group = new NioEventLoopGroup(0);
        for (int attempt = 0; attempt < 10; attempt++) {
            try {
                Bootstrap bootstrap = new Bootstrap()
                        .group(group)
                        .channel(NioSocketChannel.class)
                        .handler(new CerberusChannelInitializer(handler));
                if (bootstrap.connect(new InetSocketAddress("localhost", port)).awaitUninterruptibly().isSuccess()) {
                    return handler;
                }
            } catch (Throwable ignored) {
            }
        }
        System.exit(0);
        return handler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.channel = ctx.channel();
        this.channel.attr(CerberusPacketDecoder.PROTOCOL_KEY).set(CerberusProtocolState.HANDSHAKE);
        this.connected = true;
        send(new ByteWriter());
        PacketCodec queued;
        while ((queued = pendingQueue.poll()) != null) {
            final PacketCodec toSend = queued;
            Runnable task = () -> channel.writeAndFlush(toSend);
            if (channel.eventLoop().inEventLoop()) {
                task.run();
            } else {
                channel.eventLoop().execute(task);
            }
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        handlePacket(ctx, (PacketCodec) msg);
    }

    public void handlePacket(ChannelHandlerContext ctx, PacketCodec msg) throws IOException {
        CerberusProtocolState state = ctx.channel().attr(CerberusPacketDecoder.PROTOCOL_KEY).get();
        switch (CerberusPackets.PROTOCOL_STATES[state.ordinal()]) {
            case 1:
                handleHandshake(ctx, msg);
                break;
            case 2:
                if (msg instanceof BytesCodec) {
                    BytesCodec codec = (BytesCodec) msg;
                    client.handleResponse(codec.requestId, codec.data);
                } else if (!(msg instanceof EmptyReader)) {
                    throw new IOException("Message read failure: " + msg.getClass().getSimpleName());
                }
                break;
            default:
                break;
        }
    }

    public void handleHandshake(ChannelHandlerContext ctx, PacketCodec msg) {
        if (!(msg instanceof BytesReader)) {
            return;
        }
        ctx.channel().attr(CerberusPacketDecoder.PROTOCOL_KEY).set(CerberusProtocolState.SESSION);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent && ((IdleStateEvent) evt).state() == IdleState.READER_IDLE) {
            send(new EmptyWriter());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Logger.log(LogLevel.ERROR, true, "Error inside Cerberus protocol: {}", cause);
    }

    public void send(PacketCodec packet) {
        Channel ch = this.channel;
        if (ch == null || !ch.isActive()) {
            pendingQueue.offer(packet);
            return;
        }
        if (ch.eventLoop().inEventLoop()) {
            ch.writeAndFlush(packet);
        } else {
            ch.eventLoop().execute(() -> ch.writeAndFlush(packet));
        }
    }

    void scheduleReconnect() {
        CerberusClient.reconnect(client);
    }
}
