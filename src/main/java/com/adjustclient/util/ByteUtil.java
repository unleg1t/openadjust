package com.adjustclient.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ByteUtil {
    public static void writeVarInt(DataOutputStream output, int value) throws IOException {
        while ((value & ~0x7f) != 0) {
            output.writeByte((value & 0x7f) | 0x80);
            value >>>= 7;
        }
        output.writeByte(value);
    }

    public static void writeInt(DataOutputStream output, byte[] buffer) throws IOException {
        output.writeInt(buffer.length);
        output.write(buffer);
    }

    public static void readFully(DataInputStream input, byte[] buffer) throws IOException {
        int offset = 0;
        while (offset != buffer.length) {
            int count = input.read(buffer, offset, buffer.length - offset);
            if (count <= 0) throw new IOException("eof");
            offset += count;
            if (offset > buffer.length) throw new IOException("exceeded length");
        }
    }

    public static int readInt(byte[] data, int offset) {
        return (data[offset] & 255) << 24 | (data[offset + 1] & 255) << 16
                | (data[offset + 2] & 255) << 8 | data[offset + 3] & 255;
    }

    public static void writeVarLong(DataOutputStream output, long value) throws IOException {
        while ((value & ~0x7fL) != 0) {
            output.writeByte((int) (value & 0x7f) | 0x80);
            value >>>= 7;
        }
        output.writeByte((int) value);
    }

    public static int readVarInt(DataInputStream input) throws IOException {
        int value = 0;
        int count = 0;
        byte next;
        do {
            next = input.readByte();
            if (++count > 5) throw new IOException("VI too big");
            value |= (next & 0x7f) << (7 * (count - 1));
        } while (next < 0);
        return value;
    }

    public static long readVarLong(DataInputStream input) throws IOException {
        long value = 0;
        int count = 0;
        byte next;
        do {
            next = input.readByte();
            if (++count > 10) throw new RuntimeException("VL too big");
            value |= (long) (next & 0x7f) << (7 * (count - 1));
        } while (next < 0);
        return value;
    }
}
