package com.adjustclient.core.settings;

@FunctionalInterface
public interface SettingChangeListener<T> {
    void onChange(T oldValue, T newValue);
}
