package com.adjustclient.managers.telemetry;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TelemetryOutput {
    private final ByteArrayOutputStream buffer;
    public final DataOutputStream out;

    public TelemetryOutput() {
        this.buffer = new ByteArrayOutputStream();
        this.out = new DataOutputStream(this.buffer);
    }

    public TelemetryOutput(ByteArrayOutputStream buffer) {
        this.buffer = buffer;
        this.out = new DataOutputStream(buffer);
    }

    public void reset() {
        buffer.reset();
    }

    public byte[] toByteArray() {
        return buffer.toByteArray();
    }

    public void writeBoolean(boolean value) throws IOException {
        out.writeBoolean(value);
    }

    public void writeByte(int value) throws IOException {
        out.writeByte(value);
    }

    public void writeShort(int value) throws IOException {
        out.writeShort(value);
    }

    public void writeInt(int value) throws IOException {
        out.writeInt(value);
    }

    public void writeFloat(float value) throws IOException {
        out.writeFloat(value);
    }

    public void writeLong(long value) throws IOException {
        out.writeLong(value);
    }

    public void writeUTF(String value) throws IOException {
        out.writeUTF(value);
    }

    public void writeBytes(byte[] bytes) throws IOException {
        out.write(bytes);
    }

    public void writeVarInt(int value) throws IOException {
        while ((value & ~0x7F) != 0) {
            out.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.writeByte(value);
    }

    public void writeVarLong(long value) throws IOException {
        while ((value & ~0x7FL) != 0L) {
            out.writeByte(((int) value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.writeByte((int) value);
    }
}
