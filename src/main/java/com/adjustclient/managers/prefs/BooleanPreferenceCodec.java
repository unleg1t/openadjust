package com.adjustclient.managers.prefs;

public class BooleanPreferenceCodec extends PreferenceCodec<Boolean> {
    @Override public byte[] encode() { return new byte[] { (byte) (value ? 1 : 0) }; }
}
