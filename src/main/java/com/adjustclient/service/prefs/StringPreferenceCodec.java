package com.adjustclient.service.prefs;

public class StringPreferenceCodec extends PreferenceCodec<String> {
    @Override public byte[] encode() {
        char[] chars = value.toCharArray();
        byte[] bytes = new byte[2 + chars.length * 2];
        if (chars.length >= 65536) throw new NullPointerException();
        bytes[0] = (byte) (chars.length >>> 8);
        bytes[1] = (byte) chars.length;
        for (int i = 0; i < chars.length; i++) {
            bytes[2 + i * 2] = (byte) (chars[i] >>> 8);
            bytes[3 + i * 2] = (byte) chars[i];
        }
        return bytes;
    }
}
