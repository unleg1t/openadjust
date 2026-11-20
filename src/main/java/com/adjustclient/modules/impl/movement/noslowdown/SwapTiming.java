package com.adjustclient.modules.impl.movement.noslowdown;

import com.adjustclient.core.settings.ModeOption;

public enum SwapTiming implements ModeOption {
    ONCE("Once"), BLINK("Blink"), CONSTANT("Constant");
    private final String name;

    SwapTiming(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}
