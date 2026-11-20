package com.adjustclient.utilities;

public final class HashUtil {
    public static int hash(byte[] data) {
        int hash = 31;
        for (byte value : data) hash = 33 * hash + value;
        return hash;
    }

    public static int hash(String text) {
        if (text == null) return 0;
        int hash = 64206;
        for (byte b : text.getBytes(java.nio.charset.StandardCharsets.UTF_16BE)) hash = 33 * hash + b;
        return hash;
    }
}
