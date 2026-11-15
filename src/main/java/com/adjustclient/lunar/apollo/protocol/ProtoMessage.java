package com.adjustclient.lunar.apollo.protocol;

import com.adjustclient.util.proto.ProtoWriter;
import java.nio.ByteBuffer;

public interface ProtoMessage {
    default void writeTo(ProtoWriter writer) {
    }

    default byte[] toByteArray() {
        ProtoWriter writer = new ProtoWriter();
        writeTo(writer);
        ByteBuffer buffer = writer.toByteBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }
}
