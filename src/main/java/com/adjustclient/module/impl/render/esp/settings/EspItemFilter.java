package com.adjustclient.module.impl.render.esp.settings;

import com.adjustclient.setting.ModeOption;

public enum EspItemFilter implements ModeOption {
    BLACKLIST("Blacklist"), WHITELIST("Whitelist");

    private final String name;
    private Object field20;

    EspItemFilter(String name) {
        this.name = name;
    }

    @Override public String getName() { return name; }
}
