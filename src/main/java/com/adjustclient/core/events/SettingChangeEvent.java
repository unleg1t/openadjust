package com.adjustclient.core.events;

public final class SettingChangeEvent extends Event {
    public com.adjustclient.core.settings.Setting<?> setting;
    public Object value;

    public SettingChangeEvent(com.adjustclient.core.settings.Setting<?> setting, Object value) {
        this.setting = setting;
        this.value = value;
    }
}
