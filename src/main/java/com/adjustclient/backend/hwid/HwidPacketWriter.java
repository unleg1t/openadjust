package com.adjustclient.backend.hwid;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class HwidPacketWriter {
    public static void a(byte[] data) {
        byte[] key = "Xg2KaZdfOsn47ovVkC6t0FqN2AQo8Mmg".getBytes(StandardCharsets.UTF_16BE);
        for (int i = 0; i < data.length; i++) {
            data[i] ^= key[i % key.length];
            data[i] ^= (byte) (i + (key[i % (key.length - 4)] ^ 0x60) + 65 + (i + 2) % 5);
        }
    }

    public static void a(int tag, String value, DataOutputStream out) throws IOException {
        out.write(tag);
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        int hash = 31;
        for (byte b : bytes) {
            hash = 33 * hash + b;
        }
        out.writeInt(hash);
        a(bytes);
        out.writeInt(bytes.length);
        out.write(bytes);
    }
}
