package com.adjustclient.module.impl.movement.scaffold;

import com.adjustclient.setting.ModeOption;

public enum NoAscendMode implements ModeOption {
    FLAT("Flat"),
    BUFFER("Buffer");

    private final String name;

    NoAscendMode(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

}
