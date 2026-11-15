package com.adjustclient.service.prefs;

public abstract class PreferenceCodec<T> {
    public T value;
    public T lastSentValue;
    public Object field12;

    public boolean isChanged() {
        if (value == null) return false;
        if (lastSentValue == null) return true;
        return differsFromLastSent();
    }

    public boolean differsFromLastSent() {
        return !lastSentValue.equals(value);
    }

    public abstract byte[] encode();
}
