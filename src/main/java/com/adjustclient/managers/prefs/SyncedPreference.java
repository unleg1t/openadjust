package com.adjustclient.managers.prefs;

public class SyncedPreference<T extends PreferenceCodec<?>> {
    public T codec;
    public java.util.function.BooleanSupplier condition;
    public int id;
    public String name;
}
