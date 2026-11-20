package com.adjustclient.modules.impl.movement.scaffold;

import com.adjustclient.core.settings.ModeOption;

public enum TowerMovement implements ModeOption {
    NORMAL("Normal"),
    STOP("Stop"),
    CENTER("Center");

    private final String name;

    TowerMovement(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

}
