package com.adjustclient.modules.impl.movement.highjump;

import com.adjustclient.core.settings.ModeOption;

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
