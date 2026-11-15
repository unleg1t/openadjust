package com.adjustclient.module.impl.player.voicechat.protocol.packet;

import com.adjustclient.module.impl.player.voicechat.protocol.type.PacketEncryption;
import com.adjustclient.module.impl.player.voicechat.protocol.type.VoiceProtocolEnumB;
import com.adjustclient.module.impl.player.voicechat.protocol.type.VoiceProtocolEnumH;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.UUID;

public abstract class VoicePacket {
    public PacketEncryption encryption;
    public VoiceProtocolEnumB phase;
    public VoiceProtocolEnumH reliability;

    protected VoicePacket() {
        this(PacketEncryption.SYMMETRIC, VoiceProtocolEnumB.CONNECTED,
                VoiceProtocolEnumH.TCP);
    }
    protected VoicePacket(PacketEncryption encryption, VoiceProtocolEnumB phase, VoiceProtocolEnumH reliability) {
        this.encryption = encryption;
        this.phase = phase;
        this.reliability = reliability;
    }

    public static void writeVarInt(int value, ByteArrayOutputStream out) {
        while ((value & ~0x7f) != 0) {
            out.write((value & 0x7f) | 0x80);
            value >>>= 7;
        }
        out.write(value);
    }

    public static String readString(Charset charset, ByteArrayInputStream in) throws IOException {
        int length = readInt(in);
        if (length > 1_000_000) throw new IOException("String too long: " + length + " bytes");
        byte[] bytes = new byte[length];
        in.read(bytes);
        return new String(bytes, charset);
    }

    public static void writeUUID(UUID uuid, ByteArrayOutputStream out) {
        writeLong(uuid == null ? 0L : uuid.getMostSignificantBits(), out);
        writeLong(uuid == null ? 0L : uuid.getLeastSignificantBits(), out);
    }

    public static void writeString(String value, Charset charset, ByteArrayOutputStream out) throws IOException {
        byte[] bytes = (value == null ? "" : value).getBytes(charset);
        writeInt(bytes.length, out);
        out.write(bytes);
    }

    public static short readShort(ByteArrayInputStream in) {
        synchronized (in) {
            return (short) ((in.read() << 8) | (in.read() & 0xff));
        }
    }

    public byte[] getSupportedVersions() {
        return new byte[0];
    }

    public static UUID readUUID(ByteArrayInputStream in) throws IOException {
        return new UUID(readLong(in), readLong(in));
    }

    public static void writeInt(int value, ByteArrayOutputStream out) {
        byte[] bytes = ByteBuffer.allocate(4).putInt(value).array();
        out.write(bytes, 0, bytes.length);
    }
    public static int readInt(ByteArrayInputStream in) throws IOException {
        byte[] bytes = new byte[4];
        in.read(bytes);
        return ByteBuffer.wrap(bytes).getInt();
    }
    public static void writeLong(long value, ByteArrayOutputStream out) {
        byte[] bytes = ByteBuffer.allocate(8).putLong(value).array();
        out.write(bytes, 0, bytes.length);
    }
    public static long readLong(ByteArrayInputStream in) throws IOException {
        byte[] bytes = new byte[8];
        in.read(bytes);
        return ByteBuffer.wrap(bytes).getLong();
    }
}
