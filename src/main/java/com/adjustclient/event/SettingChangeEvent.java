package com.adjustclient.event;

public final class SettingChangeEvent extends Event {
    public com.adjustclient.setting.Setting<?> setting;
    public Object value;

    public SettingChangeEvent(com.adjustclient.setting.Setting<?> setting, Object value) {
        this.setting = setting;
        this.value = value;
    }
}
