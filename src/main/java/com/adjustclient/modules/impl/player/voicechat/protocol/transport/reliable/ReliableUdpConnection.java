package com.adjustclient.modules.impl.player.voicechat.protocol.transport.reliable;

import com.adjustclient.modules.impl.player.voicechat.protocol.VoiceCryptoHandler;
import com.adjustclient.modules.impl.player.voicechat.protocol.packet.VoicePacket;
import com.adjustclient.modules.impl.player.voicechat.protocol.type.FrameType;
import com.adjustclient.modules.impl.player.voicechat.protocol.type.PacketEncryption;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

public final class ReliableUdpConnection {
    public final InetSocketAddress remote;
    public final DatagramSender receiver;
    public final ConcurrentHashMap<Short, PendingFrame> incomplete = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<Short, PendingFrame> outgoing = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<Short, PendingFrame> queued = new ConcurrentHashMap<>();
    public FrameType networkVersion = FrameType.UNIDENTIFIED;
    public VoiceCryptoHandler symmetricCrypto;
    public VoiceCryptoHandler asymmetricCrypto;
    public short sendSequence;
    public short receiveSequence;
    public long missingSince;
    public boolean waitingForResetAck;

    public ReliableUdpConnection(InetSocketAddress remote, DatagramSender receiver) {
        this.remote = remote;
        this.receiver = receiver;
    }

    public boolean hasKeyFor(PacketEncryption encryption) {
        return switch (encryption) {
            case ASYMMETRIC -> asymmetricCrypto != null;
            case SYMMETRIC -> symmetricCrypto != null;
            case NONE -> true;
        };
    }

    public void resendUnacknowledgedFrames(DatagramSocket socket) {
        try {
            Map<Short, PendingFrame> snapshot;
            synchronized (outgoing) { snapshot = new HashMap<>(outgoing); }
            for (Map.Entry<Short, PendingFrame> entry : snapshot.entrySet()) {
                PendingFrame frame = entry.getValue();
                synchronized (outgoing) {
                    if (frame.isComplete() || frame.isExpired()) {
                        outgoing.remove(entry.getKey());
                        continue;
                    }
                }
                boolean sent = false;
                for (int part = 0; part < (short) frame.segments.length; part++) {
                    boolean shouldResend;
                    synchronized (outgoing) { shouldResend = frame.shouldResend(part); }
                    if (shouldResend) {
                        byte[] payload;
                        synchronized (frame.segments) { payload = frame.segments[part]; }
                        sendSegment(socket, entry.getKey(), (short) part, (short) frame.segments.length, payload);
                        sent = true;
                    }
                }
                if (sent) frame.markSent();
            }
            synchronized (incomplete) {
                for (Map.Entry<Short, PendingFrame> entry : new HashMap<>(incomplete).entrySet()) {
                    if (entry.getValue().isExpired()) incomplete.remove(entry.getKey());
                }
            }
        } catch (Exception error) {
            System.out.println("[" + remote.getHostString() + "] [IN] Failed to resend unacknowledged frames: "
                    + error.getClass().getSimpleName());
        }
    }

    public void processQueuedFrames(Object context) throws Exception {
        while (true) {
            PendingFrame frame;
            synchronized (incomplete) { frame = queued.remove(receiveSequence); }
            if (frame == null) return;
            receiver.processCompleteFrame(decodePacket(frame.assemble(), context));
            synchronized (incomplete) { receiveSequence++; }
        }
    }

