package com.adjustclient.module.impl.combat.autoblock.mode;

import com.adjustclient.setting.ModeOption;

public enum PingMode implements ModeOption {
    AUTOMATIC("Automatic"),
    STATIC("Static");

    private final String name;

    PingMode(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
