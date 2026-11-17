package com.adjustclient.module.impl.movement.highjump;

import com.adjustclient.setting.ModeOption;

public enum HighJumpMode implements ModeOption {
    REWOUND("Rewound");

    private final String name;

    HighJumpMode(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
