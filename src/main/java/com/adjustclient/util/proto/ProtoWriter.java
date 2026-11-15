package com.adjustclient.util.proto;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class ProtoWriter {
    private ByteBuffer buffer;
    private int field8;
    private boolean growable;
    private Object field16;

    public ProtoWriter() {
        buffer = ByteBuffer.allocate(64);
        growable = true;
    }

    public ProtoWriter writeRawByte(int value) {
        ensureCapacity(1);
        buffer.put((byte) value);
        return this;
    }

    public ProtoWriter writeRawBytes(long value) {
        while ((value & ~0x7fL) != 0) {
            writeRawByte((int) value & 0x7f | 0x80);
            value >>>= 7;
        }
        return writeRawByte((int) value);
    }

    public ProtoWriter writeBool(int fieldNumber, boolean value) {
        writeTag(fieldNumber, 0);
        writeRawByte(value ? 1 : 0);
        return this;
    }

    public ProtoWriter writeTag(int fieldNumber, int wireType) {
        return writeVarint32(makeTag(fieldNumber, wireType));
    }

    public ProtoWriter writeString(int fieldNumber, String value) {
        byte[] bytes = Objects.requireNonNull(value, "value").getBytes(StandardCharsets.UTF_8);
        writeLength(fieldNumber, bytes.length);
        ensureCapacity(bytes.length);
        buffer.put(bytes);
        return this;
    }

    public ProtoWriter writeBytes(int fieldNumber, byte[] value) {
        Objects.requireNonNull(value, "value");
        writeLength(fieldNumber, value.length);
        ensureCapacity(value.length);
        buffer.put(value);
        return this;
    }

    public ProtoWriter writeBytes(int fieldNumber, byte[] value, int offset, int length) {
        Objects.checkFromIndexSize(offset, length, value.length);
        writeLength(fieldNumber, length);
        ensureCapacity(length);
        buffer.put(value, offset, length);
        return this;
    }

    public ProtoWriter writeVarint32(int value) {
        while ((value & ~0x7f) != 0) {
            writeRawByte(value & 0x7f | 0x80);
            value >>>= 7;
        }
        return writeRawByte(value);
    }

    public ProtoWriter writeVarint64(long value) {
        ensureCapacity(8);
        buffer.put((byte) value);
        buffer.put((byte) (value >>> 8));
        buffer.put((byte) (value >>> 16));
        buffer.put((byte) (value >>> 24));
        buffer.put((byte) (value >>> 32));
        buffer.put((byte) (value >>> 40));
        buffer.put((byte) (value >>> 48));
        buffer.put((byte) (value >>> 56));
        return this;
    }

    public ProtoWriter writeInt32(int fieldNumber, int value) {
        writeTag(fieldNumber, 0);
        if (value < 0) {
            writeRawBytes((long) value);
        } else {
            writeVarint32(value);
        }
        return this;
    }

    public byte[] toByteArray() {
        byte[] bytes = new byte[buffer.position()];
        toByteBuffer().get(bytes);
        return bytes;
    }

    public void ensureCapacity(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count must not be negative");
        }
        if (count <= buffer.remaining()) {
            return;
        }
        if (!growable) {
            throw new BufferOverflowException();
        }
        long required = (long) buffer.position() + count;
        if (required > Integer.MAX_VALUE) {
            throw new BufferOverflowException();
        }
        int capacity = Math.max(1, buffer.capacity());
        while (capacity < required) {
            int doubled = capacity * 2;
            if (doubled <= capacity) {
                capacity = (int) required;
                break;
            }
            capacity = doubled;
        }
        ByteBuffer replacement = ByteBuffer.allocate(capacity);
        buffer.flip();
        replacement.put(buffer);
        buffer = replacement;
    }

    public ProtoWriter writeFixed64(int fieldNumber, long value) {
        writeTag(fieldNumber, 1);
        return writeVarint64(value);
    }

    public static void checkFieldNumber(int fieldNumber) {
        if (fieldNumber < 1 || fieldNumber > 536870911) {
            throw new IllegalArgumentException("fieldNumber must be between 1 and 536870911: " + fieldNumber);
        }
    }

    public ByteBuffer toByteBuffer() {
        ByteBuffer view = buffer.duplicate();
        view.flip();
        return view.slice();
    }

    public static int makeTag(int fieldNumber, int wireType) {
        checkFieldNumber(fieldNumber);
        if (wireType < 0 || wireType > 5) {
            throw new IllegalArgumentException("Invalid protobuf wire type: " + wireType);
        }
        return (fieldNumber << 3) | wireType;
    }

    public ProtoWriter writeLength(int fieldNumber, int length) {
        writeTag(fieldNumber, 2);
        writeVarint32(length);
        return this;
    }
}
