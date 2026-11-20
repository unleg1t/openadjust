package com.adjustclient.utilities.proto;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public final class ProtoReader {
    private final ByteBuffer buffer;
    private Object field8;

    public ProtoReader(ByteBuffer input) {
        buffer = input.slice();
    }

    public String readString() {
        return new String(readBytes(), StandardCharsets.UTF_8);
    }

    public byte[] readBytes() {
        return readRawBytes(readLength());
    }

    public ByteBuffer readByteBuffer() {
        return readByteBuffer(readLength());
    }

    public ProtoReader readMessage() {
        return new ProtoReader(readByteBuffer());
    }

    public long readEnum() {
        long value = 0;
        for (int shift = 0; shift < 63; shift += 7) {
            int next = readBool();
            value |= (long) (next & 0x7f) << shift;
            if ((next & 0x80) == 0) {
                return value;
            }
        }
        int last = readBool();
        if ((last & 0xfe) != 0) {
            throw truncated("Varint exceeds 64 bits");
        }
        return value | (long) last << 63;
    }

    public int readBool() {
        hasRemaining(1);
        return buffer.get() & 0xff;
    }

    public int readLength() {
        int length = readVarint32("length");
        if (length < 0) {
            throw truncated("Length exceeds the supported signed integer range");
        }
        hasRemaining(length);
        return length;
    }

    public byte[] readRawBytes(int length) {
        if (length < 0) {
            throw truncated("Negative length");
        }
        hasRemaining(length);
        byte[] result = new byte[length];
        buffer.get(result);
        return result;
    }

    public int readVarint32(String kind) {
        long value = readEnum();
        if ((value & 0xffffffff00000000L) != 0) {
            throw truncated("Protobuf " + kind + " exceeds 32 bits");
        }
        return (int) value;
    }

    public void skipGroup(int fieldNumber, int depth) {
        if (depth > 100) {
            throw truncated("Group nesting exceeds 100");
        }
        while (true) {
            int tag = readTag();
            if (tag == 0) {
                throw truncated("Unexpected end of input inside a group");
            }
            int number = tag >>> 3;
            int wireType = tag & 7;
            if (wireType == 3) {
                skipGroup(number, depth + 1);
            } else if (wireType == 4) {
                if (number != fieldNumber) {
                    throw truncated("Mismatched end-group tag");
                }
                return;
            } else {
                skipField(tag);
            }
        }
    }

    public ByteBuffer readByteBuffer(int length) {
        if (length < 0) {
            throw truncated("Negative length");
        }
        hasRemaining(length);
        ByteBuffer view = buffer.slice();
        view.limit(length);
        buffer.position(buffer.position() + length);
        return view;
    }

    public IllegalArgumentException truncated(String message) {
        return new IllegalArgumentException(message + " at byte " + buffer.position());
    }

    public void skipBytes(int count) {
        hasRemaining(count);
        buffer.position(buffer.position() + count);
    }

    public boolean skipField(int tag) {
        switch (tag & 7) {
            case 0:
                readEnum();
                break;
            case 1:
                skipBytes(8);
                break;
            case 2:
                skipBytes(readLength());
                break;
            case 3:
                skipGroup(tag >>> 3, 1);
                break;
            case 4:
                return false;
            case 5:
                skipBytes(4);
                break;
            default:
                throw truncated("Invalid protobuf wire type " + (tag & 7));
        }
        return true;
    }

    public int readTag() {
        if (!buffer.hasRemaining()) {
            return 0;
        }
        int tag = readVarint32("tag");
        if ((tag >>> 3) == 0) {
            throw truncated("Invalid tag with field number 0");
        }
        if ((tag & 7) >= 6) {
            throw truncated("Invalid protobuf wire type " + (tag & 7));
        }
        return tag;
    }

    public void hasRemaining(int count) {
        if (count < 0) {
            throw truncated("Negative length");
        }
        int remaining = buffer.remaining();
        if (count > remaining) {
            throw truncated("Truncated input: needed " + count + " bytes but only " + remaining + " remain");
        }
    }

    public int readFixed32() {
        hasRemaining(4);
        return (buffer.get() & 0xff)
                | (buffer.get() & 0xff) << 8
                | (buffer.get() & 0xff) << 16
                | (buffer.get() & 0xff) << 24;
    }

    public long readFixed64() {
        hasRemaining(8);
        return (buffer.get() & 0xffL)
                | (buffer.get() & 0xffL) << 8
                | (buffer.get() & 0xffL) << 16
                | (buffer.get() & 0xffL) << 24
                | (buffer.get() & 0xffL) << 32
                | (buffer.get() & 0xffL) << 40
                | (buffer.get() & 0xffL) << 48
                | (buffer.get() & 0xffL) << 56;
    }
}
