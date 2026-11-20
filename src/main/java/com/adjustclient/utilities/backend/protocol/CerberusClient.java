package com.adjustclient.utilities.backend.protocol;

import com.adjustclient.utilities.backend.protocol.netty.CerberusChannelHandler;
import com.adjustclient.utilities.backend.protocol.packet.BytesWriter;
import com.adjustclient.utilities.backend.protocol.packet.CerberusPacket;
import com.adjustclient.utilities.backend.protocol.packet.CerberusPacketData;
import com.adjustclient.utilities.backend.protocol.packet.PingPacket;
import com.adjustclient.utilities.backend.protocol.packet.StringPacket;
import com.adjustclient.core.logging.LogLevel;
import com.adjustclient.core.logging.Logger;
import com.adjustclient.utilities.RandomUtil;
import com.adjustclient.utilities.Timer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import net.minecraft.network.PacketBuffer;

public class CerberusClient {
    public static final Timer GLOBAL_PING_TIMER = new Timer();
    public static CerberusClient INSTANCE;

    public CerberusChannelHandler handler;
    public final Int2ObjectMap<CerberusPacket<?>> pendingRequests = new Int2ObjectOpenHashMap<>();
    public final Object writeLock = new Object();
    public final Object connectLock = new Object();
    public final Timer pingCacheTimer = new Timer();
    public PingPacket cachedPingPacket;
    public final Timer reconnectTimer = new Timer();
    public final int port;
    public int nextRequestId;

    public CerberusClient(int port) {
        this.port = port;
        this.handler = CerberusChannelHandler.connect(port, this);
    }

    public void handleResponse(int requestId, byte[] payload) {
        CerberusPacket<?> packet;
        synchronized (pendingRequests) {
            packet = pendingRequests.remove(requestId);
        }
        if (packet == null) {
            Logger.log(LogLevel.ERROR, true, "Request ID not found: {}", requestId);
            System.exit(1);
            return;
        }
        ByteBuf raw = Unpooled.wrappedBuffer(payload);
        try {
            packet.read(new CerberusPacketData(new PacketBuffer(raw)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            raw.release();
        }
        packet.latch.countDown();
    }

    public static void reconnect(CerberusClient client) {
        if (client == null) {
            return;
        }
        synchronized (client.connectLock) {
            if (INSTANCE != client || !client.reconnectTimer.hasElapsed(1500L)) {
                return;
            }
            client.reconnectTimer.reset();
            try {
                INSTANCE = new CerberusClient(client.port);
            } catch (Throwable t) {
                Logger.log(LogLevel.ERROR, true, "Failed to recreate connection with Cerberus: {}", t);
            }
        }
    }

    public static void waitForHandler(CerberusChannelHandler target) {
        for (int i = 0; i < 2049; i++) {
            if (target.connected) {
                return;
            }
            try {
                Thread.sleep(1L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        Logger.log(LogLevel.ERROR, true, "Failed to begin connection with Cerberus");
        System.exit(1);
    }

    public Byte sendPingCheck(CerberusPacket<?> packet) {
        PingPacket ping = getOrCreatePing();
        send(ping);
        return ping.getResult();
    }

    public static byte[] drainBuffer(ByteBuf buffer) {
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        buffer.release();
        return bytes;
    }

    public <T> T send(CerberusPacket<T> packet) {
        if (INSTANCE == null) {
            return null;
        }
        waitForHandler(handler);
        int id;
        synchronized (writeLock) {
            id = nextRequestId++;
            synchronized (pendingRequests) {
                pendingRequests.put(id, packet);
            }
            ByteBuf buf = Unpooled.buffer();
            try {
                packet.write(new CerberusPacketData(new PacketBuffer(buf)));
                byte[] bytes = drainBuffer(buf);
                handler.send(new BytesWriter(id, packet.packetId == 10, bytes));
            } catch (IOException e) {
                buf.release();
                throw new RuntimeException(e);
            }
        }
        long timeoutMs = packet.packetId == 10 ? (GLOBAL_PING_TIMER.hasElapsed(1000L) ? 250L : 150L)
                : (packet.packetId == 0 ? 780L : 3000L);
        try {
            if (packet.latch.await(timeoutMs, TimeUnit.MILLISECONDS)) {
                return packet.getResult();
            }
        } catch (InterruptedException ignored) {
            throw new RuntimeException("Interrupt");
        }
        Logger.log(LogLevel.ERROR, true, "Cerberus timed out in '{}'", packet.packetId);
        reconnectTimer.reset();
        reconnect(this);
        return INSTANCE.send(packet);
    }

    public String[] sendString(String input) {
        StringPacket packet = new StringPacket(input);
        return send(packet);
    }

    public PingPacket getOrCreatePing() {
        if (cachedPingPacket == null || pingCacheTimer.hasElapsed(3500L)) {
            byte challenge = (byte) (RandomUtil.RANDOM.nextInt(255) - 127);
            cachedPingPacket = new PingPacket(challenge);
            pingCacheTimer.reset();
        }
        return cachedPingPacket;
    }
}