    public int sendFrame(DatagramSocket socket, byte[] payload) throws Exception {
        short sequence;
        synchronized (outgoing) { sequence = sendSequence++; }
        short count = (short) Math.max(1, (int) Math.ceil(payload.length / 512.0));
        byte[][] segments = new byte[count][];
        boolean[] acknowledged = new boolean[count];
        long created = System.currentTimeMillis();
        long lastSent;
        synchronized (segments) { lastSent = System.currentTimeMillis(); }
        int sent = 0;
        for (short part = 0; part < count; part++) {
            int offset = part << 9;
            int length = Math.min(512, payload.length - offset);
            byte[] segment = new byte[length];
            System.arraycopy(payload, offset, segment, 0, length);
            sent += sendSegment(socket, sequence, part, count, segment);
            synchronized (segments) {
                segments[part] = segment;
                acknowledged[part] = false;
            }
        }
        synchronized (outgoing) {
            outgoing.put(sequence, new PendingFrame(segments, acknowledged, sequence, created, lastSent));
        }
        return sent;
    }

    public static boolean isHandshakeMagic(byte[] data) {
        return data.length >= 10 && data[0] == 0 && data[1] == (byte) 0x80
                && data[2] == 0x1a && data[3] == (byte) 0xb1 && data[4] == (byte) 0xb1
                && data[5] == 0x1f && data[6] == (byte) 0xdc && data[7] == 0x4e
                && data[8] == (byte) 0x80;
    }

