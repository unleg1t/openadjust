package com.adjustclient.service.config.user;

import com.adjustclient.setting.StringSetting;
import java.util.List;
import java.util.UUID;

public class ApiKeyProperty {
    public final StringSetting setting;
    public UUID key;
    public String keyString;
    public final String name;
    private boolean updating;
    public Object field24;

    public ApiKeyProperty(UserConfiguration owner, String name) {
        this.name = name;
        setting = new StringSetting(null, name + " Key", List.of(""), 1, 36, null);
        setting.field45 = true;
        setting.previousValue = null;
        setting.addListener(() -> {
            if (updating) return;
            updating = true;
            key = setting.getValue().isEmpty() ? null : parse(setting.getValue().getFirst());
            keyString = key == null ? null : key.toString();
            setting.getValue().clear();
            setting.getValue().add(keyString == null ? "" : keyString);
            setting.notifyListeners(setting.getValue(), setting.getValue());
            updating = false;
        });
    }

    public static UUID parse(String text) {
        try { return UUID.fromString(text); }
        catch (IllegalArgumentException error) { return null; }
    }

    public void setKey(UUID key) {
        this.key = key;
        keyString = key == null ? null : key.toString();
        setting.getValue().clear();
        setting.getValue().add(keyString == null ? "" : keyString);
        setting.notifyListeners(setting.getValue(), setting.getValue());
    }
}
