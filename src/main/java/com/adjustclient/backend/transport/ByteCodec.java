package com.adjustclient.backend.transport;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class ByteCodec {
    public static void writeLong(byte[] buffer, int offset, long value) {
        buffer[offset] = (byte) (value >>> 56);
        buffer[offset + 1] = (byte) (value >>> 48);
        buffer[offset + 2] = (byte) (value >>> 40);
        buffer[offset + 3] = (byte) (value >>> 32);
        buffer[offset + 4] = (byte) (value >>> 24);
        buffer[offset + 5] = (byte) (value >>> 16);
        buffer[offset + 6] = (byte) (value >>> 8);
        buffer[offset + 7] = (byte) value;
    }

    public static String readString(byte[] buffer, int offset) {
        int length = ((buffer[offset] & 255) << 8) + (buffer[offset + 1] & 255);
        char[] characters = new char[length];
        for (int i = 0; i < length; i++) {
            characters[i] = (char) (((buffer[offset + 2 + 2 * i] & 255) << 8)
                    + (buffer[offset + 3 + 2 * i] & 255));
        }
        return new String(characters);
    }

    /** Reads a big-endian long; address name preserves existing feature callers. */
    public static long readLong(byte[] buffer, int offset) {
        return ((long) buffer[offset] << 56)
                | ((long) (buffer[offset + 1] & 255) << 48)
                | ((long) (buffer[offset + 2] & 255) << 40)
                | ((long) (buffer[offset + 3] & 255) << 32)
                | ((long) (buffer[offset + 4] & 255) << 24)
                | ((long) (buffer[offset + 5] & 255) << 16)
                | ((long) (buffer[offset + 6] & 255) << 8)
                | (buffer[offset + 7] & 255L);
    }

    /** Reads a big-endian int. */
    public static int readInt(byte[] buffer, int offset) {
        return ((buffer[offset] & 255) << 24)
                | ((buffer[offset + 1] & 255) << 16)
                | ((buffer[offset + 2] & 255) << 8)
                | (buffer[offset + 3] & 255);
    }

    public static int readIntLE(byte[] buffer, int offset) {
        int b3 = buffer[offset + 3] & 255;
        int b2 = buffer[offset + 2] & 255;
        int b1 = buffer[offset + 1] & 255;
        int b0 = buffer[offset] & 255;
        return b0 | (b1 << 8) | (b2 << 16) | (b3 << 24);
    }

    /** Reads the UUID's most-significant and least-significant long in network order. */
    public static UUID readUuid(byte[] buffer, int offset) {
        return new UUID(readLong(buffer, offset), readLong(buffer, offset + 8));
    }

    public static void writeIntLE(byte[] buffer, int offset, int value) {
        buffer[offset + 3] = (byte) (value >>> 24);
        buffer[offset + 2] = (byte) (value >>> 16);
        buffer[offset + 1] = (byte) (value >>> 8);
        buffer[offset] = (byte) value;
    }

    public static int endpointHash(String endpoint) {
        int hash = 2673;
        for (byte value : endpoint.getBytes(StandardCharsets.UTF_16LE)) {
            hash = 33 * hash + value;
        }
        return hash;
    }
}
