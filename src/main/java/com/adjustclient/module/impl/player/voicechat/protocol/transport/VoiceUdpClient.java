package com.adjustclient.module.impl.player.voicechat.protocol.transport;

import com.adjustclient.module.impl.player.voicechat.protocol.VoiceCryptoHandler;
import com.adjustclient.module.impl.player.voicechat.protocol.packet.ClientboundVoicePacket;
import com.adjustclient.module.impl.player.voicechat.protocol.packet.VoicePacket;
import com.adjustclient.module.impl.player.voicechat.protocol.packet.VoicePacketListener;
import com.adjustclient.module.impl.player.voicechat.protocol.packet.VoicePacketRegistry;
import com.adjustclient.module.impl.player.voicechat.protocol.packet.serverbound.ServerboundKeepAlivePacket;
import com.adjustclient.module.impl.player.voicechat.protocol.transport.reliable.ReliableUdpConnection;
import com.adjustclient.module.impl.player.voicechat.protocol.type.ConnectionState;
import com.adjustclient.module.impl.player.voicechat.protocol.type.FrameType;
import com.adjustclient.module.impl.player.voicechat.protocol.type.PacketEncryption;
import com.adjustclient.module.impl.player.voicechat.protocol.type.VoiceProtocolEnumB;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class VoiceUdpClient {
    public final DatagramSocket socket;
    public final AtomicBoolean open = new AtomicBoolean();
    public final List<VoiceReceiveThread> receiveThreads = new ArrayList<>();
    public final ReliableUdpConnection connection;
    public final ScheduledFuture<?> keepAliveTask;
    public final ScheduledFuture<?> resendTask;
    public final VoicePacketListener listener;
    public final AtomicLong lastReceived = new AtomicLong(System.currentTimeMillis());
    public VoiceProtocolEnumB phase = VoiceProtocolEnumB.HANDSHAKE;
    public Consumer<ConnectionState> stateListener;

    public VoiceUdpClient(VoicePacketListener listener, InetSocketAddress address, String publicKey, int timeout) throws Exception {
        socket = new DatagramSocket();
        open.set(true);
        this.listener = listener;
        socket.setSoTimeout(timeout);
        connection = new ReliableUdpConnection(address, this::createPacket);
        connection.symmetricCrypto = new VoiceCryptoHandler();
        connection.asymmetricCrypto = new VoiceCryptoHandler(publicKey);
        keepAliveTask = Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            try {
                if (System.currentTimeMillis() - lastReceived.get() > timeout) {
                    setState(ConnectionState.TIMEOUT);
                } else if (phase == VoiceProtocolEnumB.CONNECTED) {
                    sendPacket(new ServerboundKeepAlivePacket(), connection, null);
                }
            } catch (Exception ignored) {
                // The scheduled callback catches failures, allowing the next periodic invocation.
            }
        }, 1, 1, TimeUnit.SECONDS);
        resendTask = Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            if (connection != null) connection.resendUnacknowledgedFrames(socket);
        }, 500, 500, TimeUnit.MILLISECONDS);
    }

    public void start() {
        VoiceReceiveThread thread = new VoiceReceiveThread(socket,
                (source, data, offset, length) -> {
                    if (connection != null) {
                        connection.receive(source, data, offset, length, null);
                    }
                }, this::onError);
        thread.start();
        receiveThreads.add(thread);
    }

    public void setState(ConnectionState state) {
        for (VoiceReceiveThread thread : receiveThreads) {
            thread.running = false;
            thread.interrupt();
        }
        open.set(false);
        socket.close();
        keepAliveTask.cancel(true);
        resendTask.cancel(true);
        if (stateListener != null && state != ConnectionState.KICK) {
            stateListener.accept(state);
        }
    }

    public void onError(Object source, Throwable error) {
        if (!open.get()) return;
        if (!(error instanceof SocketTimeoutException)) {
            setState(ConnectionState.ERROR);
        } else if (open.get()) {
            setState(ConnectionState.TIMEOUT);
        }
    }

    @SuppressWarnings("unchecked")
    public void createPacket(byte[] data) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        byte id = (byte) in.read();
        Map<Byte, java.util.concurrent.Callable<ClientboundVoicePacket>> factories = VoicePacketRegistry.INCOMING_PACKETS;
        java.util.concurrent.Callable<ClientboundVoicePacket> factory = factories.get(id);
        if (factory == null) throw new IllegalStateException("Unknown packet id " + id);
        ClientboundVoicePacket packet = factory.call();
        if (packet == null) throw new IllegalStateException("Unknown packet id " + id);
        packet.read(in, 5);
        lastReceived.set(System.currentTimeMillis());
        packet.handle(listener);
    }

    public int sendPacket(com.adjustclient.module.impl.player.voicechat.protocol.packet.ServerboundVoicePacket packet, ReliableUdpConnection connection, Object context) throws Exception {
        if (!open.get()) throw new IllegalStateException("Can't send packet while socket is closed");
        if (connection.networkVersion == FrameType.UNIDENTIFIED) throw new IllegalStateException("Network version not set");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(VoicePacketRegistry.getId(packet));
        packet.write(out, 5);
        byte[] data = out.toByteArray();
        PacketEncryption encryption = (PacketEncryption) packet.encryption;
        if (connection.networkVersion == FrameType.V1) {
            byte[] encrypted = connection.encryptFrame(encryption, Arrays.copyOfRange(data, 1, data.length));
            byte[] result = new byte[encrypted.length + 1];
            result[0] = data[0];
            System.arraycopy(encrypted, 0, result, 1, encrypted.length);
            return connection.sendRaw(socket, result);
        }
        byte[] extension = packet.getSupportedVersions();
        if (packet.reliability != com.adjustclient.module.impl.player.voicechat.protocol.type.VoiceProtocolEnumH.UDP) {
            return connection.sendReliable(socket, extension, data, encryption, context);
        }
        return connection.sendUnreliable(socket, extension, data, encryption, context);
    }
}
