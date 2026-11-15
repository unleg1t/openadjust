package com.adjustclient.setting;

@FunctionalInterface
public interface SettingChangeListener<T> {
    void onChange(T oldValue, T newValue);
}
