package com.adjustclient.managers.prefs;

public class EnumPreferenceCodec<E extends Enum<E>> extends PreferenceCodec<E> {
    public E[] choices;
    @Override public byte[] encode() { return new byte[] { (byte) value.ordinal() }; }
}
