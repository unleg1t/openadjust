package com.adjustclient.setting;

import java.util.List;

public interface SettingContainer {
    List<Setting<?>> getSettings();

    default List<Setting<?>> B_() {
        return getSettings();
    }

    default void addSetting(Setting<?> setting, int index) {
        List<Setting<?>> settings = getSettings();
        if (settings.contains(setting) && settings.size() > index) {
            settings.remove(setting);
            settings.add(index, setting);
        }
    }
}
