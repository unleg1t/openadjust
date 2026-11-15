package com.adjustclient.service.prefs;

public class IntPreferenceCodec extends PreferenceCodec<Integer> {
    @Override public byte[] encode() {
        int n = value;
        return new byte[] { (byte) (n >>> 24), (byte) (n >>> 16), (byte) (n >>> 8), (byte) n };
    }
}
