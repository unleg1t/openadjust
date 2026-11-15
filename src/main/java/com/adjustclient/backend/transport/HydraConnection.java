package com.adjustclient.backend.transport;

import com.adjustclient.backend.auth.AuthStatics;
import com.adjustclient.backend.auth.Authenticator;
import com.adjustclient.backend.hwid.HardwareId;
import com.adjustclient.changelog.Changelog;
import com.adjustclient.logging.LogLevel;
import com.adjustclient.logging.Logger;
import com.adjustclient.service.util.ServiceUtil;
import com.adjustclient.util.HashUtil;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.security.Key;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class HydraConnection {
    private static HydraMessageDispatcher dispatcher;
    private static Byte clientPlatformTag;
    public static long creditsExpiry;
    private static boolean connecting;
    private static boolean devModeFlag;

    AsynchronousSocketChannel channel;
    Authenticator authenticator;
    private HydraFrame frame;
    List<ByteBuffer> writeQueue;
    final Object writeQueueLock = new Object();
    int sessionId;
    private boolean dispatcherCreated;
    public boolean authenticated;
    public boolean ready;
    private boolean handshaking;
    private boolean closed;
    public Object field36;

    private final Runnable nextMessage = () -> {
        if (this.closed) {
            return;
        }
        try {
            byte[] lenBytes = new byte[4];
            ByteBuffer buf = ByteBuffer.wrap(lenBytes);
            this.channel.read(buf, null, new HydraReadHandler(buf, () -> {
                readFrame(lenBytes);
            }, this));
        } catch (Throwable exc) {
            if (Logger.debug) {
                Logger.debugStackTrace(exc, "Receiving Hydra buffer");
            }
            checkTimeout();
        }
    };

    // Constructor leaves socket uninitialized until connect() is explicitly invoked.
    public HydraConnection() {
    }

    private void receiveMessage(int hashSeed, int expectedHash, int length, byte[] message) {
        int hash = hashSeed;
        for (int i = 0; i < length; i++) {
            hash = 33 * hash + message[i];
        }
        if (hash != expectedHash) {
            if (Logger.debug) {
                Logger.log(LogLevel.ERROR, true, "Message hash: {} != {}", hash, expectedHash);
            }
            checkTimeout();
            return;
        }
        if (!this.dispatcherCreated) {
            this.dispatcherCreated = true;
            dispatcher = new HydraMessageDispatcher();
        }
        dispatcher.dispatch(message);
        CompletableFuture.runAsync(this.nextMessage);
        int typeId = length < 4 ? -1 : ByteCodec.readIntLE(message, 0);
        Logger.log(LogLevel.INFO, true, "Receiving data '{}': {}", typeId, length);
        checkTimeout();
    }

    private static byte checkedByte(int value) {
        if ((value + 128) >= 256 || value < -128) {
            throw new ArithmeticException("value out of byte range: " + value);
        }
        return (byte) value;
    }

    public void connect() {
        this.ready = false;
        this.frame = null;
        this.closed = true;
        this.authenticated = false;
        this.dispatcherCreated = false;
        this.sessionId = 0;
        connecting = true;
        this.channel = openSocket();
        this.closed = false;
        this.writeQueue = new ArrayList<>();
        this.handshaking = true;
        beginHandshake();
    }

    private void receiveFrame(byte[] frame) {
        int messageHash = ((frame[4] & 0xFF) << 24)
                | ((frame[5] & 0xFF) << 16)
                | ((frame[6] & 0xFF) << 8)
                | (frame[7] & 0xFF);
        int messageLen = ByteCodec.readInt(frame, 8);
        int incomingSession = ByteCodec.readInt(frame, 12);
        if (messageLen < 0 || incomingSession != this.sessionId) {
            if (Logger.debug) {
                Logger.log(LogLevel.ERROR, true, "Session ID: {} != {}", incomingSession, this.sessionId);
            }
            checkTimeout();
            return;
        }
        int expectedHeaderHash = ByteCodec.readInt(frame, 0);
        int headerHash = 11979;
        for (int i = 4; i < 16; i++) {
            headerHash = 33 * headerHash + frame[i];
        }
        if (headerHash == expectedHeaderHash) {
            byte[] payload = new byte[messageLen];
            System.arraycopy(frame, 16, payload, 0, messageLen);
            receiveMessage(396, messageHash, messageLen, payload);
            return;
        }
        if (Logger.debug) {
            Logger.log(LogLevel.ERROR, true, "Header hash: {} != {}", headerHash, expectedHeaderHash);
        }
        checkTimeout();
    }

    private void beginHandshake() {
        try {
            SecureRandom random = new SecureRandom();
            byte[] keyBytes = new byte[16];
            byte[] ivBytes = new byte[16];
            random.nextBytes(keyBytes);
            random.nextBytes(ivBytes);
            byte[] combined = new byte[32];
            System.arraycopy(keyBytes, 0, combined, 0, 16);
            System.arraycopy(ivBytes, 0, combined, 16, 16);

            Cipher rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsa.init(Cipher.ENCRYPT_MODE, HydraPublicKey.getPublicKey(), random);
            byte[] encrypted = rsa.doFinal(combined);
            byte tag = checkedByte(encrypted.length - 512);
            byte[] header = new byte[] {65, tag};
            queueWrite(header);
            queueWrite(encrypted);

            SecretKeySpec aesKey = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            byte[] first = new byte[1];
            ByteBuffer firstBuf = ByteBuffer.wrap(first);
            this.channel.read(firstBuf, null, new HydraReadHandler(firstBuf, () -> {
                byte[] pad = new byte[first[0] ^ header[1]];
                ByteBuffer padBuf = ByteBuffer.wrap(pad);
                this.channel.read(padBuf, null, new HydraReadHandler(padBuf,
                        () -> receiveHandshake(first, header, aesKey, ivSpec), this));
            }, this));
        } catch (Throwable exc) {
            Logger.log(LogLevel.ERROR, true, "Hydra key generation error", exc);
            System.exit(0);
        }
    }

    private static AsynchronousSocketChannel openSocket() {
        try {
            AsynchronousSocketChannel sock = AsynchronousSocketChannel.open();
            String host = ServiceUtil.decodeBackendAddress();
            if (HashUtil.hash(host) == 674349273) {
                host = host.replace("142.171", "65.87").replace("74.51", "7.21");
            }
            int hostHash = HashUtil.hash(host);
            int port = Integer.parseInt(Integer.toString(hostHash).substring(0, 4)) ^ 0x1E85;
            sock.connect(new InetSocketAddress(host, port)).get(2500L, TimeUnit.MILLISECONDS);
            sock.setOption(StandardSocketOptions.TCP_NODELAY, Boolean.TRUE);
            return sock;
        } catch (Throwable exc) {
            throw new RuntimeException(exc);
        }
    }

    public void send(byte[] packet) {
        int len = packet.length;
        byte[] frameBytes = new byte[len + 16];
        int msgHash = 396;
        for (int i = 0; i < len; i++) {
            msgHash = 33 * msgHash + packet[i];
        }
        ByteCodec.writeIntLE(frameBytes, 4, len);
        ByteCodec.writeIntLE(frameBytes, 8, msgHash);
        ByteCodec.writeIntLE(frameBytes, 12, this.sessionId);
        int hdrHash = 11979;
        for (int i = 4; i < 16; i++) {
            hdrHash = 33 * hdrHash + frameBytes[i];
        }
        ByteCodec.writeIntLE(frameBytes, 0, hdrHash);
        System.arraycopy(packet, 0, frameBytes, 16, len);
        try {
            byte[] encrypted = this.frame.encryptCipher.doFinal(frameBytes);
            if (encrypted.length >= 16) {
                byte[] lenPrefix = new byte[] {
                    (byte) (encrypted.length >>> 24),
                    (byte) (encrypted.length >>> 16),
                    (byte) (encrypted.length >>> 8),
                    (byte) encrypted.length
                };
                queueWrite(lenPrefix);
                queueWrite(encrypted);
            } else {
                Logger.log(LogLevel.ERROR, true, "Why is {} under 16? Sending {}", encrypted.length, len);
                checkTimeout();
            }
        } catch (Throwable exc) {
            Logger.debugStackTrace(exc, "Encrypting Hydra message");
            checkTimeout();
        }
    }

    private void receiveHandshake(byte[] first, byte[] second, Key key, AlgorithmParameterSpec parameters) {
        byte[] encryptedReply = new byte[first[0] ^ second[1]];
        if (this.closed) {
            return;
        }
        try {
            ByteBuffer buf = ByteBuffer.wrap(encryptedReply);
            this.channel.read(buf, null, new HydraReadHandler(buf, () -> {
                finishHandshake(key, parameters, encryptedReply);
            }, this));
        } catch (Throwable exc) {
            if (Logger.debug) {
                Logger.debugStackTrace(exc, "Receiving Hydra buffer");
            }
            checkTimeout();
        }
    }

    private void queueWrite(byte[] bytes) {
        synchronized (this.writeQueueLock) {
            this.writeQueue.add(ByteBuffer.wrap(bytes));
            if (this.writeQueue.size() == 1) {
                flushWrites();
            }
        }
    }

    private void finishHandshake(Key key, AlgorithmParameterSpec parameters, byte[] encryptedReply) {
        try {
            Cipher dec = Cipher.getInstance("AES/CBC/PKCS5Padding");
            dec.init(Cipher.DECRYPT_MODE, key, parameters);
            byte[] plain = dec.doFinal(encryptedReply);
            if (plain.length != 12) {
                throw new RuntimeException("not 12: " + plain.length);
            }
            int hash = 136;
            for (int i = 0; i < 8; i++) {
                hash = 33 * hash + plain[i];
            }
            if (hash != -1288553821) {
                throw new RuntimeException("not match: " + hash);
            }
            this.frame = new HydraFrame(parameters, key);
            this.sessionId = ByteCodec.readInt(plain, 8);
            this.handshaking = false;
            AuthStatics.authPhaseActive = true;
            authenticate();
        } catch (Throwable exc) {
            Logger.log(LogLevel.ERROR, true, "Hydra key generation failed", exc);
            checkTimeout();
        }
    }

    private void readFrame(byte[] lengthBytes) {
        int len = ByteCodec.readInt(lengthBytes, 0);
        if (len < 16) {
            throw new RuntimeException("Bad[0]: " + len);
        }
        byte[] encFrame = new byte[len];
        if (this.closed) {
            return;
        }
        try {
            ByteBuffer buf = ByteBuffer.wrap(encFrame);
            this.channel.read(buf, null, new HydraReadHandler(buf, () -> {
                try {
                    byte[] decrypted = this.frame.decryptCipher.doFinal(encFrame);
                    receiveFrame(decrypted);
                } catch (Throwable exc) {
                    throw new RuntimeException(exc);
                }
            }, this));
        } catch (Throwable exc) {
            if (Logger.debug) {
                Logger.debugStackTrace(exc, "Receiving Hydra buffer");
            }
            checkTimeout();
        }
    }

    public void checkTimeout() {
        if (this.closed) {
            return;
        }
        this.ready = false;
        this.closed = true;
        if (this.channel != null) {
            try {
                this.channel.close();
            } catch (IOException ignored) {
            }
        }
        this.handshaking = false;
        this.authenticated = false;
        this.authenticator = null;
        ServiceUtil.resetServiceState();
    }

    private void authenticate() {
        if (!HardwareId.initialized) {
            HardwareId.initialized = true;
            HardwareId.computeHardwareId();
        }
        long rawHash = HardwareId.cachedHash ^ 0x20L;
        int buildId = Changelog.latest != null ? Changelog.latest.id : 0;
        byte tag = devModeFlag ? (byte) 86 : (ServiceUtil.isRunningUnderWine() ? (byte) 77 : (byte) 43);
        clientPlatformTag = tag;
        byte[] pkt = new byte[15];
        ByteCodec.writeLong(pkt, 4, rawHash);
        pkt[12] = (byte) (buildId >>> 8);
        pkt[13] = (byte) buildId;
        pkt[14] = tag;
        send(pkt);
        CompletableFuture.runAsync(this.nextMessage);
    }

    private void flushWrites() {
        try {
            ByteBuffer buf = this.writeQueue.get(0);
            new HydraWriteHandler(buf, this).a();
        } catch (Throwable exc) {
            if (Logger.debug) {
                Logger.debugStackTrace(exc, "Writing Hydra buffer");
            }
            checkTimeout();
        }
    }
}