    public byte[] decodePacket(byte[] data, Object context) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        int flags = (byte) in.read();
        int encryptionId = flags & 3;
        if (encryptionId >= 3) throw new IllegalArgumentException("Invalid encrypt type id: " + encryptionId);
        PacketEncryption encryption = PacketEncryption.values()[encryptionId];
        if ((flags & 4) != 0) {
            int size = 0, bytes = 0, value;
            do {
                value = (byte) in.read();
                size |= (value & 127) << (bytes++ * 7);
                if (bytes > 5) throw new IndexOutOfBoundsException("VarInt too big");
            } while ((value & 128) != 0);
            in.skip(size);
        }
        if ((flags & 8) != 0) {
            byte version = (byte) in.read();
            if (networkVersion == FrameType.UNIDENTIFIED) networkVersion = FrameType.values()[version];
        } else if (networkVersion == FrameType.UNIDENTIFIED) {
            networkVersion = FrameType.V2;
        }
        int length = 0, bytes = 0, value;
        do {
            value = (byte) in.read();
            length |= (value & 127) << (bytes++ * 7);
            if (bytes > 5) throw new IndexOutOfBoundsException("VarInt too big");
        } while ((value & 128) != 0);
        byte[] encrypted = new byte[length];
        in.read(encrypted, 0, encrypted.length);
        return decrypt(encryption, encrypted);
    }

    public byte[] decrypt(PacketEncryption encryption, byte[] data) throws Exception {
        if (!hasKeyFor(encryption)) throw new IllegalStateException("Can't decrypt packet without " + encryption + " encryption key");
        return switch (encryption) {
            case ASYMMETRIC -> asymmetricCrypto.decrypt(data, asymmetricCrypto.decryptCipher);
            case SYMMETRIC -> symmetricCrypto.decrypt(data, symmetricCrypto.decryptCipher);
            case NONE -> data;
        };
    }

    public void sendDatagram(DatagramSocket socket, short sequence, short segment) throws Exception {
        byte[] ack = {2, (byte) (sequence >> 8), (byte) sequence, (byte) (segment >> 8), (byte) segment};
        DatagramPacket packet = new DatagramPacket(ack, ack.length, remote);
        socket.send(packet);
        packet.getLength();
    }

    public void handleSegment(DatagramSocket socket, PendingFrame frame, Object context) throws Exception {
        short sequence = frame.sequence;
        synchronized (incomplete) {
            boolean older;
            if (sequence > 0 && receiveSequence < 0) older = receiveSequence - sequence < -32767;
            else if (sequence < 0 && receiveSequence > 0) older = sequence - receiveSequence >= -32766;
            else older = receiveSequence > sequence;
            if (older) return;
            if (sequence != receiveSequence) {
                if (queued.isEmpty()) missingSince = System.currentTimeMillis();
                queued.put(sequence, frame);
                if (System.currentTimeMillis() - missingSince > 5000) {
                    System.out.println("[" + remote.getHostString() + "] [IN] Drop missing frame (Remote: "
                            + sequence + ", Local: " + receiveSequence + ")");
                    receiveSequence++;
                    processQueuedFrames(context);
                    waitingForResetAck = true;
                    sendSequence = 0;
                    sendFrame(socket, new byte[0]);
                }
                return;
            }
        }
        receiver.processCompleteFrame(decodePacket(frame.assemble(), context));
        synchronized (incomplete) { receiveSequence++; }
        processQueuedFrames(context);
    }

    public void processPacket(byte[] data, int length, Object context) throws Exception {
        byte packetId = data[0];
        int headerLength = packetId == 0 ? 8 : 1;
        PacketEncryption encryption = packetId == 0 ? PacketEncryption.ASYMMETRIC
                : packetId == 2 ? PacketEncryption.NONE : PacketEncryption.SYMMETRIC;
        if (packetId != 0 && !hasKeyFor(PacketEncryption.SYMMETRIC)) {
            System.out.println("[" + remote.getHostString() + "] [IN] Tried to process packet 0x"
                    + Long.toHexString(packetId) + " before handshake");
            return;
        }
        int size = length - headerLength;
        if (size > 1024) throw new IllegalStateException("Invalid frame size");
        byte[] body = new byte[size];
        System.arraycopy(data, headerLength, body, 0, size);
        try {
            body = decrypt(encryption, body);
            byte[] decoded = new byte[body.length + 1];
            decoded[0] = packetId;
            System.arraycopy(body, 0, decoded, 1, body.length);
            networkVersion = FrameType.V1;
            receiver.processCompleteFrame(decoded);
        } catch (IllegalBlockSizeException ignored) {
        } catch (BadPaddingException badKey) {
            System.out.println("[" + remote.getHostString() + "] [IN] Tried to process packet 0x"
                    + Long.toHexString(packetId) + " with invalid encryption key");
        }
    }

    public int sendEncrypted(DatagramSocket socket, byte[] packet, PacketEncryption encryption, Object context) throws Exception {
        byte[] encrypted = encryptFrame(encryption, Arrays.copyOfRange(packet, 1, packet.length));
        byte[] result = new byte[encrypted.length + 1];
        result[0] = packet[0];
        System.arraycopy(encrypted, 0, result, 1, encrypted.length);
        DatagramPacket datagram = new DatagramPacket(result, result.length, remote);
        socket.send(datagram);
        return datagram.getLength();
    }

    public byte[] encodeFrame(byte[] extension, byte[] payload, PacketEncryption encryption, Object context) throws Exception {
        int flags = encryption.ordinal() | (extension.length != 0 ? 4 : 0) | (networkVersion.ordinal() >= 3 ? 8 : 0);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(flags);
        if (extension.length != 0) {
            VoicePacket.writeVarInt(extension.length, out);
            out.write(extension, 0, extension.length);
        }
        if (networkVersion.ordinal() >= 3) out.write(networkVersion.ordinal());
        byte[] encrypted = encryptFrame(encryption, payload);
        VoicePacket.writeVarInt(encrypted.length, out);
        out.write(encrypted, 0, encrypted.length);
        return out.toByteArray();
    }

    public int sendSegment(DatagramSocket socket, short sequence, short segment, short count, byte[] data) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(1);
        out.write(sequence >> 8); out.write(sequence);
        out.write(segment >> 8); out.write(segment);
        out.write(count >> 8); out.write(count);
        out.write(data.length >> 8); out.write(data.length);
        out.write(data, 0, data.length);
        byte[] encoded = out.toByteArray();
        DatagramPacket datagram = new DatagramPacket(encoded, encoded.length, remote);
        socket.send(datagram);
        return datagram.getLength();
    }

    public void receive(DatagramSocket socket, byte[] data, int offset, int length, Object context) throws Exception {
        if (networkVersion == FrameType.V1
                || (networkVersion == FrameType.UNIDENTIFIED && isHandshakeMagic(data))) {
            processPacket(data, length, context);
            return;
        }
        length = Math.min(length, data.length);
        if (length < 1) return;
        int kind = data[0];
        ByteArrayInputStream in = new ByteArrayInputStream(data, 1, length - 1);
        if (kind == 2) {
            short sequence = VoicePacket.readShort(in), segment = VoicePacket.readShort(in);
            synchronized (outgoing) {
                PendingFrame frame = outgoing.get(sequence);
                if (frame == null) return;
                synchronized (frame.segments) { frame.acknowledged[segment] = true; }
                if (frame.isComplete()) {
                    outgoing.remove(sequence);
                    if (frame.isEmptyFrame()) waitingForResetAck = false;
                }
            }
        } else if (kind == 1) {
            if (waitingForResetAck) return;
            short sequence = VoicePacket.readShort(in), segment = VoicePacket.readShort(in);
            short count = VoicePacket.readShort(in), size = VoicePacket.readShort(in);
            if (count >= 101 || size >= 513) throw new IllegalStateException("Invalid segment length");
            byte[] payload = new byte[size];
            in.read(payload, 0, payload.length);
            PendingFrame frame;
            synchronized (incomplete) {
                frame = incomplete.computeIfAbsent(sequence,
                        ignored -> new PendingFrame(sequence, count));
                frame.setSegment(segment, payload, true);
            }
            sendDatagram(socket, sequence, segment);
            if (!frame.isComplete()) return;
            synchronized (incomplete) {
                if (incomplete.remove(sequence) == null) return;
            }
            if (frame.isEmptyFrame()) {
                receiveSequence = (short) (sequence + 1);
                if (networkVersion.ordinal() >= 3) {
                    sendSequence = 0;
                    incomplete.clear(); outgoing.clear(); queued.clear();
                }
            } else if (networkVersion.ordinal() >= 3 || (frame.getPacketId() & 8) != 0) {
                handleSegment(socket, frame, context);
            } else {
                decodeAndDispatch(frame.assemble(), context);
            }
        } else if (kind == 0) {
            if (length - 1 > 1024) throw new IllegalStateException("Invalid frame size");
            decodeAndDispatch(Arrays.copyOfRange(data, 1, length), context);
        }
    }

    public int sendUnreliable(DatagramSocket socket, byte[] extension, byte[] data, PacketEncryption encryption, Object context) throws Exception {
        byte[] frame = encodeFrame(extension, data, encryption, context);
        byte[] result = new byte[frame.length + 1];
        System.arraycopy(frame, 0, result, 1, frame.length);
        DatagramPacket packet = new DatagramPacket(result, result.length, remote);
        socket.send(packet);
        return packet.getLength();
    }

    public byte[] encryptFrame(PacketEncryption encryption, byte[] data) throws Exception {
        if (!hasKeyFor(encryption)) throw new IllegalStateException("Can't encrypt packet without " + encryption + " encryption key");
        return switch (encryption) {
            case ASYMMETRIC -> asymmetricCrypto.encrypt(data, asymmetricCrypto.encryptCipher);
            case SYMMETRIC -> symmetricCrypto.encrypt(data, symmetricCrypto.encryptCipher);
            case NONE -> data;
        };
    }

    public int sendReliable(DatagramSocket socket, byte[] extension, byte[] data, PacketEncryption encryption, Object context) throws Exception {
        return sendFrame(socket, encodeFrame(extension, data, encryption, context));
    }

    public int sendRaw(DatagramSocket socket, byte[] data) throws Exception {
        DatagramPacket datagram = new DatagramPacket(data, data.length, remote);
        socket.send(datagram);
        return datagram.getLength();
    }

    public void decodeAndDispatch(byte[] data, Object context) throws Exception {
        receiver.processCompleteFrame(decodePacket(data, context));
    }
}
