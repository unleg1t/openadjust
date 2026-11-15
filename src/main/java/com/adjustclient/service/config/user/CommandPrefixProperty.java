package com.adjustclient.service.config.user;

import com.adjustclient.setting.StringSetting;
import java.util.List;

public class CommandPrefixProperty {
    public final StringSetting setting;
    public final char defaultChar;
    private boolean updating;
    public Object field12;

    public CommandPrefixProperty(String name, char defaultChar) {
        this.defaultChar = defaultChar;
        setting = new StringSetting(null, name, List.of(String.valueOf(defaultChar)), 1, 1, null);
        setting.previousValue = null;
        setting.addListener(() -> {
            if (updating) return;
            updating = true;
            char value = defaultChar;
            try {
                value = getChar();
                for (CommandPrefixProperty other : UserConfiguration.PREFIXES) {
                    if (other != this && other.getChar() == value) {
                        value = defaultChar;
                        break;
                    }
                }
            } catch (Exception ignored) {
                value = defaultChar;
            }
            setChar(value);
            updating = false;
        });
    }

    public String serialize() { return String.valueOf(getChar()); }

    public void setChar(char value) {
        setting.getValue().clear();
        setting.getValue().add(String.valueOf(value));
        setting.notifyListeners(setting.getValue(), setting.getValue());
    }

    public char getChar() { return setting.getValue().getFirst().charAt(0); }
}
