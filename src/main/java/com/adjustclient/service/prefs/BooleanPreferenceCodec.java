package com.adjustclient.service.prefs;

public class BooleanPreferenceCodec extends PreferenceCodec<Boolean> {
    @Override public byte[] encode() { return new byte[] { (byte) (value ? 1 : 0) }; }
}
