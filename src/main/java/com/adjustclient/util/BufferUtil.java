package com.adjustclient.util;

import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

public final class BufferUtil {
    public static ByteBuffer resize(ByteBuffer buffer, int capacity) {
        ByteBuffer replacement = BufferUtils.createByteBuffer(capacity);
        buffer.flip();
        replacement.put(buffer);
        return replacement;
    }

    public static ByteBuffer read(InputStream stream) {
        try {
            ByteBuffer buffer;
            try (ReadableByteChannel channel = Channels.newChannel(stream)) {
                buffer = BufferUtils.createByteBuffer(0x80000);
                while (channel.read(buffer) != -1) {
                    if (!buffer.hasRemaining()) buffer = resize(buffer, 3 * buffer.capacity() / 2);
                }
            }
            buffer.flip();
            return MemoryUtil.memSlice(buffer);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
}
