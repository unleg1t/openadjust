package com.adjustclient.module.impl.movement.noslowdown;

import com.adjustclient.setting.ModeOption;

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
