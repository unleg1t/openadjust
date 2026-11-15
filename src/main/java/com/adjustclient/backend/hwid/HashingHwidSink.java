package com.adjustclient.backend.hwid;

import com.google.common.hash.Hasher;
import java.nio.charset.StandardCharsets;

public class HashingHwidSink implements HwidSink {
    private final Hasher hasher;

    public HashingHwidSink(Hasher hasher) {
        this.hasher = hasher;
    }

    @Override
    public void a(int value) {
        this.hasher.putInt(value);
    }

    @Override
    public void a(String value) {
        String text = value != null ? value : "null";
        this.hasher.putString(text, StandardCharsets.UTF_16);
    }

    @Override
    public void b(int code) {
        for (int i = 0; i < 3; i++) {
            this.hasher.putInt(code + i * i);
        }
        this.hasher.putInt(code);
    }
}
